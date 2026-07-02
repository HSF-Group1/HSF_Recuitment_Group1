package com.group1.recruitment.security;

import com.group1.recruitment.entity.User;

import java.io.Serializable;

/**
 * Immutable, serializable snapshot of the signed-in user bound to the
 * HttpSession under {@link SessionConstants#LOGGED_IN_USER}. Never store
 * JPA entities in the session — they detach and go stale.
 */
public class SessionUser implements Serializable {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_HR = "HR_MANAGER";
    public static final String ROLE_INTERVIEWER = "INTERVIEWER";
    public static final String ROLE_CANDIDATE = "CANDIDATE";

    private final Long id;
    private final String fullName;
    private final String username;
    private final String email;
    private final String roleName;

    public SessionUser(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roleName = user.getRole() != null ? user.getRole().getName() : "";
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRoleName() { return roleName; }

    public boolean isAdmin() { return ROLE_ADMIN.equals(roleName); }
    public boolean isHr() { return ROLE_HR.equals(roleName); }
    public boolean isInterviewer() { return ROLE_INTERVIEWER.equals(roleName); }
    public boolean isCandidate() { return ROLE_CANDIDATE.equals(roleName); }

    /** Human-readable role label for badges. */
    public String roleLabel() {
        return switch (roleName) {
            case ROLE_ADMIN -> "Admin";
            case ROLE_HR -> "HR Manager";
            case ROLE_INTERVIEWER -> "Interviewer";
            case ROLE_CANDIDATE -> "Candidate";
            default -> roleName;
        };
    }

    /**
     * Landing page after login. The spec routes each role to its own dashboard
     * (/admin/dashboard, /hr/dashboard, ...); those modules are owned by other
     * team members and do not exist yet, so until they land everyone goes to
     * SCR-04 User Profile. Swap the cases in when the dashboards are merged.
     */
    public String homePath() {
        return "/profile";
    }
}
