package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InterviewControllerTest {

    @Autowired
    private InterviewController interviewController;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void testGetAssignPage() {
        MockHttpSession session = new MockHttpSession();
        SessionUser sessionUser = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, sessionUser);

        ConcurrentModel model = new ConcurrentModel();
        String view = interviewController.assignForm(2L, session, model);
        assertEquals("interview/assign", view);
        assertNotNull(model.getAttribute("app"));
        assertNotNull(model.getAttribute("interviewers"));
    }

    @Test
    void testScheduleInterviewPostSuccess() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        MockHttpSession session = new MockHttpSession();
        SessionUser sessionUser = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, sessionUser);

        ConcurrentModel model = new ConcurrentModel();
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        String view = interviewController.schedule(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Prepare CV", session, model, null, response);
        assertEquals("redirect:/application/2", view);
    }

    @Test
    void testScheduleInterviewPostValidationFail() {
        MockHttpSession session = new MockHttpSession();
        SessionUser sessionUser = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, sessionUser);

        ConcurrentModel model = new ConcurrentModel();
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        // interviewerId is null to trigger validation error
        String view = interviewController.schedule(2L, null, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "", session, model, null, response);
        assertEquals("interview/assign", view);
        assertNotNull(model.getAttribute("errorMsg"));
    }
}
