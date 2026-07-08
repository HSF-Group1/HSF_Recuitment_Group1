package com.group1.recruitment.service;

import com.group1.recruitment.dto.response.ApplicationDetailResponse;
import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationWorkflowService workflowService;

    public ApplicationService(ApplicationRepository applicationRepository, ApplicationWorkflowService workflowService) {
        this.applicationRepository = applicationRepository;
        this.workflowService = workflowService;
    }

    public ApplicationDetailResponse getById(Long id) {
        Application application = applicationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Application does not exists!"));

        ApplicationDetailResponse applicationDetail = new ApplicationDetailResponse();
        applicationDetail.setId(id);
        applicationDetail.setStatus(application.getStatus());
        applicationDetail.setSubmissionDate(application.getSubmissionDate());
        applicationDetail.setCvFileUrl(application.getCvFileUrl());
        applicationDetail.setJobPosting(application.getJobPosting());
        applicationDetail.setCandidate(application.getCandidate());
        return applicationDetail;
    }

    public List<Application> getAll() {
        List<Application> applications = applicationRepository.findAll();
        return applications;
    }

    public Application getOrThrow(Long id) {
        return applicationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Application not found: " + id));
    }

    /** Candidate counts per stage for pipeline charts (SCR-12 / SCR-20). */
    public Map<ApplicationStatus, Long> pipelineCounts(JobPosting job) {
        Map<ApplicationStatus, Long> counts = new LinkedHashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            counts.put(s, applicationRepository.countByJobPostingAndStatus(job, s));
        }
        return counts;
    }

    /** Updates the status of an application after validating the transition via WorkflowService. */
    public void updateApplicationStatus(Long id, ApplicationStatus targetStatus) {
        Application application = getOrThrow(id);
        if (!workflowService.isValidTransition(application.getStatus(), targetStatus)) {
            throw ValidationException.global("Invalid status transition from " + application.getStatus() + " to " + targetStatus);
        }
        application.setStatus(targetStatus);
        application.setStatusUpdatedAt(java.time.LocalDateTime.now());
        applicationRepository.save(application);
    }
}
