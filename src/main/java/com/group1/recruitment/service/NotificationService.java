package com.group1.recruitment.service;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.enums.EventType;
import com.group1.recruitment.repository.ActivityLogRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    public NotificationService(UserRepository userRepository, ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Notify HR (Job creator and HR Managers), Admins, and Interviewers about candidate application details.
     */
    @Transactional
    public void notifyApplicationSubmitted(Application application) {
        if (application == null || application.getJobPosting() == null || application.getCandidate() == null) {
            return;
        }

        JobPosting job = application.getJobPosting();
        Candidate candidate = application.getCandidate();
        User candidateUser = candidate.getUser();

        String candidateName = candidateUser != null ? candidateUser.getFullName() : "Candidate";
        String candidateEmail = candidateUser != null ? candidateUser.getEmail() : "N/A";
        String jobTitle = job.getTitle() != null ? job.getTitle() : "N/A";
        String cvUrl = application.getCvFileUrl() != null ? application.getCvFileUrl() : "No CV provided";
        String phone = (candidate.getProfile() != null && candidate.getProfile().getPhone() != null)
                ? candidate.getProfile().getPhone()
                : "N/A";

        // Collect all target users (HRs, Admins, Interviewers)
        Map<Long, User> targetUsersMap = new HashMap<>();

        // 1. HR Manager who created the job
        if (job.getCreatedBy() != null) {
            targetUsersMap.put(job.getCreatedBy().getId(), job.getCreatedBy());
        }

        // 2. All active HR Managers
        List<User> hrManagers = userRepository.findByRole_NameAndStatusOrderByFullNameAsc(SessionUser.HR_MANAGER, AccountStatus.ACTIVE);
        for (User hr : hrManagers) {
            targetUsersMap.put(hr.getId(), hr);
        }

        // 3. All active Admins
        List<User> admins = userRepository.findByRole_NameAndStatusOrderByFullNameAsc(SessionUser.ADMIN, AccountStatus.ACTIVE);
        for (User admin : admins) {
            targetUsersMap.put(admin.getId(), admin);
        }

        // 4. All active Interviewers
        List<User> interviewers = userRepository.findByRole_NameAndStatusOrderByFullNameAsc(SessionUser.INTERVIEWER, AccountStatus.ACTIVE);
        for (User interviewer : interviewers) {
            targetUsersMap.put(interviewer.getId(), interviewer);
        }

        List<User> targetUsers = new ArrayList<>(targetUsersMap.values());
        List<String> recipientEmails = targetUsers.stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String logMessage = String.format(
                "[NOTIFICATION] Application Notification sent to HR, Admin, Interviewer (%s) -> Candidate: %s (%s, Phone: %s) applied for Job: '%s' (ID: %d). CV: %s",
                String.join(", ", recipientEmails),
                candidateName,
                candidateEmail,
                phone,
                jobTitle,
                job.getId(),
                cvUrl
        );

        logger.info(logMessage);

        // Save activity log entries for target users so they can see this event in activity logs
        for (User user : targetUsers) {
            ActivityLog activityLog = new ActivityLog();
            activityLog.setUser(user);
            activityLog.setEventType(EventType.APPLICATION_STATUS_CHANGED);
            activityLog.setDescription(String.format(
                    "Candidate %s (%s) applied for job '%s'. CV: %s",
                    candidateName, candidateEmail, jobTitle, cvUrl
            ));
            activityLog.setIpAddress("127.0.0.1");
            activityLog.setTimestamp(LocalDateTime.now());
            activityLogRepository.save(activityLog);
        }
    }
}
