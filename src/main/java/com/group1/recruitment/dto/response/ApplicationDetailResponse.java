package com.group1.recruitment.dto.response;

import java.time.LocalDateTime;

import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.enums.ApplicationStatus;

public class ApplicationDetailResponse {
    private Long id;

    private LocalDateTime submissionDate;

    private ApplicationStatus status;

    private JobPosting jobPosting;

    private Candidate candidate;

    private String cvFileUrl;

    public ApplicationDetailResponse(String cvFileUrl, JobPosting jobPosting, ApplicationStatus status, LocalDateTime submissionDate, Long id, Candidate candidate) {
        this.cvFileUrl = cvFileUrl;
        this.jobPosting = jobPosting;
        this.status = status;
        this.submissionDate = submissionDate;
        this.id = id;
        this.candidate = candidate;
    }

    public ApplicationDetailResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public JobPosting getJobPosting() {
        return jobPosting;
    }

    public void setJobPosting(JobPosting jobPosting) {
        this.jobPosting = jobPosting;
    }

    public String getCvFileUrl() {
        return cvFileUrl;
    }

    public void setCvFileUrl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }
}
