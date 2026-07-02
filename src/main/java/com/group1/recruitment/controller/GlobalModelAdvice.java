package com.group1.recruitment.controller;

import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the signed-in user to every view as {@code currentUser} so the
 * shared layout can render the top-nav username dropdown (SCR-04 entry).
 */
@ControllerAdvice(basePackages = "com.group1.recruitment.controller")
public class GlobalModelAdvice {

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpSession session) {
        return SessionUtil.current(session);
    }
}
