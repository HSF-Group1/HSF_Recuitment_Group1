package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
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
                     "LEFT JOIN FETCH iv.evaluation " +
                     "WHERE a.id = :id")
       Optional<Application> findByIdWithDetails(@Param("id") Long id);

       Optional<Application> findById(Long id);

       @Query("SELECT DISTINCT a FROM Application a " +
                     "LEFT JOIN FETCH a.jobPosting jp " +
                     "LEFT JOIN FETCH jp.createdBy " +
                     "LEFT JOIN FETCH a.candidate c " +
                     "LEFT JOIN FETCH c.user " +
                     "LEFT JOIN FETCH c.profile " +
                     "LEFT JOIN FETCH a.interviews iv " +
                     "LEFT JOIN FETCH iv.interviewer " +
                     "LEFT JOIN FETCH iv.evaluation " +
                     "ORDER BY a.submissionDate DESC")
       List<Application> findAllWithDetails();

       @Query("SELECT DISTINCT a FROM Application a " +
                     "LEFT JOIN FETCH a.jobPosting jp " +
                     "LEFT JOIN FETCH jp.createdBy " +
                     "LEFT JOIN FETCH a.candidate c " +
                     "LEFT JOIN FETCH c.user " +
                     "LEFT JOIN FETCH c.profile " +
                     "LEFT JOIN FETCH a.interviews iv " +
                     "LEFT JOIN FETCH iv.interviewer " +
                     "LEFT JOIN FETCH iv.evaluation " +
                     "WHERE jp.id = :jobId " +
                     "ORDER BY a.submissionDate DESC")
       List<Application> findByJobPostingIdWithDetails(@Param("jobId") Long jobId);

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

       // Get all applications by submission date descending(for admin)
       List<Application> findAllByOrderBySubmissionDateDesc();

       // Get all applications by status and submission date descending(for admin)
       List<Application> findByStatusOrderBySubmissionDateDesc(ApplicationStatus status);

       // Get all applications belonging to jobs created by a given HR Manager and
       // ordered by submission date descending
       List<Application> findByJobPosting_CreatedByOrderBySubmissionDateDesc(User createdBy);

       // Get all applications belonging to jobs created by a given HR Manager and
       // ordered by status and submission date descending
       List<Application> findByJobPosting_CreatedByAndStatusOrderBySubmissionDateDesc(User createdBy,
                     ApplicationStatus status);

       // Count all applications in a given date range (for Admin weekly chart)
       @Query("SELECT COUNT(a) FROM Application a WHERE a.submissionDate >= :from AND a.submissionDate < :to")
       long countBySubmissionDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

       // Count HR-owned applications submitted in a date range (for HR weekly chart)
       @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.createdBy = :hr AND a.submissionDate >= :from AND a.submissionDate < :to")
       long countByHrAndSubmissionDateBetween(@Param("hr") User hr, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

       // Interviewer assigned applications
       @Query("SELECT DISTINCT a FROM Application a JOIN a.interviews iv WHERE iv.interviewer = :interviewer ORDER BY a.submissionDate DESC")
       List<Application> findByInterviewerOrderBySubmissionDateDesc(@Param("interviewer") User interviewer);

       @Query("SELECT DISTINCT a FROM Application a JOIN a.interviews iv WHERE iv.interviewer = :interviewer AND a.status = :status ORDER BY a.submissionDate DESC")
       List<Application> findByInterviewerAndStatusOrderBySubmissionDateDesc(@Param("interviewer") User interviewer, @Param("status") ApplicationStatus status);

       @Query("SELECT COUNT(DISTINCT a) FROM Application a JOIN a.interviews iv WHERE iv.interviewer = :interviewer AND a.status = :status")
       long countByInterviewerAndStatus(@Param("interviewer") User interviewer, @Param("status") ApplicationStatus status);
}

