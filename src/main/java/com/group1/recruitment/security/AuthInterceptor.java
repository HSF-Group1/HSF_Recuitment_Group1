package com.group1.recruitment.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Session-based authentication gate (SCR-01: any protected route redirects
 * to /login, remembering the original destination).
 *
 * Only the auth feature (SCR-01..05) lives in this module, so the interceptor
 * does authentication only; role-based authorization belongs to the modules
 * that own /admin/**, /hr/** etc. and can be layered on here later.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (isPublic(path)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        SessionUser user = SessionUtil.current(session);
        if (user == null) {
            String target = request.getRequestURI();
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }
            request.getSession(true).setAttribute(SessionConstants.REDIRECT_AFTER_LOGIN, target);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    private boolean isPublic(String path) {
        return path.equals("/")                     // skeleton welcome page
                || path.equals("/login")            // SCR-01
                || path.equals("/logout")
                || path.equals("/register")         // SCR-03
                || path.startsWith("/reset-password") // SCR-02 (request + confirm)
                || path.startsWith("/examples")     // skeleton demo pages
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.startsWith("/webjars")
                || path.startsWith("/error")
                || path.equals("/favicon.ico");
    }
}
