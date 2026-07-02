package com.group1.recruitment.security;

import com.group1.recruitment.entity.User;

import java.io.Serializable;

/**
 * Lightweight, immutable snapshot of the authenticated user held in session.
 * We deliberately avoid parking a JPA-managed {@link User} entity in the
 * session (detached/lazy pitfalls); screens that need fresh data re-query the
 * DB by {@link #getId()} as the spec requires for state synchronisation.
 */
public class SessionUser implements Serializable {

    // Role names — must match the values seeded into the roles table.
    public static final String ADMIN = "ADMIN";
    public static final String HR_MANAGER = "HR_MANAGER";
    public static final String INTERVIEWER = "INTERVIEWER";
    public static final String CANDIDATE = "CANDIDATE";

    private final Long id;
    private final String fullName;
    private final String username;
    private final String email;
    private final String roleName;

    public SessionUser(Long id, String fullName, String username, String email, String roleName) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.roleName = roleName;
    }

    public static SessionUser from(User user) {
        String role = user.getRole() != null ? user.getRole().getName() : CANDIDATE;
        return new SessionUser(user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), role);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return roleName;
    }

    /** Human-friendly role label for badges in the UI. */
    public String getRoleLabel() {
        if (roleName == null) {
            return "";
        }
        return switch (roleName) {
            case ADMIN -> "Administrator";
            case HR_MANAGER -> "HR Manager";
            case INTERVIEWER -> "Interviewer";
            case CANDIDATE -> "Candidate";
            default -> roleName;
        };
    }

    /**
     * Landing page after login.
     *
     * <p>The specification maps each role to its own dashboard:
     * <ul>
     *   <li>ADMIN        &rarr; /admin/dashboard</li>
     *   <li>HR_MANAGER   &rarr; /hr/dashboard</li>
     *   <li>INTERVIEWER  &rarr; /interviewer/dashboard</li>
     *   <li>CANDIDATE    &rarr; /candidate/my-applications</li>
     * </ul>
     * Those dashboards belong to teammates' feature slices and do not exist in
     * this authentication-only module, so every role lands on the profile page
     * (which this module owns and which every authenticated user can reach).
     * Swap the body for the mapping above once the sibling screens are merged.
     */
    public String homePath() {
        return "/profile";
    }
}
