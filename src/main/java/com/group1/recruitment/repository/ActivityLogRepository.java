package com.group1.recruitment.repository;

import com.group1.recruitment.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByTimestampGreaterThanEqual(LocalDateTime dateTime);
}
