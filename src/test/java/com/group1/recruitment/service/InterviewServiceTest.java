package com.group1.recruitment.service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InterviewServiceTest {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Test
    void testGetActiveInterviewers() {
        List<User> interviewers = interviewService.getActiveInterviewers();
        assertNotNull(interviewers);
        assertFalse(interviewers.isEmpty());
        assertTrue(interviewers.stream().allMatch(u -> "INTERVIEWER".equals(u.getRole().getName())));
    }

    @Test
    void testScheduleInterviewSuccess() {
        // Application 2 is for Job 1 (created by hr_hieu, User ID 3)
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);
        assertEquals(ApplicationStatus.SCREENING, app.getStatus());

        SessionUser actor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        
        // Find an interviewer from active list
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        Interview interview = interviewService.scheduleInterview(2L, interviewerId, date, time, "Room 101", "Please prepare CV", actor);

        assertNotNull(interview);
        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());
        assertEquals("Room 101", interview.getLocationOrLink());
        assertEquals("Please prepare CV", interview.getNotes());
        assertEquals(ApplicationStatus.INTERVIEW, interview.getApplication().getStatus());
    }

    @Test
    void testScheduleInterviewFailPastDate() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser actor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        assertThrows(IllegalArgumentException.class, () -> {
            interviewService.scheduleInterview(2L, interviewerId, pastDate, time, "Room 101", "", actor);
        });
    }

    @Test
    void testScheduleInterviewFailInvalidStatus() {
        // Application 5 is in OFFER status
        Application app = applicationService.getOrThrow(5L);
        app.setStatus(ApplicationStatus.OFFER);
        applicationRepository.save(app);
        assertEquals(ApplicationStatus.OFFER, app.getStatus());

        SessionUser actor = new SessionUser(2L, "Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", "HR_MANAGER");
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        assertThrows(IllegalStateException.class, () -> {
            interviewService.scheduleInterview(5L, interviewerId, date, time, "Room 101", "", actor);
        });
    }

    @Test
    void testScheduleInterviewFailAlreadyScheduled() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser actor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        // Schedule first interview
        interviewService.scheduleInterview(2L, interviewerId, date, time, "Room 101", "", actor);

        // Attempt to schedule second one
        assertThrows(IllegalStateException.class, () -> {
            interviewService.scheduleInterview(2L, interviewerId, date.plusDays(1), time, "Room 102", "", actor);
        });
    }

    @Test
    void testScheduleInterviewFailUserNotInterviewer() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser actor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        // User ID 1 is Admin, not Interviewer
        assertThrows(IllegalArgumentException.class, () -> {
            interviewService.scheduleInterview(2L, 1L, date, time, "Room 101", "", actor);
        });
    }

    @Test
    void testScheduleInterviewFailPermissionDenied() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        // User ID 2 (hr_huong) did not create Job 1 (job 1 was created by hr_hieu, User ID 3)
        SessionUser actor = new SessionUser(2L, "Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", "HR_MANAGER");
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        assertThrows(com.group1.recruitment.exception.AccessDeniedException.class, () -> {
            interviewService.scheduleInterview(2L, interviewerId, date, time, "Room 101", "", actor);
        });
    }

    @Test
    void testCancelInterviewFailPermissionDenied() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser ownerHr = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        Interview interview = interviewService.scheduleInterview(2L, interviewerId, date, time, "Room 101", "", ownerHr);

        // Unauthorized HR (hr_huong, User ID 2) attempts to cancel
        SessionUser otherHr = new SessionUser(2L, "Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", "HR_MANAGER");
        assertThrows(com.group1.recruitment.exception.AccessDeniedException.class, () -> {
            interviewService.cancelInterview(interview.getId(), otherHr);
        });
    }

    @Test
    void testCancelInterviewSuccess() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser actor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        List<User> interviewers = interviewService.getActiveInterviewers();
        Long interviewerId = interviewers.get(0).getId();

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        Interview interview = interviewService.scheduleInterview(2L, interviewerId, date, time, "Room 101", "", actor);

        // Cancel it
        interviewService.cancelInterview(interview.getId(), actor);

        assertEquals(InterviewStatus.CANCELLED, interview.getStatus());
        assertEquals(ApplicationStatus.SCREENING, app.getStatus());
    }
}
