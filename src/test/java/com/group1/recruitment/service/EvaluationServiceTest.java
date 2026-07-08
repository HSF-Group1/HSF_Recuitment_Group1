package com.group1.recruitment.service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Evaluation;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EvaluationServiceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    void testSubmitEvaluationSuccess() {
        // Prepare scheduled interview
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        
        // Schedule interview with Interviewer ID 4 (interviewer1)
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);
        Interview interview = interviewService.scheduleInterview(2L, 4L, date, time, "Room 101", "Notes", hrActor);
        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());

        // Evaluation actor is the interviewer (ID 4)
        SessionUser interviewerActor = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");

        Evaluation eval = evaluationService.submitEvaluation(interview.getId(), 4, "Candidate has strong SQL skills", interviewerActor, "127.0.0.1");

        assertNotNull(eval);
        assertEquals(4, eval.getRating());
        assertEquals("Candidate has strong SQL skills", eval.getFeedback());
        assertEquals(InterviewStatus.EVALUATED, interview.getStatus());

        // Verify fetching works
        Evaluation fetched = evaluationService.getByInterviewId(interview.getId());
        assertEquals(eval.getId(), fetched.getId());
        assertTrue(evaluationService.hasEvaluation(interview.getId()));
    }

    @Test
    void testSubmitEvaluationFailInterviewNotFound() {
        SessionUser interviewerActor = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");
        assertThrows(NotFoundException.class, () -> {
            evaluationService.submitEvaluation(999L, 4, "Good", interviewerActor, "127.0.0.1");
        });
    }

    @Test
    void testSubmitEvaluationFailNotScheduled() {
        // Fetch an already evaluated interview (e.g. ID 5 has an evaluated interview)
        // Wait, let's create a new scheduled interview and cancel it first
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);
        
        // Cancel it
        interviewService.cancelInterview(interview.getId(), hrActor);
        assertEquals(InterviewStatus.CANCELLED, interview.getStatus());

        SessionUser interviewerActor = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");

        assertThrows(IllegalStateException.class, () -> {
            evaluationService.submitEvaluation(interview.getId(), 4, "Good", interviewerActor, "127.0.0.1");
        });
    }

    @Test
    void testSubmitEvaluationFailNotAssigned() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        // Actor is different interviewer (interviewer2, ID 5)
        SessionUser wrongInterviewer = new SessionUser(5L, "Le Thi Tu", "interviewer2", "tu.le@hsf.com", "INTERVIEWER");

        assertThrows(AccessDeniedException.class, () -> {
            evaluationService.submitEvaluation(interview.getId(), 4, "Good", wrongInterviewer, "127.0.0.1");
        });
    }

    @Test
    void testSubmitEvaluationFailInvalidRating() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        SessionUser interviewerActor = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");

        // Rating > 5
        assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.submitEvaluation(interview.getId(), 6, "Good", interviewerActor, "127.0.0.1");
        });

        // Rating < 1
        assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.submitEvaluation(interview.getId(), 0, "Good", interviewerActor, "127.0.0.1");
        });
    }

    @Test
    void testSubmitEvaluationFailEmptyFeedback() {
        Application app = applicationService.getOrThrow(2L);
        app.setStatus(ApplicationStatus.SCREENING);
        applicationRepository.save(app);

        SessionUser hrActor = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");
        Interview interview = interviewService.scheduleInterview(2L, 4L, LocalDate.now().plusDays(2), LocalTime.of(10, 0), "Room 101", "Notes", hrActor);

        SessionUser interviewerActor = new SessionUser(4L, "Nguyen Van Khoa", "interviewer1", "khoa.nguyen@hsf.com", "INTERVIEWER");

        assertThrows(IllegalArgumentException.class, () -> {
            evaluationService.submitEvaluation(interview.getId(), 4, "  ", interviewerActor, "127.0.0.1");
        });
    }
}
