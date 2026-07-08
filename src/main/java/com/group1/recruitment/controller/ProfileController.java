package com.group1.recruitment.controller;

import com.group1.recruitment.dto.ChangePasswordForm;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public ProfileController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }


    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        model.addAttribute("user", user);
        return "profile/view";
    }

    // -------------------------------------------------- SCR-05 Change password

    @GetMapping("/profile/change-password")
    public String changePasswordForm(HttpSession session, Model model) {
        SessionUtil.require(session);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "profile/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult binding, HttpSession session, RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = SessionUtil.require(session);
        boolean ok = authService.changePassword(sessionUser.getId(), form, binding);
        if (!ok) {
            return "profile/change-password";
        }
        redirectAttributes.addFlashAttribute("flashMessage", "Your password has been changed.");
        return "redirect:/profile";
    }
}
