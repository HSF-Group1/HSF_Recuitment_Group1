package com.group1.recruitment.service;

import com.group1.recruitment.dto.JobForm;
import com.group1.recruitment.entity.Company;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.repository.CompanyRepository;
import com.group1.recruitment.repository.JobPostingRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private final JobPostingRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobPostingRepository jobRepository, ApplicationRepository applicationRepository,
            CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
    }

    public JobPosting getOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job posting not found: " + id));
    }

    /** SCR-10: HR sees only own postings; Admin sees all. */
    public List<JobPosting> listForManager(SessionUser user, User managed) {
        if (user.isAdmin()) {
            return jobRepository.findAllByOrderByCreatedAtDesc();
        }
        return jobRepository.findByCreatedByOrderByCreatedAtDesc(managed);
    }

    public long applicationCount(JobPosting job) {
        return applicationRepository.countByJobPosting(job);
    }

    /** HR may only manage their own postings; Admin may manage all. */
    public boolean canManage(JobPosting job, SessionUser user) {
        if (user.isAdmin())
            return true;
        return user.isHr() && job.getCreatedBy() != null && user.getId().equals(job.getCreatedBy().getId());
    }

    public void assertCanManage(JobPosting job, SessionUser user) {
        if (!canManage(job, user)) {
            throw new AccessDeniedException("You cannot manage this job posting.");
        }
    }

    @Transactional
    public JobPosting create(JobForm form, User creator) {
        validate(form);
        JobPosting job = new JobPosting();
        apply(job, form);
        job.setStatus(JobStatus.DRAFT);
        job.setCreatedBy(creator);
        job.setCompany(defaultCompany());
        job.setCreatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Transactional
    public JobPosting update(Long id, JobForm form, SessionUser user) {
        JobPosting job = getOrThrow(id);
        assertCanManage(job, user);
        validate(form);
        apply(job, form);
        return jobRepository.save(job);
    }

    @Transactional
    public void publish(Long id, SessionUser user) {
        JobPosting job = getOrThrow(id);
        assertCanManage(job, user);
        if (job.getStatus() != JobStatus.DRAFT && job.getStatus() != JobStatus.CLOSED) {
            throw ValidationException.global("Only draft or closed postings can be published.");
        }
        job.setStatus(JobStatus.ACTIVE);
        jobRepository.save(job);
    }

    @Transactional
    public void close(Long id, SessionUser user) {
        JobPosting job = getOrThrow(id);
        assertCanManage(job, user);
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw ValidationException.global("Only active postings can be closed.");
        }
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    @Transactional
    public void delete(Long id, SessionUser user) {
        JobPosting job = getOrThrow(id);
        assertCanManage(job, user);
        if (job.getStatus() != JobStatus.DRAFT || applicationRepository.countByJobPosting(job) > 0) {
            throw ValidationException.global("Only draft postings with no applications can be deleted.");
        }
        jobRepository.delete(job);
    }

    private void validate(JobForm form) {
        java.util.Map<String, String> errors = new java.util.LinkedHashMap<>();
        if (isBlank(form.getTitle()))
            errors.put("title", "Job title is required.");
        else if (form.getTitle().length() > 200)
            errors.put("title", "Max 200 characters.");
        if (isBlank(form.getDepartment()))
            errors.put("department", "Department is required.");
        if (isBlank(form.getLocation()))
            errors.put("location", "Location is required.");
        if (isBlank(form.getDescription()))
            errors.put("description", "Job description is required.");
        if (form.getApplicationDeadline() != null && form.getApplicationDeadline().isBefore(LocalDate.now())) {
            errors.put("applicationDeadline", "Application deadline must be a future date.");
        }
        if (!errors.isEmpty())
            throw new ValidationException(errors);
    }

    private void apply(JobPosting job, JobForm form) {
        job.setTitle(trim(form.getTitle()));
        job.setDepartment(trim(form.getDepartment()));
        job.setLocation(trim(form.getLocation()));
        job.setDescription(form.getDescription());
        job.setRequirements(form.getRequirements());
        job.setSalaryRange(trim(form.getSalaryRange()));
        job.setApplicationDeadline(form.getApplicationDeadline());
    }

    private Company defaultCompany() {
        return companyRepository.findAll().stream().findFirst().orElseGet(() -> {
            Company c = new Company();
            c.setName("HSF Technology JSC");
            c.setIndustry("Information Technology");
            c.setCreatedAt(LocalDateTime.now());
            return companyRepository.save(c);
        });
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}