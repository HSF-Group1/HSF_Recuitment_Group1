package com.group1.recruitment.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Password hashing utility backed by Spring Security Crypto's BCrypt.
 *
 * <p>Only the {@code spring-security-crypto} artifact is on the classpath, so
 * no Spring Security filter chain or auto-configuration is pulled in — this is
 * purely the hashing algorithm. Each hash embeds its own random salt and cost
 * factor, and verification is timing-safe.
 *
 * <p>Only two methods are exposed to the rest of the app, exactly as the spec
 * requires: {@link #hash(String)} and {@link #matches(String, String)}.
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Hash a raw password into BCrypt's self-describing storage format. */
    public String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be null");
        }
        return encoder.encode(rawPassword);
    }

    /** Verify a raw password against a previously stored hash. */
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return encoder.matches(rawPassword, storedHash);
    }
}
