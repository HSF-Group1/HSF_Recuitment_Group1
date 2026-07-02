package com.group1.recruitment.controller;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** SCR-04 User Profile, SCR-05 Password Change. */
@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public ProfileController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    // ---------------- SCR-04 User Profile ----------------

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        // Re-query the DB by the session-bound id so the page always reflects
        // current state (spec: "guarantee state synchronization").
        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        model.addAttribute("user", user);
        return "profile/view";
    }

    // ---------------- SCR-05 Password Change ----------------

    @GetMapping("/profile/change-password")
    public String changePasswordPage(HttpSession session) {
        SessionUtil.require(session);
        return "profile/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra,
                                 Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        try {
            authService.changePassword(sessionUser.getId(), currentPassword, newPassword, confirmPassword);
            ra.addFlashAttribute("flash", "Your password has been changed.");
            return "redirect:/profile";
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            return "profile/change-password";
        }
    }
}
