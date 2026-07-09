package com.group1.recruitment.service;

import com.group1.recruitment.entity.ActivityLog;
import com.group1.recruitment.entity.Evaluation;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.ActivityLogRepository;
import com.group1.recruitment.repository.EvaluationRepository;
import com.group1.recruitment.repository.InterviewRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    public EvaluationService(EvaluationRepository evaluationRepository,
                             InterviewRepository interviewRepository,
                             UserRepository userRepository,
                             ActivityLogRepository activityLogRepository) {
        this.evaluationRepository = evaluationRepository;
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public Evaluation submitEvaluation(Long interviewId, Integer rating, String feedback, SessionUser actor, String ipAddress) {
        // Validate actor
        if (actor == null) {
            throw new AccessDeniedException("User must be logged in");
        }

        // Find Interview
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found with ID: " + interviewId));

        // Validate Interview Status
        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled interviews can be evaluated");
        }

        // Validate interviewer assignment
        if (interview.getInterviewer() == null || !interview.getInterviewer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only the assigned interviewer can submit evaluation");
        }

        // Check if evaluation already exists
        if (evaluationRepository.existsByInterviewId(interviewId)) {
            throw new IllegalStateException("This interview has already been evaluated");
        }

        // Validate Rating
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Overall rating must be between 1 and 5");
        }

        // Validate Feedback
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback comments are required");
        }

        // Save Evaluation
        Evaluation evaluation = new Evaluation();
        evaluation.setInterview(interview);
        evaluation.setRating(rating);
        evaluation.setFeedback(feedback);
        evaluation.setSubmittedAt(LocalDateTime.now());
        Evaluation saved = evaluationRepository.save(evaluation);

        // Update Interview Status to EVALUATED
        interview.setStatus(InterviewStatus.EVALUATED);
        interviewRepository.save(interview);

        // Log Activity
        User user = userRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + actor.getId()));
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setEventType(EventType.EVALUATION_SUBMITTED);
        log.setDescription(String.format("Evaluation submitted for application #%d and interview #%d", 
                interview.getApplication().getId(), interviewId));
        log.setIpAddress(ipAddress != null ? ipAddress : "N/A");
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);

        return saved;
    }

    @Transactional(readOnly = true)
    public Evaluation getByInterviewId(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found with ID: " + interviewId));
        return evaluationRepository.findByInterview(interview)
                .orElseThrow(() -> new NotFoundException("Evaluation not found for interview ID: " + interviewId));
    }

    @Transactional(readOnly = true)
    public boolean hasEvaluation(Long interviewId) {
        return evaluationRepository.existsByInterviewId(interviewId);
    }
}
