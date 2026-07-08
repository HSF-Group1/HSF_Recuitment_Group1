package com.group1.recruitment.service;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;

    }

    public Application getOrThrow(Long id) {
        return applicationRepository.findById(id)
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

    @Transactional
    public Application applyToJob(Candidate candidate, JobPosting job, String cvFileUrl) {
        applicationRepository.findByCandidateAndJobPosting(candidate, job).ifPresent(existing -> {
            throw ValidationException.global("You have already applied for this job.");
        });

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJobPosting(job);
        application.setSubmissionDate(LocalDateTime.now());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setCvFileUrl(cvFileUrl);

        return applicationRepository.save(application);
    }

    public List<Application> listForCandidate(Candidate candidate){
        return applicationRepository.findByCandidateOrderBySubmissionDateDesc(candidate);
    }

    @Transactional
    public void withdraw(Long applicationId, Candidate candidate) {
        Application application = getOrThrow(applicationId);

        if (application.getCandidate() == null
                || !application.getCandidate().getId().equals(candidate.getId())) {
            throw ValidationException.global("You cannot withdraw this application.");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED
                && application.getStatus() != ApplicationStatus.SCREENING) {
            throw ValidationException.global("Only applied or screening applications can be withdrawn.");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }
    
}
