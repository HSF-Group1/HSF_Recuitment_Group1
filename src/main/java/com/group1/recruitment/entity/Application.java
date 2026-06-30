package com.group1.recruitment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.group1.recruitment.enums.ApplicationStatus;

@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status;

    @Column(name = "cv_file_url")
    private String cvFileUrl;

    @OneToMany(mappedBy = "application")
    private List<Interview> interviews;

    @OneToMany(mappedBy = "application")
    private List<InternalNote> internalNotes;

    // No-args constructor
    public Application() {
    }

    // All-args constructor
    public Application(Long id, Candidate candidate, JobPosting jobPosting, LocalDateTime submissionDate, ApplicationStatus status, String cvFileUrl, List<Interview> interviews, List<InternalNote> internalNotes) {
        this.id = id;
        this.candidate = candidate;
        this.jobPosting = jobPosting;
        this.submissionDate = submissionDate;
        this.status = status;
        this.cvFileUrl = cvFileUrl;
        this.interviews = interviews;
        this.internalNotes = internalNotes;
    }

    // Getters and Setters
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

    public JobPosting getJobPosting() {
        return jobPosting;
    }

    public void setJobPosting(JobPosting jobPosting) {
        this.jobPosting = jobPosting;
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

    public String getCvFileUrl() {
        return cvFileUrl;
    }

    public void setCvFileUrl(String cvFileUrl) {
        this.cvFileUrl = cvFileUrl;
    }

    public List<Interview> getInterviews() {
        return interviews;
    }

    public void setInterviews(List<Interview> interviews) {
        this.interviews = interviews;
    }

    public List<InternalNote> getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(List<InternalNote> internalNotes) {
        this.internalNotes = internalNotes;
    }
}
