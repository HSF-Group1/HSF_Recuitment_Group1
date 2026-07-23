package com.group1.recruitment.controller;

import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DashboardControllerTest {

    @Autowired
    private DashboardController dashboardController;

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        SessionUser admin = new SessionUser(1L, "Admin HSF", "admin", "admin@hsf.com", "ADMIN");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, admin);
        return session;
    }

    private MockHttpSession createHrSession() {
        MockHttpSession session = new MockHttpSession();
        SessionUser hr = new SessionUser(2L, "HR HSF", "hr_manager", "hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, hr);
        return session;
    }

    @Test
    void testAdminDashboardRedirectForNonAdmin() {
        MockHttpSession session = createHrSession();
        ConcurrentModel model = new ConcurrentModel();
        String view = dashboardController.adminDashboard(session, model);
        assertEquals("redirect:/home", view);
    }

    @Test
    void testAdminDashboardSuccessForAdmin() {
        MockHttpSession session = createAdminSession();
        ConcurrentModel model = new ConcurrentModel();
        String view = dashboardController.adminDashboard(session, model);
        assertEquals("dashboard/admin", view);

        assertNotNull(model.getAttribute("hrManagersCount"));
        assertNotNull(model.getAttribute("totalUsers"));
        assertNotNull(model.getAttribute("totalJobs"));

        assertNotNull(model.getAttribute("activityHeights"));
        assertNotNull(model.getAttribute("activityCounts"));

        @SuppressWarnings("unchecked")
        Map<String, Integer> heights = (Map<String, Integer>) model.getAttribute("activityHeights");
        assertNotNull(heights);
        assertTrue(heights.containsKey("MONDAY"));
        assertTrue(heights.containsKey("SUNDAY"));
    }
}
