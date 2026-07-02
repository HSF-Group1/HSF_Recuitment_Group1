package com.group1.recruitment.security;

/** Session attribute keys (see screen specification §1). */
public final class SessionConstants {

    /** The signed-in {@link SessionUser} is stored under this key. */
    public static final String LOGGED_IN_USER = "LOGGED_IN_USER";

    /** Where to send the user after login when they hit a protected page first. */
    public static final String REDIRECT_AFTER_LOGIN = "REDIRECT_AFTER_LOGIN";

    private SessionConstants() {
    }
}
