package com.group1.recruitment.service;

import java.util.List;

import com.group1.recruitment.dto.response.ApplicationDetailResponse;
import org.springframework.stereotype.Service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.repository.ApplicationRepository;

@Service
public class ApplicationService {
    private ApplicationRepository applicationRepository;

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

}
