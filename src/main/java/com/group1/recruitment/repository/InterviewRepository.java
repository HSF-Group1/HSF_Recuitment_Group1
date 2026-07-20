package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplication(com.group1.recruitment.entity.Application application);

    List<Interview> findByInterviewer(User interviewer);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.application.jobPosting.createdBy = :hr AND i.interviewDate = :date")
    long countByHrAndDate(@Param("hr") User hr, @Param("date") LocalDate date);
}
