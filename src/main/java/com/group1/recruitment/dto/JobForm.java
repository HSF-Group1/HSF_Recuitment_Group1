package com.group1.recruitment.dto;

import com.group1.recruitment.entity.JobPosting;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class JobForm {

    private Long id;
    private String title;
    private String department;
    private String location;
    private String description;
    private String requirements;
    private String salaryRange;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applicationDeadline;

    public JobForm() {
    }

    public static JobForm from(JobPosting job) {
        JobForm f = new JobForm();
        f.id = job.getId();
        f.title = job.getTitle();
        f.department = job.getDepartment();
        f.location = job.getLocation();
        f.description = job.getDescription();
        f.requirements = job.getRequirements();
        f.salaryRange = job.getSalaryRange();
        f.applicationDeadline = job.getApplicationDeadline();
        return f;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
}