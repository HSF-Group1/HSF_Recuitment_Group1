package com.group1.recruitment.service;

import com.group1.recruitment.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ApplicationWorkflowServiceTest {

    private final ApplicationWorkflowService service = new ApplicationWorkflowService();

    @Test
    void testGetAllowedTransitions() {
        // APPLIED -> SCREENING, REJECTED
        List<ApplicationStatus> appliedTransitions = service.getAllowedTransitions(ApplicationStatus.APPLIED);
        assertEquals(2, appliedTransitions.size());
        assertTrue(appliedTransitions.contains(ApplicationStatus.SCREENING));
        assertTrue(appliedTransitions.contains(ApplicationStatus.REJECTED));

        // SCREENING -> INTERVIEW, REJECTED
        List<ApplicationStatus> screeningTransitions = service.getAllowedTransitions(ApplicationStatus.SCREENING);
        assertEquals(2, screeningTransitions.size());
        assertTrue(screeningTransitions.contains(ApplicationStatus.INTERVIEW));
        assertTrue(screeningTransitions.contains(ApplicationStatus.REJECTED));

        // INTERVIEW -> OFFER, REJECTED
        List<ApplicationStatus> interviewTransitions = service.getAllowedTransitions(ApplicationStatus.INTERVIEW);
        assertEquals(2, interviewTransitions.size());
        assertTrue(interviewTransitions.contains(ApplicationStatus.OFFER));
        assertTrue(interviewTransitions.contains(ApplicationStatus.REJECTED));

        // OFFER -> HIRED, REJECTED
        List<ApplicationStatus> offerTransitions = service.getAllowedTransitions(ApplicationStatus.OFFER);
        assertEquals(2, offerTransitions.size());
        assertTrue(offerTransitions.contains(ApplicationStatus.HIRED));
        assertTrue(offerTransitions.contains(ApplicationStatus.REJECTED));

        // Terminal states -> empty
        assertTrue(service.getAllowedTransitions(ApplicationStatus.HIRED).isEmpty());
        assertTrue(service.getAllowedTransitions(ApplicationStatus.REJECTED).isEmpty());
        assertTrue(service.getAllowedTransitions(ApplicationStatus.WITHDRAWN).isEmpty());
    }

    @Test
    void testIsValidTransition() {
        // Valid
        assertTrue(service.isValidTransition(ApplicationStatus.APPLIED, ApplicationStatus.SCREENING));
        assertTrue(service.isValidTransition(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED));
        assertTrue(service.isValidTransition(ApplicationStatus.SCREENING, ApplicationStatus.INTERVIEW));
        assertTrue(service.isValidTransition(ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER));
        assertTrue(service.isValidTransition(ApplicationStatus.OFFER, ApplicationStatus.HIRED));

        // Invalid
        assertFalse(service.isValidTransition(ApplicationStatus.APPLIED, ApplicationStatus.OFFER));
        assertFalse(service.isValidTransition(ApplicationStatus.APPLIED, ApplicationStatus.HIRED));
        assertFalse(service.isValidTransition(ApplicationStatus.HIRED, ApplicationStatus.SCREENING));
        assertFalse(service.isValidTransition(ApplicationStatus.REJECTED, ApplicationStatus.APPLIED));
        assertFalse(service.isValidTransition(null, ApplicationStatus.APPLIED));
        assertFalse(service.isValidTransition(ApplicationStatus.APPLIED, null));
    }
}
