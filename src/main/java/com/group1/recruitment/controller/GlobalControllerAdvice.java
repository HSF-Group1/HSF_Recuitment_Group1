package com.group1.recruitment.controller;

import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Exposes the authenticated user to every Thymeleaf view as {@code currentUser}. */
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpSession session) {
        return SessionUtil.current(session);
    }
}
