package com.group1.recruitment.controller;

import com.group1.recruitment.dto.ForgotPasswordForm;
import com.group1.recruitment.dto.LoginForm;
import com.group1.recruitment.dto.RegisterForm;
import com.group1.recruitment.dto.ResetPasswordForm;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.AuthService;
import com.group1.recruitment.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** SCR-01 Login, SCR-02 Password Reset, SCR-03 Register. */
@Controller
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    // --------------------------------------------------------- SCR-01 Login

    /** Friendly aliases — the screen list calls SCR-01 out as "Direct URL /login". */
    @GetMapping("/login")
    public String loginAlias() {
        return "redirect:/auth/login";
    }

    @GetMapping("/register")
    public String registerAlias() {
        return "redirect:/auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordAlias() {
        return "redirect:/auth/forgot-password";
    }

    /** Root: guests land on login, members on their home page. */
    @GetMapping("/home")
    public String home(HttpSession session) {
        SessionUser user = SessionUtil.current(session);
        return "redirect:" + (user != null ? user.homePath() : "/auth/login");
    }

    @GetMapping("/auth/login")
    public String loginForm(@RequestParam(required = false) String registered,
                            @RequestParam(required = false) String reset,
                            @RequestParam(required = false) String logout,
                            HttpSession session, Model model) {
        if (SessionUtil.isAuthenticated(session)) {
            return "redirect:" + SessionUtil.require(session).homePath();
        }
        model.addAttribute("loginForm", new LoginForm());
        if (registered != null) {
            model.addAttribute("infoMessage", "Your account was created. Please sign in.");
        }
        if (reset != null) {
            model.addAttribute("infoMessage", "Your password was reset. Please sign in.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been signed out.");
        }
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form, BindingResult binding,
                        HttpSession session, Model model) {
        if (binding.hasErrors()) {
            return "auth/login";
        }
        AuthService.AuthResult result = authService.authenticate(form.getUsernameOrEmail(), form.getPassword());
        switch (result.outcome()) {
            case SUCCESS -> {
                User user = result.user();
                SessionUser sessionUser = SessionUser.from(user);
                SessionUtil.login(session, sessionUser);
                return "redirect:" + resolvePostLoginTarget(session, sessionUser);
            }
            case LOCKED -> model.addAttribute("errorMessage",
                    "This account is locked. Please contact your administrator to unlock it.");
            case INACTIVE -> model.addAttribute("errorMessage",
                    "This account is inactive. Please contact your administrator.");
            case BAD_CREDENTIALS -> model.addAttribute("errorMessage",
                    "Invalid username/email or password.");
        }
        return "auth/login";
    }

    private String resolvePostLoginTarget(HttpSession session, SessionUser user) {
        Object saved = session.getAttribute(SessionConstants.REDIRECT_AFTER_LOGIN);
        session.removeAttribute(SessionConstants.REDIRECT_AFTER_LOGIN);
        if (saved instanceof String target && !target.isBlank() && !target.contains("/auth/")) {
            return target;
        }
        return user.homePath();
    }

    @PostMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout";
    }

    // ------------------------------------------------------ SCR-03 Register

    @GetMapping("/auth/register")
    public String registerForm(HttpSession session, Model model) {
        if (SessionUtil.isAuthenticated(session)) {
            return "redirect:" + SessionUtil.require(session).homePath();
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form, BindingResult binding) {
        // Field-format errors are already in `binding`; the service layers on the
        // uniqueness / match / complexity rules and adds any further errors.
        User created = authService.register(form, binding);
        if (created == null) {
            return "auth/register";
        }
        return "redirect:/auth/login?registered";
    }

    // ------------------------------------------ SCR-02 Password Reset request

    @GetMapping("/auth/forgot-password")
    public String forgotPasswordForm(HttpSession session, Model model) {
        if (SessionUtil.isAuthenticated(session)) {
            return "redirect:" + SessionUtil.require(session).homePath();
        }
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
                                 BindingResult binding, HttpServletRequest request, Model model) {
        if (binding.hasErrors()) {
            return "auth/forgot-password";
        }
        // Anti-enumeration: always report the same "email sent" outcome.
        passwordResetService.createToken(form.getEmail()).ifPresent(token -> {
            String base = request.getScheme() + "://" + request.getServerName()
                    + (isDefaultPort(request) ? "" : ":" + request.getServerPort());
            model.addAttribute("demoResetLink", base + "/auth/reset-password?token=" + token);
        });
        model.addAttribute("submitted", true);
        model.addAttribute("ttlMinutes", PasswordResetService.TOKEN_TTL_MINUTES);
        return "auth/forgot-password";
    }

    // --------------------------------------- SCR-02 Password Reset confirmation

    @GetMapping("/auth/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("resetPasswordForm", form);
        model.addAttribute("invalidToken", !passwordResetService.isValid(token));
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                                BindingResult binding, Model model) {
        if (!passwordResetService.isValid(form.getToken())) {
            model.addAttribute("invalidToken", true);
            return "auth/reset-password";
        }
        String complexity = authService.passwordComplexityError(form.getNewPassword());
        if (complexity != null && !binding.hasFieldErrors("newPassword")) {
            binding.rejectValue("newPassword", "password.weak", complexity);
        }
        if (!binding.hasFieldErrors("confirmPassword")
                && (form.getNewPassword() == null || !form.getNewPassword().equals(form.getConfirmPassword()))) {
            binding.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }
        if (binding.hasErrors()) {
            model.addAttribute("invalidToken", false);
            return "auth/reset-password";
        }
        passwordResetService.resetPassword(form.getToken(), form.getNewPassword());
        return "redirect:/auth/login?reset";
    }

    private static boolean isDefaultPort(HttpServletRequest request) {
        int port = request.getServerPort();
        return ("http".equals(request.getScheme()) && port == 80)
                || ("https".equals(request.getScheme()) && port == 443);
    }
}
