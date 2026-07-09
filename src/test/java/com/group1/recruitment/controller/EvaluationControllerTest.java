package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EvaluationControllerTest {

    @Autowired
    private EvaluationController evaluationController;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void testGetEvaluatePageSuccess() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        MockHttpSession session = new MockHttpSession();
        SessionUser interviewer = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, interviewer);

        ConcurrentModel model = new ConcurrentModel();
        String view = evaluationController.evaluateForm(interview.getId(), session, model);
        assertEquals("interview/evaluate", view);
        assertNotNull(model.getAttribute("interview"));
        assertNotNull(model.getAttribute("app"));
    }

    @Test
    void testGetEvaluatePageAccessDenied() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        MockHttpSession session = new MockHttpSession();
        // interviewer2 (ID 5) is NOT assigned to this interview
        SessionUser interviewer = new SessionUser(5L, "Le Thi Tu", "interviewer2", "tu.le@hsf.com", "INTERVIEWER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, interviewer);

        ConcurrentModel model = new ConcurrentModel();
        assertThrows(com.group1.recruitment.exception.AccessDeniedException.class, () -> {
            evaluationController.evaluateForm(interview.getId(), session, model);
        });
    }

    @Test
    void testSubmitEvaluationSuccess() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        MockHttpSession session = new MockHttpSession();
        SessionUser interviewer = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, interviewer);

        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletRequest request = new MockHttpServletRequest();

        String view = evaluationController.submitEvaluation(interview.getId(), 5, "Excellent candidate", session, model, request, null);
        assertEquals("redirect:/application/2", view);
    }

    @Test
    void testSubmitEvaluationValidationFail() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        MockHttpSession session = new MockHttpSession();
        SessionUser interviewer = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, interviewer);

        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletRequest request = new MockHttpServletRequest();

        // Rating 6 is invalid
        String view = evaluationController.submitEvaluation(interview.getId(), 6, "Excellent candidate", session, model, request, "true");
        assertEquals("interview/fragments/_evaluation_form", view);
        assertNotNull(model.getAttribute("errorMsg"));
    }
}
