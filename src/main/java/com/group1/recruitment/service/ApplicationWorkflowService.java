package com.group1.recruitment.service;

import com.group1.recruitment.enums.ApplicationStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ApplicationWorkflowService {

    /**
     * Get the allowed target states from a given status for HR/Admin users.
     */
    public List<ApplicationStatus> getAllowedTransitions(ApplicationStatus currentStatus) {
        if (currentStatus == null) {
            return Collections.emptyList();
        }
        return switch (currentStatus) {
            case APPLIED -> List.of(ApplicationStatus.SCREENING, ApplicationStatus.REJECTED);
            case SCREENING -> List.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED);
            case INTERVIEW -> List.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED);
            case OFFER -> List.of(ApplicationStatus.HIRED, ApplicationStatus.REJECTED);
            default -> Collections.emptyList();
        };
    }

    /**
     * Checks if a transition from one status to another is valid.
     */
    public boolean isValidTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return getAllowedTransitions(from).contains(to);
    }
}
