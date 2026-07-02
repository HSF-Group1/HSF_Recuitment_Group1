package com.group1.recruitment.controller;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.AuthService;
import com.group1.recruitment.service.PasswordResetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** SCR-01 User Login, SCR-02 Password Reset, SCR-03 User Register, plus logout. */
@Controller
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    // ---------------- SCR-01 User Login ----------------

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        SessionUser user = SessionUtil.current(session);
        if (user != null) {
            return "redirect:" + user.homePath();
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String usernameOrEmail,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        AuthService.AuthResult result = authService.authenticate(usernameOrEmail, password);
        if (result.success()) {
            User user = result.user();
            SessionUser sessionUser = new SessionUser(user);
            SessionUtil.login(session, sessionUser);

            Object redirect = session.getAttribute(SessionConstants.REDIRECT_AFTER_LOGIN);
            session.removeAttribute(SessionConstants.REDIRECT_AFTER_LOGIN);
            if (redirect instanceof String target && !target.contains("/login")) {
                return "redirect:" + target;
            }
            return "redirect:" + sessionUser.homePath();
        }
        if (result.locked()) {
            model.addAttribute("lockout", true);
        } else {
            model.addAttribute("error", "Incorrect username or password.");
        }
        model.addAttribute("usernameOrEmail", usernameOrEmail);
        return "auth/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("flash", "You have been signed out.");
        return "redirect:/login";
    }

    // ---------------- SCR-03 User Register ----------------

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        SessionUser user = SessionUtil.current(session);
        if (user != null) {
            return "redirect:" + user.homePath();
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes ra,
                           Model model) {
        try {
            authService.register(fullName, username, email, password, confirmPassword);
            ra.addFlashAttribute("flash", "Account created successfully. Please sign in.");
            return "redirect:/login";
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("fullName", fullName);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "auth/register";
        }
    }

    // ---------------- SCR-02 Password Reset ----------------

    @GetMapping("/reset-password")
    public String resetRequestPage() {
        return "auth/reset-request";
    }

    @PostMapping("/reset-password")
    public String resetRequest(@RequestParam String email, Model model) {
        // Same confirmation whether or not the email exists (no enumeration).
        passwordResetService.createToken(email).ifPresent(token ->
                // No SMTP in this milestone: surface the link on screen,
                // clearly labelled as the simulated email.
                model.addAttribute("demoResetLink", "/reset-password/confirm?token=" + token));
        model.addAttribute("submitted", true);
        model.addAttribute("ttlMinutes", PasswordResetService.TOKEN_TTL_MINUTES);
        return "auth/reset-request";
    }

    @GetMapping("/reset-password/confirm")
    public String resetConfirmPage(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("invalidToken", !passwordResetService.isValid(token));
        model.addAttribute("token", token);
        return "auth/reset-confirm";
    }

    @PostMapping("/reset-password/confirm")
    public String resetConfirm(@RequestParam String token,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes ra,
                               Model model) {
        try {
            passwordResetService.resetPassword(token, newPassword, confirmPassword);
            ra.addFlashAttribute("flash", "Password updated — please sign in with your new password.");
            return "redirect:/login";
        } catch (ValidationException ex) {
            boolean tokenDead = !passwordResetService.isValid(token);
            model.addAttribute("invalidToken", tokenDead);
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("token", token);
            return "auth/reset-confirm";
        }
    }
}
