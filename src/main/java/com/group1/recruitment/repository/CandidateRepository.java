package com.group1.recruitment.repository;

import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByUser(User user);
}
