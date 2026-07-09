package com.group1.recruitment.service;

import com.group1.recruitment.dto.response.ApplicationDetailResponse;
import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.*;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationWorkflowService workflowService;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository, 
                              ApplicationWorkflowService workflowService,
                              ActivityLogRepository activityLogRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.workflowService = workflowService;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
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

    /** Updates the status of an application after validating the transition via WorkflowService and logging the activity. */
    @Transactional
    public void updateApplicationStatus(Long id, ApplicationStatus targetStatus, SessionUser actor) {
        Application application = getOrThrow(id);
        if (!workflowService.isValidTransition(application.getStatus(), targetStatus)) {
            throw ValidationException.global("Invalid status transition from " + application.getStatus() + " to " + targetStatus);
        }

        // Set or clear rejectedAtStage
        if (targetStatus == ApplicationStatus.REJECTED) {
            application.setRejectedAtStage(application.getStatus());
        } else {
            application.setRejectedAtStage(null);
        }

        // If leaving the INTERVIEW stage, auto-cancel any SCHEDULED interviews
        if (application.getStatus() == ApplicationStatus.INTERVIEW && targetStatus != ApplicationStatus.INTERVIEW) {
            if (application.getInterviews() != null) {
                for (Interview interview : application.getInterviews()) {
                    if (interview.getStatus() == InterviewStatus.SCHEDULED) {
                        interview.setStatus(InterviewStatus.CANCELLED);
                    }
                }
            }
        }

        application.setStatus(targetStatus);
        application.setStatusUpdatedAt(java.time.LocalDateTime.now());
        applicationRepository.save(application);

        if (actor != null) {
            User actorEntity = userRepository.findById(actor.getId()).orElse(null);
            if (actorEntity != null) {
                ActivityLog log = new ActivityLog();
                log.setUser(actorEntity);
                log.setEventType(EventType.APPLICATION_STATUS_CHANGED);
                log.setDescription("Application #" + application.getId() + " moved to " + targetStatus);
                log.setIpAddress("127.0.0.1");
                log.setTimestamp(java.time.LocalDateTime.now());
                activityLogRepository.save(log);
            }
        }
    }
}
