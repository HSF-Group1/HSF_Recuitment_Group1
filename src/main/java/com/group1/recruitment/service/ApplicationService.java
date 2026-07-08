package com.group1.recruitment.service;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;

    }

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public ApplicationDetailResponse getById(Long id) {
        Application application = applicationRepository.findById(id)
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

}
