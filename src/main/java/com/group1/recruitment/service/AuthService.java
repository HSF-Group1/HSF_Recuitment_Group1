package com.group1.recruitment.service;

import com.group1.recruitment.dto.ChangePasswordForm;
import com.group1.recruitment.dto.RegisterForm;
import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Business logic for authentication, self-registration and password change.
 * Cross-field / database rules are reported back through Spring's {@link Errors}
 * (the caller's {@code BindingResult}) so the templates can render field-level
 * messages, exactly as the spec's "@Valid + BindingResult" flow intends.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordUtil passwordUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       CandidateRepository candidateRepository, PasswordUtil passwordUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.candidateRepository = candidateRepository;
        this.passwordUtil = passwordUtil;
    }

    // ---------------------------------------------------------------- login

    public enum Outcome { SUCCESS, BAD_CREDENTIALS, LOCKED, INACTIVE }

    public record AuthResult(Outcome outcome, User user) {
        public boolean isSuccess() {
            return outcome == Outcome.SUCCESS;
        }
    }

    /** Verify credentials and account status. */
    @Transactional(readOnly = true)
    public AuthResult authenticate(String usernameOrEmail, String rawPassword) {
        String key = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        Optional<User> found = userRepository.findByUsernameOrEmail(key, key);
        if (found.isEmpty()) {
            return new AuthResult(Outcome.BAD_CREDENTIALS, null);
        }
        User user = found.get();
        if (!passwordUtil.matches(rawPassword, user.getPasswordHash())) {
            return new AuthResult(Outcome.BAD_CREDENTIALS, null);
        }
        // Credentials are valid — now gate on account status.
        if (user.getStatus() == AccountStatus.LOCKED) {
            return new AuthResult(Outcome.LOCKED, user);
        }
        if (user.getStatus() == AccountStatus.INACTIVE) {
            return new AuthResult(Outcome.INACTIVE, user);
        }
        return new AuthResult(Outcome.SUCCESS, user);
    }

    // ------------------------------------------------------------- register

    /**
     * Validate and persist a new CANDIDATE account.
     *
     * @return the saved user, or {@code null} if validation errors were added.
     */
    @Transactional
    public User register(RegisterForm form, Errors errors) {
        String email = form.getEmail() == null ? "" : form.getEmail().trim().toLowerCase();

        if (!errors.hasFieldErrors("email") && userRepository.existsByEmail(email)) {
            errors.rejectValue("email", "email.taken", "An account with this email already exists.");
        }
        if (!errors.hasFieldErrors("password")) {
            String complexity = passwordComplexityError(form.getPassword());
            if (complexity != null) {
                errors.rejectValue("password", "password.weak", complexity);
            }
        }
        if (!errors.hasFieldErrors("confirmPassword")
                && !equalsSafe(form.getPassword(), form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }
        if (errors.hasErrors()) {
            return null;
        }

        Role candidateRole = roleRepository.findByName(SessionUser.CANDIDATE)
                .orElseThrow(() -> new IllegalStateException("CANDIDATE role is not seeded"));

        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setUsername(deriveUniqueUsername(email));
        user.setPasswordHash(passwordUtil.hash(form.getPassword()));
        user.setRole(candidateRole);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        // Every candidate account gets a matching candidate profile row.
        Candidate candidate = new Candidate();
        candidate.setUser(saved);
        candidateRepository.save(candidate);

        return saved;
    }

    // -------------------------------------------------------- change password

    /**
     * Change the password of the given user after re-verifying the current one.
     *
     * @return {@code true} on success; otherwise errors were added to {@code errors}.
     */
    @Transactional
    public boolean changePassword(Long userId, ChangePasswordForm form, Errors errors) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));

        if (!errors.hasFieldErrors("currentPassword")
                && !passwordUtil.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            errors.rejectValue("currentPassword", "password.current.wrong", "Your current password is incorrect.");
        }
        if (!errors.hasFieldErrors("newPassword")) {
            String complexity = passwordComplexityError(form.getNewPassword());
            if (complexity != null) {
                errors.rejectValue("newPassword", "password.weak", complexity);
            } else if (passwordUtil.matches(form.getNewPassword(), user.getPasswordHash())) {
                errors.rejectValue("newPassword", "password.reuse",
                        "New password must be different from the current one.");
            }
        }
        if (!errors.hasFieldErrors("confirmPassword")
                && !equalsSafe(form.getNewPassword(), form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }
        if (errors.hasErrors()) {
            return false;
        }

        user.setPasswordHash(passwordUtil.hash(form.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    // ------------------------------------------------------------- helpers

    /** @return an error message if the password is too weak, else {@code null}. */
    public String passwordComplexityError(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUpper || !hasDigit) {
            return "Password must include at least one uppercase letter and one digit.";
        }
        return null;
    }

    private String deriveUniqueUsername(String email) {
        String base = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        base = base.replaceAll("[^A-Za-z0-9_]", "");
        if (base.length() < 4) {
            base = ("user" + base);
        }
        if (base.length() > 45) {
            base = base.substring(0, 45);
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private static boolean equalsSafe(String a, String b) {
        return a != null && a.equals(b);
    }
}
