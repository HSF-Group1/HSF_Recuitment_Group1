package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.enums.InterviewStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.ApplicationWorkflowService;
import com.group1.recruitment.service.InterviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final ApplicationService applicationService;
    private final ApplicationWorkflowService workflowService;

    public InterviewController(InterviewService interviewService,
                               ApplicationService applicationService,
                               ApplicationWorkflowService workflowService) {
        this.interviewService = interviewService;
        this.applicationService = applicationService;
        this.workflowService = workflowService;
    }

    @GetMapping("/assign/{applicationId}")
    public String assignForm(@PathVariable Long applicationId, HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        Application application = applicationService.getOrThrow(applicationId);

        // Security check
        checkAccess(application, sessionUser);

        model.addAttribute("app", application);
        model.addAttribute("interviewers", interviewService.getActiveInterviewers());

        Interview activeInterview = application.getInterviews() != null ?
                application.getInterviews().stream()
                        .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED)
                        .findFirst()
                        .orElse(null) : null;
        model.addAttribute("activeInterview", activeInterview);
        return "interview/assign";
    }

    @PostMapping("/assign/{applicationId}")
    public String schedule(@PathVariable Long applicationId,
                           @RequestParam(required = false) Long interviewerId,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
                           @RequestParam(required = false) String location,
                           @RequestParam(required = false) String notes,
                           HttpSession session,
                           Model model,
                           @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                           jakarta.servlet.http.HttpServletResponse response) {
        SessionUser sessionUser = SessionUtil.require(session);
        Application application = applicationService.getOrThrow(applicationId);

        try {
            if (interviewerId == null) {
                throw new IllegalArgumentException("Interviewer must be selected");
            }
            if (date == null) {
                throw new IllegalArgumentException("Interview date is required");
            }
            if (time == null) {
                throw new IllegalArgumentException("Interview time is required");
            }
            if (location == null || location.trim().isEmpty()) {
                throw new IllegalArgumentException("Location/Meeting link is required");
            }

            Interview interview = interviewService.scheduleInterview(applicationId, interviewerId, date, time, location, notes, sessionUser);

            model.addAttribute("interview", interview);
            model.addAttribute("app", application);

            if (hxRequest != null) {
                response.setHeader("HX-Redirect", "/application/" + applicationId);
                return null;
            }
            return "redirect:/application/" + applicationId;

        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("app", application);
            model.addAttribute("interviewers", interviewService.getActiveInterviewers());
            model.addAttribute("interviewerId", interviewerId);
            model.addAttribute("date", date);
            model.addAttribute("time", time);
            model.addAttribute("location", location);
            model.addAttribute("notes", notes);

            if (hxRequest != null) {
                return "interview/fragments/_interview_form";
            }
            return "interview/assign";
        }
    }

    @PostMapping("/cancel/{interviewId}")
    public String cancel(@PathVariable Long interviewId,
                         HttpSession session,
                         @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                         Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        Interview interview = interviewService.getInterviewOrThrow(interviewId);
        Application application = interview.getApplication();

        // Security check
        checkAccess(application, sessionUser);

        interviewService.cancelInterview(interviewId, sessionUser);

        if (hxRequest != null) {
            // Re-query application to get the updated status and list of interviews
            application = applicationService.getOrThrow(application.getId());
            model.addAttribute("currentApplication", application);
            model.addAttribute("currentUserRole", sessionUser.getRoleName());
            model.addAttribute("applicationStatus", application.getStatus());
            model.addAttribute("allowedTransitions", workflowService.getAllowedTransitions(application.getStatus()));

            boolean canDownload = sessionUser.isAdmin() || sessionUser.isHr() ||
                    (sessionUser.isInterviewer() && application.getInterviews() != null &&
                            application.getInterviews().stream()
                                    .anyMatch(i -> i.getInterviewer() != null && i.getInterviewer().getId().equals(sessionUser.getId())));
            model.addAttribute("canDownloadCv", canDownload);

            return "application/detail :: #application-detail-panels";
        }

        return "redirect:/application/" + application.getId();
    }

    private void checkAccess(Application application, SessionUser sessionUser) {
        if (sessionUser.isAdmin()) {
            return;
        }
        if (sessionUser.isHr()) {
            if (application.getJobPosting() != null &&
                    application.getJobPosting().getCreatedBy() != null &&
                    application.getJobPosting().getCreatedBy().getId().equals(sessionUser.getId())) {
                return;
            }
        }
        throw new AccessDeniedException("You do not have access to manage interviews for this application.");
    }
}
