package com.group1.recruitment.repository;

import com.group1.recruitment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRole_NameAndStatusOrderByFullNameAsc(String roleName, com.group1.recruitment.enums.AccountStatus status);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRole_Name(String roleName);

    long countByCreatedAtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);
}
