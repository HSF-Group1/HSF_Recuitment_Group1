package com.group1.recruitment.service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;

    public ReportService(ApplicationRepository applicationRepository, JobPostingRepository jobPostingRepository) {
        this.applicationRepository = applicationRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> getPipelineSummary(Long jobId) {
        Map<ApplicationStatus, Long> counts = new LinkedHashMap<>();

        if (jobId != null) {
            JobPosting job = jobPostingRepository.findById(jobId)
                    .orElseThrow(() -> new NotFoundException("Job posting not found with ID: " + jobId));
            for (ApplicationStatus status : ApplicationStatus.values()) {
                counts.put(status, applicationRepository.countByJobPostingAndStatus(job, status));
            }
        } else {
            for (ApplicationStatus status : ApplicationStatus.values()) {
                counts.put(status, applicationRepository.countByStatus(status));
            }
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public List<Application> getPipelineApplications(Long jobId) {
        if (jobId != null) {
            return applicationRepository.findByJobPostingIdWithDetails(jobId);
        } else {
            return applicationRepository.findAllWithDetails();
        }
    }

    public long calculateDaysInStage(Application app) {
        LocalDateTime baseDate = app.getStatusUpdatedAt() != null ? app.getStatusUpdatedAt() : app.getSubmissionDate();
        if (baseDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(baseDate.toLocalDate(), LocalDate.now());
        return Math.max(0, days);
    }

    public String getEvaluationRatingStr(Application app) {
        if (app.getInterviews() == null) {
            return "-";
        }
        return app.getInterviews().stream()
                .filter(i -> i.getStatus() == com.group1.recruitment.enums.InterviewStatus.EVALUATED && i.getEvaluation() != null)
                .map(i -> i.getEvaluation().getRating() + "/5")
                .findFirst()
                .orElse("-");
    }

    public String getInterviewerName(Application app) {
        if (app.getInterviews() == null) {
            return "-";
        }
        return app.getInterviews().stream()
                .filter(i -> i.getStatus() == com.group1.recruitment.enums.InterviewStatus.SCHEDULED || i.getStatus() == com.group1.recruitment.enums.InterviewStatus.EVALUATED)
                .map(i -> i.getInterviewer() != null ? i.getInterviewer().getFullName() : "N/A")
                .findFirst()
                .orElse("-");
    }
}
