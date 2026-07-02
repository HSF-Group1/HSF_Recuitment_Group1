package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    long countByJobPosting(JobPosting jobPosting);
}
