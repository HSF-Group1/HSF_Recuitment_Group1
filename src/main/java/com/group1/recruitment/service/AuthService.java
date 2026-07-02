package com.group1.recruitment.service;

import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** SCR-01 Login, SCR-03 Register, SCR-05 Password Change. */
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

    /** Result of a sign-in attempt. */
    public record AuthResult(boolean success, boolean locked, User user) {
        static AuthResult failure() { return new AuthResult(false, false, null); }
        static AuthResult lockedOut() { return new AuthResult(false, true, null); }
        static AuthResult ok(User u) { return new AuthResult(true, false, u); }
    }

    /**
     * Authenticate by username OR email using passwordUtil.matches().
     * Wrong username and wrong password return the same generic failure
     * (no account enumeration); a locked account is signalled separately.
     */
    public AuthResult authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) {
            return AuthResult.failure();
        }
        String key = usernameOrEmail.trim();
        User user = userRepository.findByUsernameOrEmail(key, key).orElse(null);
        if (user == null) {
            return AuthResult.failure();
        }
        if (user.getStatus() == AccountStatus.LOCKED) {
            return AuthResult.lockedOut();
        }
        if (user.getStatus() != AccountStatus.ACTIVE) {
            return AuthResult.failure(); // INACTIVE accounts cannot sign in
        }
        if (!passwordUtil.matches(rawPassword, user.getPasswordHash())) {
            return AuthResult.failure();
        }
        return AuthResult.ok(user);
    }

    /** SCR-03: self-service candidate registration. */
    @Transactional
    public User register(String fullName, String username, String email, String password, String confirm) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(fullName)) errors.put("fullName", "Full name is required.");
        if (isBlank(username)) {
            errors.put("username", "Username is required.");
        } else if (!username.trim().matches("[A-Za-z0-9_]{4,50}")) {
            errors.put("username", "4–50 characters. Letters, digits, and underscores only.");
        } else if (userRepository.existsByUsername(username.trim())) {
            errors.put("username", "This username is already taken. Please choose another.");
        }
        if (isBlank(email)) {
            errors.put("email", "Email address is required.");
        } else if (!email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.put("email", "Please enter a valid email address.");
        } else if (userRepository.existsByEmail(email.trim())) {
            errors.put("email", "This email address is already registered.");
        }
        String pwError = passwordComplexityError(password);
        if (pwError != null) errors.put("password", pwError);
        if (!nullSafeEquals(password, confirm)) errors.put("confirmPassword", "Passwords do not match.");

        if (!errors.isEmpty()) throw new ValidationException(errors);

        Role candidateRole = roleRepository.findByName(SessionUser.ROLE_CANDIDATE)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(SessionUser.ROLE_CANDIDATE);
                    r.setDescription("Candidate");
                    return roleRepository.save(r);
                });

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordUtil.hash(password));
        user.setRole(candidateRole);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        Candidate candidate = new Candidate();
        candidate.setUser(user);
        candidateRepository.save(candidate);

        return user;
    }

    /** SCR-05: change password after re-authenticating the current one. */
    @Transactional
    public void changePassword(Long userId, String current, String newPassword, String confirm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ValidationException.global("User not found."));
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(current) || !passwordUtil.matches(current, user.getPasswordHash())) {
            errors.put("currentPassword", "Incorrect current password.");
        }
        String pwError = passwordComplexityError(newPassword);
        if (pwError != null) {
            errors.put("newPassword", pwError);
        } else if (newPassword.equals(current)) {
            errors.put("newPassword", "New password must be different from your current password.");
        }
        if (!nullSafeEquals(newPassword, confirm)) {
            errors.put("confirmPassword", "Passwords do not match.");
        }
        if (!errors.isEmpty()) throw new ValidationException(errors);

        user.setPasswordHash(passwordUtil.hash(newPassword));
        userRepository.save(user);
    }

    /** Shared complexity rule for register / change / reset. */
    public String passwordComplexityError(String password) {
        if (password == null || password.length() < 8) {
            return "At least 8 characters, including 1 uppercase letter and 1 number.";
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUpper || !hasDigit) {
            return "At least 8 characters, including 1 uppercase letter and 1 number.";
        }
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private boolean nullSafeEquals(String a, String b) { return a != null && a.equals(b); }
}
