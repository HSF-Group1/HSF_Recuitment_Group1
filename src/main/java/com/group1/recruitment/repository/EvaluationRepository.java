package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Evaluation;
import com.group1.recruitment.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByInterview(Interview interview);
    boolean existsByInterviewId(Long interviewId);
}
