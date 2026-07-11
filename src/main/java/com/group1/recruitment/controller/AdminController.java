package com.group1.recruitment.controller;

import com.group1.recruitment.dto.UserForm;
import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.AuthService;
import com.group1.recruitment.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordUtil passwordUtil;
    private final AuthService authService;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository,
                           CandidateRepository candidateRepository, PasswordUtil passwordUtil,
                           AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.candidateRepository = candidateRepository;
        this.passwordUtil = passwordUtil;
        this.authService = authService;
    }

    private void checkAdminAccess(HttpSession session) {
        SessionUser user = SessionUtil.require(session);
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Access denied: ADMIN role required.");
        }
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {
        checkAdminAccess(session);

        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        if (!model.containsAttribute("editMode")) {
            model.addAttribute("editMode", false);
        }

        populateListModel(q, roleId, status, model);
        return "admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {
        checkAdminAccess(session);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", UserForm.from(user));
        }
        model.addAttribute("editMode", true);

        populateListModel(q, roleId, status, model);
        return "admin/users";
    }

    @PostMapping
    public String create(
            @ModelAttribute UserForm userForm,
            HttpSession session,
            RedirectAttributes ra,
            Model model) {
        checkAdminAccess(session);

        try {
            validateCreate(userForm);

            User user = new User();
            user.setFullName(userForm.getFullName().trim());
            user.setUsername(userForm.getUsername().trim());
            user.setEmail(userForm.getEmail().trim().toLowerCase());

            Role role = roleRepository.findById(userForm.getRoleId())
                    .orElseThrow(() -> ValidationException.of("roleId", "Invalid role selection."));
            user.setRole(role);
            user.setStatus(userForm.getStatus() != null ? userForm.getStatus() : AccountStatus.ACTIVE);
            user.setPasswordHash(passwordUtil.hash(userForm.getPassword()));
            user.setCreatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);

            if ("CANDIDATE".equals(role.getName())) {
                if (candidateRepository.findByUser(saved).isEmpty()) {
                    Candidate candidate = new Candidate();
                    candidate.setUser(saved);
                    candidateRepository.save(candidate);
                }
            }

            ra.addFlashAttribute("flash", "User " + saved.getUsername() + " created successfully.");
            return "redirect:/admin/users";
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("userForm", userForm);
            model.addAttribute("editMode", false);
            populateListModel(null, null, null, model);
            return "admin/users";
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute UserForm userForm,
            HttpSession session,
            RedirectAttributes ra,
            Model model) {
        checkAdminAccess(session);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        try {
            validateUpdate(id, userForm);

            user.setFullName(userForm.getFullName().trim());
            user.setEmail(userForm.getEmail().trim().toLowerCase());

            Role role = roleRepository.findById(userForm.getRoleId())
                    .orElseThrow(() -> ValidationException.of("roleId", "Invalid role selection."));
            user.setRole(role);
            user.setStatus(userForm.getStatus());

            if (!isBlank(userForm.getPassword())) {
                user.setPasswordHash(passwordUtil.hash(userForm.getPassword()));
            }

            User saved = userRepository.save(user);

            if ("CANDIDATE".equals(role.getName())) {
                if (candidateRepository.findByUser(saved).isEmpty()) {
                    Candidate candidate = new Candidate();
                    candidate.setUser(saved);
                    candidateRepository.save(candidate);
                }
            }

            ra.addFlashAttribute("flash", "User " + saved.getUsername() + " updated successfully.");
            return "redirect:/admin/users";
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("userForm", userForm);
            model.addAttribute("editMode", true);
            populateListModel(null, null, null, model);
            return "admin/users";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes ra) {
        checkAdminAccess(session);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (user.getStatus() == AccountStatus.ACTIVE) {
            user.setStatus(AccountStatus.LOCKED);
            ra.addFlashAttribute("flash", "User " + user.getUsername() + " has been locked.");
        } else {
            user.setStatus(AccountStatus.ACTIVE);
            ra.addFlashAttribute("flash", "User " + user.getUsername() + " has been activated.");
        }
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    private void populateListModel(String q, Long roleId, String status, Model model) {
        List<User> all = userRepository.findAll();
        // Newest first
        all.sort((u1, u2) -> u2.getId().compareTo(u1.getId()));

        String term = q == null ? null : q.trim().toLowerCase();
        AccountStatus activeStatus = parseStatus(status);

        List<User> filtered = all.stream()
                .filter(u -> activeStatus == null || u.getStatus() == activeStatus)
                .filter(u -> roleId == null || (u.getRole() != null && u.getRole().getId().equals(roleId)))
                .filter(u -> term == null || term.isEmpty()
                        || (u.getFullName() != null && u.getFullName().toLowerCase().contains(term))
                        || (u.getUsername() != null && u.getUsername().toLowerCase().contains(term))
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(term)))
                .toList();

        Map<String, Long> tabCounts = new LinkedHashMap<>();
        tabCounts.put("ALL", (long) all.size());
        tabCounts.put("ACTIVE", all.stream().filter(u -> u.getStatus() == AccountStatus.ACTIVE).count());
        tabCounts.put("LOCKED", all.stream().filter(u -> u.getStatus() == AccountStatus.LOCKED).count());
        tabCounts.put("INACTIVE", all.stream().filter(u -> u.getStatus() == AccountStatus.INACTIVE).count());

        model.addAttribute("users", filtered);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("activeTab", status == null || status.isBlank() ? "ALL" : status.toUpperCase());
        model.addAttribute("q", q);
        model.addAttribute("roleId", roleId);
        model.addAttribute("tabCounts", tabCounts);
    }

    private void validateCreate(UserForm form) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(form.getFullName())) {
            errors.put("fullName", "Full name is required.");
        }

        if (isBlank(form.getUsername())) {
            errors.put("username", "Username is required.");
        } else {
            String username = form.getUsername().trim();
            if (username.length() < 3 || username.length() > 50) {
                errors.put("username", "Username must be between 3 and 50 characters.");
            } else if (userRepository.existsByUsername(username)) {
                errors.put("username", "Username is already taken.");
            }
        }

        if (isBlank(form.getEmail())) {
            errors.put("email", "Email is required.");
        } else {
            String email = form.getEmail().trim().toLowerCase();
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                errors.put("email", "Invalid email format.");
            } else if (userRepository.existsByEmail(email)) {
                errors.put("email", "Email is already taken.");
            }
        }

        if (form.getRoleId() == null) {
            errors.put("roleId", "Role selection is required.");
        }

        if (form.getStatus() == null) {
            errors.put("status", "Status is required.");
        }

        if (isBlank(form.getPassword())) {
            errors.put("password", "Password is required.");
        } else {
            String complexity = authService.passwordComplexityError(form.getPassword());
            if (complexity != null) {
                errors.put("password", complexity);
            }
        }

        if (!equalsSafe(form.getPassword(), form.getConfirmPassword())) {
            errors.put("confirmPassword", "Passwords do not match.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateUpdate(Long id, UserForm form) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(form.getFullName())) {
            errors.put("fullName", "Full name is required.");
        }

        if (isBlank(form.getEmail())) {
            errors.put("email", "Email is required.");
        } else {
            String email = form.getEmail().trim().toLowerCase();
            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                errors.put("email", "Invalid email format.");
            } else {
                userRepository.findByEmail(email).ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        errors.put("email", "Email is already taken.");
                    }
                });
            }
        }

        if (form.getRoleId() == null) {
            errors.put("roleId", "Role selection is required.");
        }

        if (form.getStatus() == null) {
            errors.put("status", "Status is required.");
        }

        if (!isBlank(form.getPassword())) {
            String complexity = authService.passwordComplexityError(form.getPassword());
            if (complexity != null) {
                errors.put("password", complexity);
            }
            if (!equalsSafe(form.getPassword(), form.getConfirmPassword())) {
                errors.put("confirmPassword", "Passwords do not match.");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private AccountStatus parseStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean equalsSafe(String a, String b) {
        return a != null && a.equals(b);
    }
}
