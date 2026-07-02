package com.group1.recruitment.security;

import com.group1.recruitment.exception.AccessDeniedException;
import jakarta.servlet.http.HttpSession;

/** Small helpers around the session-bound {@link SessionUser}. */
public final class SessionUtil {

    private SessionUtil() {
    }

    /** @return the signed-in user, or null for guests. */
    public static SessionUser current(HttpSession session) {
        return session == null ? null : (SessionUser) session.getAttribute(SessionConstants.LOGGED_IN_USER);
    }

    /** @return the signed-in user, never null (throws 403 otherwise). */
    public static SessionUser require(HttpSession session) {
        SessionUser user = current(session);
        if (user == null) {
            throw new AccessDeniedException("Sign in required.");
        }
        return user;
    }

    public static void login(HttpSession session, SessionUser user) {
        session.setAttribute(SessionConstants.LOGGED_IN_USER, user);
    }
}
