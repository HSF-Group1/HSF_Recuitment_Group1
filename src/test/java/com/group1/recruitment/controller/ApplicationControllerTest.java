package com.group1.recruitment.controller;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.security.SessionConstants;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.context.WebApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.web.IWebExchange;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationControllerTest {

    @Autowired
    private ApplicationController applicationController;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private WebApplicationContext wac;

    @Test
    void testGetApplicationDetailAsHr() {
        MockHttpSession session = new MockHttpSession();
        SessionUser sessionUser = new SessionUser(2L, "Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", "HR_MANAGER");
        session.setAttribute(SessionConstants.LOGGED_IN_USER, sessionUser);

        ConcurrentModel model = new ConcurrentModel();
        String view = applicationController.detail(5L, session, model);
        assertEquals("application/detail", view);
        assertNotNull(model.getAttribute("currentApplication"));
    }

    @Test
    void testTemplateProcessing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        jakarta.servlet.ServletContext servletContext = wac.getServletContext();

        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
        IWebExchange webExchange = webApplication.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);

        Application application = applicationService.getOrThrow(5L);
        SessionUser sessionUser = new SessionUser(2L, "Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", "HR_MANAGER");
        context.setVariable("currentUser", sessionUser);
        context.setVariable("currentApplication", application);
        context.setVariable("currentUserRole", "HR_MANAGER");
        context.setVariable("applicationStatus", application.getStatus());
        context.setVariable("allowedTransitions", java.util.List.of(com.group1.recruitment.enums.ApplicationStatus.OFFER, com.group1.recruitment.enums.ApplicationStatus.REJECTED));
        context.setVariable("canDownloadCv", true);
        
        String html = templateEngine.process("application/detail", context);
        assertNotNull(html);
        assertTrue(html.contains("Candidate Detail"));
    }
}
