package com.group1.recruitment.service;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Issues and consumes time-bound password-reset tokens (SCR-02).
 *
 * <p>Tokens live in memory only — there is no SMTP server wired up in this
 * assignment, so the controller surfaces the generated reset link on screen as
 * a "simulated email". Tokens are single-use and expire after 30 minutes.
 */
@Service
public class PasswordResetService {

    public static final long TOKEN_TTL_MINUTES = 30;

    private static final SecureRandom RANDOM = new SecureRandom();
    private final ConcurrentHashMap<String, TokenEntry> tokens = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public PasswordResetService(UserRepository userRepository, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.passwordUtil = passwordUtil;
    }

    private record TokenEntry(Long userId, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    /**
     * Create a reset token for the account owning {@code email}.
     *
     * @return the token if the email maps to a user, otherwise empty. The caller
     *         must NOT reveal which case occurred (anti-enumeration).
     */
    @Transactional(readOnly = true)
    public Optional<String> createToken(String email) {
        purgeExpired();
        String key = email == null ? "" : email.trim().toLowerCase();
        Optional<User> user = userRepository.findByEmail(key);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        tokens.put(token, new TokenEntry(user.get().getId(),
                Instant.now().plusSeconds(TOKEN_TTL_MINUTES * 60)));
        return Optional.of(token);
    }

    /** @return true if the token exists and has not expired. */
    public boolean isValid(String token) {
        if (token == null) {
            return false;
        }
        TokenEntry entry = tokens.get(token);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    /**
     * Consume a token and set the new password. The token stays alive if the
     * lookup fails, but is removed (single-use) once the password is updated.
     *
     * @return true if the password was reset.
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        TokenEntry entry = tokens.get(token);
        if (entry == null || entry.isExpired()) {
            tokens.remove(token);
            return false;
        }
        Optional<User> user = userRepository.findById(entry.userId());
        if (user.isEmpty()) {
            tokens.remove(token);
            return false;
        }
        User u = user.get();
        u.setPasswordHash(passwordUtil.hash(newPassword));
        userRepository.save(u);
        tokens.remove(token); // single use
        return true;
    }

    private void purgeExpired() {
        tokens.values().removeIf(TokenEntry::isExpired);
    }
}
