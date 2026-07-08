package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.jobPosting jp " +
           "LEFT JOIN FETCH jp.createdBy " +
           "LEFT JOIN FETCH a.candidate c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.profile " +
           "LEFT JOIN FETCH a.interviews iv " +
           "LEFT JOIN FETCH iv.interviewer " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);

    Optional<Application> findById(Long id);

    List<Application> findByJobPostingOrderBySubmissionDateDesc(JobPosting jobPosting);

    List<Application> findByJobPostingAndStatusOrderBySubmissionDateDesc(JobPosting jobPosting,
            ApplicationStatus status);

    List<Application> findByCandidateOrderBySubmissionDateDesc(Candidate candidate);

    Optional<Application> findByCandidateAndJobPosting(Candidate candidate, JobPosting jobPosting);

    long countByJobPosting(JobPosting jobPosting);

    long countByJobPostingAndStatus(JobPosting jobPosting, ApplicationStatus status);

    long countByStatus(ApplicationStatus status);

    // Applications belonging to jobs created by a given HR Manager
    long countByJobPosting_CreatedByAndStatus(User createdBy, ApplicationStatus status);
}