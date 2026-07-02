package com.group1.recruitment.security;

import jakarta.servlet.http.HttpSession;

/** Small helper around reading/writing the {@link SessionUser} in session. */
public final class SessionUtil {

    private SessionUtil() {
    }

    /** @return the logged-in user, or {@code null} if the session is anonymous. */
    public static SessionUser current(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SessionConstants.LOGGED_IN_USER);
        return value instanceof SessionUser sessionUser ? sessionUser : null;
    }

    /**
     * @return the logged-in user, never {@code null}.
     * @throws IllegalStateException if no user is bound (interceptor should
     *         have prevented this from ever happening on a protected route).
     */
    public static SessionUser require(HttpSession session) {
        SessionUser user = current(session);
        if (user == null) {
            throw new IllegalStateException("No authenticated user bound to the session");
        }
        return user;
    }

    /** Bind a freshly authenticated user into the session. */
    public static void login(HttpSession session, SessionUser user) {
        session.setAttribute(SessionConstants.LOGGED_IN_USER, user);
    }

    public static boolean isAuthenticated(HttpSession session) {
        return current(session) != null;
    }
}
