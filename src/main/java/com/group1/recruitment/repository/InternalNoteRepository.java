package com.group1.recruitment.repository;

import com.group1.recruitment.entity.InternalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {

    @Query("SELECT n FROM InternalNote n " +
           "LEFT JOIN FETCH n.author " +
           "WHERE n.application.id = :applicationId " +
           "ORDER BY n.createdAt DESC")
    List<InternalNote> findByApplicationIdOrderByCreatedAtDesc(@Param("applicationId") Long applicationId);
}
