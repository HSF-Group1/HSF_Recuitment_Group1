package com.group1.recruitment.service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Test
    void testGetPipelineSummaryAll() {
        Map<ApplicationStatus, Long> summary = reportService.getPipelineSummary(null);
        assertNotNull(summary);
        assertTrue(summary.containsKey(ApplicationStatus.APPLIED));
        assertTrue(summary.containsKey(ApplicationStatus.SCREENING));
        assertTrue(summary.containsKey(ApplicationStatus.INTERVIEW));
    }

    @Test
    void testGetPipelineSummaryForJob() {
        // Job posting 1 has seeded applications
        Map<ApplicationStatus, Long> summary = reportService.getPipelineSummary(1L);
        assertNotNull(summary);
        assertTrue(summary.containsKey(ApplicationStatus.SCREENING));
    }

    @Test
    void testGetPipelineApplicationsAll() {
        List<Application> applications = reportService.getPipelineApplications(null);
        assertNotNull(applications);
        assertFalse(applications.isEmpty());
    }

    @Test
    void testGetPipelineApplicationsForJob() {
        List<Application> applications = reportService.getPipelineApplications(1L);
        assertNotNull(applications);
        assertFalse(applications.isEmpty());
        // Verify all belong to job posting 1
        assertTrue(applications.stream().allMatch(a -> a.getJobPosting().getId().equals(1L)));
    }

    @Test
    void testCalculateDaysInStage() {
        Application app = new Application();
        app.setSubmissionDate(LocalDateTime.now().minusDays(5));
        
        // No statusUpdatedAt, should use submissionDate -> 5 days
        long days = reportService.calculateDaysInStage(app);
        assertEquals(5, days);

        // With statusUpdatedAt -> 2 days
        app.setStatusUpdatedAt(LocalDateTime.now().minusDays(2));
        days = reportService.calculateDaysInStage(app);
        assertEquals(2, days);
    }
}
