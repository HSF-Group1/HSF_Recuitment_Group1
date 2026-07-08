package com.group1.recruitment.service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.repository.InterviewRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public List<User> getActiveInterviewers() {
        return userRepository.findByRole_NameAndStatusOrderByFullNameAsc("INTERVIEWER", AccountStatus.ACTIVE);
    }

    @Transactional
    public Interview scheduleInterview(Long applicationId, Long interviewerId, LocalDate date, LocalTime time, String location, String notes, SessionUser actor) {
        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));

        // Validate actor permission
        checkSchedulingPermission(application, actor);

        // Validation rules:
        // Status must be SCREENING or INTERVIEW (to schedule a new round/reschedule)
        if (application.getStatus() != ApplicationStatus.SCREENING && application.getStatus() != ApplicationStatus.INTERVIEW) {
            throw new IllegalStateException("Application status must be SCREENING or INTERVIEW to schedule an interview");
        }

        // Only one active scheduled interview at a time
        boolean hasActiveScheduled = application.getInterviews() != null &&
                application.getInterviews().stream()
                        .anyMatch(i -> i.getStatus() == InterviewStatus.SCHEDULED);
        if (hasActiveScheduled) {
            throw new IllegalStateException("An interview is already scheduled for this application");
        }

        // Date check: date cannot be in the past
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Interview date cannot be in the past");
        }

        User interviewer = userRepository.findById(interviewerId)
                .orElseThrow(() -> new NotFoundException("Interviewer not found with ID: " + interviewerId));

        if (interviewer.getRole() == null || !"INTERVIEWER".equals(interviewer.getRole().getName())) {
            throw new IllegalArgumentException("Selected user is not an Interviewer");
        }

        if (interviewer.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Selected interviewer account is not active");
        }

        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewer(interviewer);
        interview.setInterviewDate(date);
        interview.setInterviewTime(time);
        interview.setLocationOrLink(location);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setNotes(notes);

        Interview saved = interviewRepository.save(interview);

        // Maintain bidirectional relationship in-memory
        if (application.getInterviews() == null) {
            application.setInterviews(new java.util.ArrayList<>());
        }
        application.getInterviews().add(saved);

        // Update application status to INTERVIEW
        application.setStatus(ApplicationStatus.INTERVIEW);
        applicationRepository.save(application);

        return saved;
    }

    @Transactional
    public void cancelInterview(Long interviewId, SessionUser actor) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found with ID: " + interviewId));

        Application application = interview.getApplication();
        checkSchedulingPermission(application, actor);

        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled interviews can be cancelled");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);

        // Revert Application status to SCREENING if there are no other active/completed interviews left
        boolean hasOtherActiveOrEvaluated = application.getInterviews() != null &&
                application.getInterviews().stream()
                        .anyMatch(i -> !i.getId().equals(interviewId) && (i.getStatus() == InterviewStatus.SCHEDULED || i.getStatus() == InterviewStatus.EVALUATED));
        if (!hasOtherActiveOrEvaluated) {
            application.setStatus(ApplicationStatus.SCREENING);
            applicationRepository.save(application);
        }
    }

    private void checkSchedulingPermission(Application application, SessionUser actor) {
        if (actor.isAdmin()) {
            return;
        }
        if (actor.isHr()) {
            if (application.getJobPosting() != null &&
                    application.getJobPosting().getCreatedBy() != null &&
                    application.getJobPosting().getCreatedBy().getId().equals(actor.getId())) {
                return;
            }
        }
        throw new AccessDeniedException("You do not have permission to manage interviews for this application");
    }

    public Interview getInterviewOrThrow(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + id));
    }
}
