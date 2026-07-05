package com.group1.recruitment.repository;

import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobStatus status);

    List<JobPosting> findAllByOrderByCreatedAtDesc();

    long countByStatus(JobStatus status);

    long countByCreatedByAndStatus(User createdBy, JobStatus status);
}