package com.group1.recruitment.security;

/** Keys used to bind authentication data into the {@code HttpSession}. */
public final class SessionConstants {

    /** The authenticated {@link SessionUser}, bound on successful login. */
    public static final String LOGGED_IN_USER = "LOGGED_IN_USER";

    /** The originally requested URL, stored when a guest is bounced to login. */
    public static final String REDIRECT_AFTER_LOGIN = "REDIRECT_AFTER_LOGIN";

    private SessionConstants() {
    }
}
