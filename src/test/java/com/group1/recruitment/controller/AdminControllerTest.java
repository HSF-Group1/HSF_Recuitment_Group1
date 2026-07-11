package com.group1.recruitment.controller;

import com.group1.recruitment.dto.UserForm;
import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminControllerTest {

    @Autowired
    private AdminController adminController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        SessionUser admin = new SessionUser(1L, "Admin HSF", "admin", "admin@hsf.com", "ADMIN");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, admin);
        return session;
    }

    private MockHttpSession createHrSession() {
        MockHttpSession session = new MockHttpSession();
        SessionUser hr = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, hr);
        return session;
    }

    @Test
    void testListAccessDeniedForHr() {
        MockHttpSession session = createHrSession();
        ConcurrentModel model = new ConcurrentModel();
        assertThrows(AccessDeniedException.class, () -> {
            adminController.list(null, null, null, session, model);
        });
    }

    @Test
    void testListSuccessForAdmin() {
        MockHttpSession session = createAdminSession();
        ConcurrentModel model = new ConcurrentModel();
        String view = adminController.list(null, null, null, session, model);
        assertEquals("admin/users", view);
        assertNotNull(model.getAttribute("users"));
        assertNotNull(model.getAttribute("roles"));
        assertNotNull(model.getAttribute("tabCounts"));
    }

    @Test
    void testListWithSearchAndFilters() {
        MockHttpSession session = createAdminSession();
        ConcurrentModel model = new ConcurrentModel();

        // Let's create a specific user to search for
        Role interviewerRole = roleRepository.findByName("INTERVIEWER").orElseThrow();
        User testUser = new User();
        testUser.setFullName("Unique Test User");
        testUser.setUsername("uniquetestuser");
        testUser.setEmail("unique.test@hsf.com");
        testUser.setRole(interviewerRole);
        testUser.setStatus(AccountStatus.ACTIVE);
        testUser.setPasswordHash("hashedpassword");
        userRepository.save(testUser);

        // Search for "Unique"
        adminController.list("Unique", null, "ACTIVE", session, model);
        List<User> users = (List<User>) model.getAttribute("users");
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("uniquetestuser")));

        // Search for something non-existent
        ConcurrentModel modelEmpty = new ConcurrentModel();
        adminController.list("NonExistentXYZPattern", null, null, session, modelEmpty);
        List<User> usersEmpty = (List<User>) modelEmpty.getAttribute("users");
        assertTrue(usersEmpty.isEmpty());
    }

    @Test
    void testCreateUserSuccess() {
        MockHttpSession session = createAdminSession();
        Role candidateRole = roleRepository.findByName("CANDIDATE").orElseThrow();

        UserForm form = new UserForm();
        form.setFullName("New Candidate");
        form.setUsername("newcandidate");
        form.setEmail("new.candidate@hsf.com");
        form.setRoleId(candidateRole.getId());
        form.setStatus(AccountStatus.ACTIVE);
        form.setPassword("Password123@");
        form.setConfirmPassword("Password123@");

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        ConcurrentModel model = new ConcurrentModel();

        String view = adminController.create(form, session, ra, model);
        assertEquals("redirect:/admin/users", view);

        // Verify user exists in database
        Optional<User> savedUserOpt = userRepository.findByUsername("newcandidate");
        assertTrue(savedUserOpt.isPresent());
        User savedUser = savedUserOpt.get();
        assertEquals("New Candidate", savedUser.getFullName());
        assertEquals("new.candidate@hsf.com", savedUser.getEmail());

        // Verify matching Candidate profile was created
        Optional<Candidate> candidateOpt = candidateRepository.findByUser(savedUser);
        assertTrue(candidateOpt.isPresent());
    }

    @Test
    void testCreateUserValidationFails() {
        MockHttpSession session = createAdminSession();
        Role candidateRole = roleRepository.findByName("CANDIDATE").orElseThrow();

        UserForm form = new UserForm();
        form.setFullName(""); // Blank full name
        form.setUsername("newcandidate2");
        form.setEmail("invalid-email"); // Invalid email format
        form.setRoleId(candidateRole.getId());
        form.setStatus(AccountStatus.ACTIVE);
        form.setPassword("weak"); // Weak password
        form.setConfirmPassword("mismatch"); // Password mismatch

        ConcurrentModel model = new ConcurrentModel();
        String view = adminController.create(form, session, new RedirectAttributesModelMap(), model);
        assertEquals("admin/users", view);

        Map<String, String> errors = (Map<String, String>) model.getAttribute("errors");
        assertNotNull(errors);
        assertTrue(errors.containsKey("fullName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("confirmPassword"));
    }

    @Test
    void testUpdateUserSuccess() {
        MockHttpSession session = createAdminSession();
        Role hrRole = roleRepository.findByName("HR_MANAGER").orElseThrow();

        // Create a user to update
        User user = new User();
        user.setFullName("Old Name");
        user.setUsername("oldusername");
        user.setEmail("old.email@hsf.com");
        user.setRole(hrRole);
        user.setStatus(AccountStatus.ACTIVE);
        user.setPasswordHash("oldhash");
        User saved = userRepository.save(user);

        UserForm form = UserForm.from(saved);
        form.setFullName("Updated Name");
        form.setEmail("updated.email@hsf.com");
        // leave password blank to not change password

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        ConcurrentModel model = new ConcurrentModel();

        String view = adminController.update(saved.getId(), form, session, ra, model);
        assertEquals("redirect:/admin/users", view);

        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Updated Name", updated.getFullName());
        assertEquals("updated.email@hsf.com", updated.getEmail());
        assertEquals("oldhash", updated.getPasswordHash()); // password unchanged
    }

    @Test
    void testToggleStatus() {
        MockHttpSession session = createAdminSession();

        User user = new User();
        user.setFullName("Toggle Test");
        user.setUsername("toggletest");
        user.setEmail("toggle.test@hsf.com");
        user.setRole(roleRepository.findByName("INTERVIEWER").orElseThrow());
        user.setStatus(AccountStatus.ACTIVE);
        user.setPasswordHash("hash");
        User saved = userRepository.save(user);

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        // Toggle to Locked
        String view1 = adminController.toggleStatus(saved.getId(), session, ra);
        assertEquals("redirect:/admin/users", view1);
        assertEquals(AccountStatus.LOCKED, userRepository.findById(saved.getId()).orElseThrow().getStatus());

        // Toggle back to Active
        String view2 = adminController.toggleStatus(saved.getId(), session, ra);
        assertEquals("redirect:/admin/users", view2);
        assertEquals(AccountStatus.ACTIVE, userRepository.findById(saved.getId()).orElseThrow().getStatus());
    }

    @Test
    void testActivityLogAccessDeniedForHr() {
        MockHttpSession session = createHrSession();
        ConcurrentModel model = new ConcurrentModel();
        assertThrows(AccessDeniedException.class, () -> {
            adminController.activityLogs(0, 10, session, model);
        });
    }

    @Test
    void testActivityLogsSuccessForAdmin() {
        MockHttpSession session = createAdminSession();
        ConcurrentModel model = new ConcurrentModel();
        String view = adminController.activityLogs(0, 10, session, model);
        assertEquals("admin/activity-log", view);
        assertNotNull(model.getAttribute("logsPage"));
        assertNotNull(model.getAttribute("currentPage"));
        assertNotNull(model.getAttribute("totalPages"));
        assertNotNull(model.getAttribute("totalItems"));
        assertEquals("activity-log", model.getAttribute("activeMenu"));
    }
}
