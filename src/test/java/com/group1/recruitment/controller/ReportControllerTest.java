package com.group1.recruitment.controller;

import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportControllerTest {

    @Autowired
    private ReportController reportController;

    @Test
    void testPipelineReportSuccessHr() {
        MockHttpSession session = new MockHttpSession();
        SessionUser hr = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, hr);

        ConcurrentModel model = new ConcurrentModel();
        String view = reportController.pipelineReport(null, session, null, model);
        assertEquals("report/pipeline", view);
        assertNotNull(model.getAttribute("jobs"));
        assertNotNull(model.getAttribute("summary"));
        assertNotNull(model.getAttribute("applications"));
    }

    @Test
    void testPipelineReportSuccessAdmin() {
        MockHttpSession session = new MockHttpSession();
        SessionUser admin = new SessionUser(1L, "Admin HSF", "admin", "admin@hsf.com", "ADMIN");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, admin);

        ConcurrentModel model = new ConcurrentModel();
        String view = reportController.pipelineReport(1L, session, "true", model);
        assertEquals("report/fragments/_pipeline_data", view);
    }

    @Test
    void testPipelineReportFailInterviewer() {
        MockHttpSession session = new MockHttpSession();
        SessionUser interviewer = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, interviewer);

        ConcurrentModel model = new ConcurrentModel();
        assertThrows(AccessDeniedException.class, () -> {
            reportController.pipelineReport(null, session, null, model);
        });
    }
}
