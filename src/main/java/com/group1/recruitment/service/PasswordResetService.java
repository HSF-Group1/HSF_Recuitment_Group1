package com.group1.recruitment.service;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SCR-02 Password Reset: time-bound single-use tokens.
 *
 * Tokens live in memory (30 minutes, single instance) — no schema change to
 * the shared codebase. Email delivery is out of scope for this milestone, so
 * the controller shows the reset link on screen marked as a simulated email.
 */
@Service
public class PasswordResetService {

    public static final int TOKEN_TTL_MINUTES = 30;

    private record TokenEntry(Long userId, Instant expiresAt) {}

    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordUtil passwordUtil;

    public PasswordResetService(UserRepository userRepository, AuthService authService,
                                PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.passwordUtil = passwordUtil;
    }

    /**
     * Issue a token if (and only if) the email belongs to an account.
     * The caller must show the exact same message either way, so an
     * attacker cannot probe which emails are registered.
     */
    public Optional<String> createToken(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim()).map(user -> {
            purgeExpired();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            tokens.put(token, new TokenEntry(user.getId(),
                    Instant.now().plusSeconds(TOKEN_TTL_MINUTES * 60L)));
            return token;
        });
    }

    /** @return true if the token exists and has not expired. */
    public boolean isValid(String token) {
        if (token == null || token.isBlank()) return false;
        TokenEntry entry = tokens.get(token);
        return entry != null && entry.expiresAt().isAfter(Instant.now());
    }

    /** Validate + consume the token and set the new password (hashed). */
    @Transactional
    public void resetPassword(String token, String newPassword, String confirm) {
        TokenEntry entry = token == null ? null : tokens.get(token);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            throw ValidationException.global("This link has expired or has already been used.");
        }
        Map<String, String> errors = new LinkedHashMap<>();
        String pwError = authService.passwordComplexityError(newPassword);
        if (pwError != null) errors.put("newPassword", pwError);
        if (newPassword == null || !newPassword.equals(confirm)) {
            errors.put("confirmPassword", "Passwords do not match.");
        }
        if (!errors.isEmpty()) throw new ValidationException(errors);

        User user = userRepository.findById(entry.userId())
                .orElseThrow(() -> ValidationException.global("This link has expired or has already been used."));
        user.setPasswordHash(passwordUtil.hash(newPassword));
        userRepository.save(user);
        tokens.remove(token); // single use
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        tokens.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }
}
