package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class HrApplicationController {

    private final ApplicationService applicationService;
    private final JobService jobService;
    private final UserRepository userRepository;

    public HrApplicationController(ApplicationService applicationService, JobService jobService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    private User getLoggedUser(SessionUser user) {
        return userRepository.findById(user.getId()).orElse(null);
    }

    // 1. Xem danh sách đơn tuyển dụng chung (của HR hoặc Admin)
    @GetMapping({"/applications", "/manage/applications"})
    public String listAllApplications(@RequestParam(required = false) String status,
                                      HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        if (!sessionUser.isHr() && !sessionUser.isAdmin() && !sessionUser.isInterviewer()) {
            return "redirect:/error/403";
        }

        User currentUser = getLoggedUser(sessionUser);
        ApplicationStatus filterStatus = parseStatus(status);
        List<Application> applications;
        Map<ApplicationStatus, Long> pipelineCounts;

        if (sessionUser.isAdmin()) {
            // Admin thấy tất cả
            applications = (filterStatus != null)
                    ? applicationService.getApplicationRepository().findByStatusOrderBySubmissionDateDesc(filterStatus)
                    : applicationService.getApplicationRepository().findAllByOrderBySubmissionDateDesc();
            pipelineCounts = applicationService.getPipelineCountsAll();
        } else if (sessionUser.isHr()) {
            // HR chỉ thấy hồ sơ ứng tuyển của các Job do mình tạo
            applications = (filterStatus != null)
                    ? applicationService.getApplicationRepository().findByJobPosting_CreatedByAndStatusOrderBySubmissionDateDesc(currentUser, filterStatus)
                    : applicationService.getApplicationRepository().findByJobPosting_CreatedByOrderBySubmissionDateDesc(currentUser);
            pipelineCounts = applicationService.getPipelineCountsForHr(currentUser);
        } else {
            // Interviewer chỉ thấy hồ sơ ứng tuyển được assign Interview
            applications = (filterStatus != null)
                    ? applicationService.getApplicationRepository().findByInterviewerAndStatusOrderBySubmissionDateDesc(currentUser, filterStatus)
                    : applicationService.getApplicationRepository().findByInterviewerOrderBySubmissionDateDesc(currentUser);
            pipelineCounts = applicationService.getPipelineCountsForInterviewer(currentUser);
        }

        model.addAttribute("applications", applications);
        model.addAttribute("pipeline", pipelineCounts);
        model.addAttribute("activeTab", filterStatus == null ? "ALL" : filterStatus.name());
        model.addAttribute("title", "Applications List");
        model.addAttribute("activeMenu", "applications");
        return "application/list";
    }

    // 2. Xem danh sách đơn tuyển dụng của 1 Job cụ thể
    @GetMapping({"/jobs/{jobId}/applications", "/manage/jobs/{jobId}/applications"})
    public String listJobApplications(@PathVariable Long jobId,
                                      @RequestParam(required = false) String status,
                                      HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        if (!sessionUser.isHr() && !sessionUser.isAdmin()) {
            return "redirect:/error/403";
        }

        JobPosting job = jobService.getOrThrow(jobId);
        if (!jobService.canManage(job, sessionUser)) {
            throw new AccessDeniedException("You are not authorized to view applications for this job.");
        }

        ApplicationStatus filterStatus = parseStatus(status);
        List<Application> applications = (filterStatus != null)
                ? applicationService.getApplicationRepository().findByJobPostingAndStatusOrderBySubmissionDateDesc(job, filterStatus)
                : applicationService.getApplicationRepository().findByJobPostingOrderBySubmissionDateDesc(job);

        Map<ApplicationStatus, Long> pipelineCounts = applicationService.pipelineCounts(job);

        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("pipeline", pipelineCounts);
        model.addAttribute("activeTab", filterStatus == null ? "ALL" : filterStatus.name());
        model.addAttribute("title", job.getTitle() + " - Applications");
        model.addAttribute("activeMenu", "jobs");
        return "application/list";
    }

    private ApplicationStatus parseStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
