package com.group1.recruitment.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Gatekeeper for the whole application. Any request that is not on the public
 * allow-list and does not carry an authenticated {@link SessionUser} is bounced
 * to the login page, remembering where the user was headed so we can send them
 * back after a successful login.
 */
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * Public prefixes that never require a session. Mirrors the spec's
     * interceptor exclusions ({@code /auth/**, /css/**, /js/**, /error/**, /})
     * plus the static asset roots this UI actually serves.
     */
    private static final String[] PUBLIC_PREFIXES = {
            "/auth/",
            "/css/",
            "/js/",
            "/images/",
            "/webjars/",
            "/error"
    };

    /** Exact public paths (aliases that immediately redirect into /auth/**). */
    private static final String[] PUBLIC_EXACT = {
            "/", "/favicon.ico", "/login", "/register", "/forgot-password"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (isPublic(path) || SessionUtil.isAuthenticated(request.getSession(true))) {
            return true;
        }

        // Remember the target (with query string) so login can round-trip back.
        HttpSession session = request.getSession(true);
        String target = request.getRequestURI();
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }
        session.setAttribute(SessionConstants.REDIRECT_AFTER_LOGIN, target);

        response.sendRedirect(request.getContextPath() + "/auth/login");
        return false;
    }

    private boolean isPublic(String path) {
        for (String exact : PUBLIC_EXACT) {
            if (path.equals(exact)) {
                return true;
            }
        }
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
