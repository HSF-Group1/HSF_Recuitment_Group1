package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.repository.JobPostingRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final JobPostingRepository jobPostingRepository;

    public ReportController(ReportService reportService, JobPostingRepository jobPostingRepository) {
        this.reportService = reportService;
        this.jobPostingRepository = jobPostingRepository;
    }

    @GetMapping("/pipeline")
    public String pipelineReport(@RequestParam(required = false) Long jobId,
                                 HttpSession session,
                                 @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                 Model model) {
        SessionUser sessionUser = SessionUtil.require(session);

        // Security check: Only Admin and HR Managers are allowed
        if (!sessionUser.isAdmin() && !sessionUser.isHr()) {
            throw new AccessDeniedException("You do not have permission to view this report.");
        }

        // Fetch jobs for filter selector
        List<JobPosting> jobs;
        if (sessionUser.isHr()) {
            User hrUser = new User();
            hrUser.setId(sessionUser.getId());
            jobs = jobPostingRepository.findByCreatedByOrderByCreatedAtDesc(hrUser);
        } else {
            jobs = jobPostingRepository.findAllByOrderByCreatedAtDesc();
        }

        // Fetch data
        Map<ApplicationStatus, Long> summary = reportService.getPipelineSummary(jobId);
        List<Application> applications = reportService.getPipelineApplications(jobId);

        model.addAttribute("jobs", jobs);
        model.addAttribute("selectedJobId", jobId);
        model.addAttribute("summary", summary);
        model.addAttribute("applications", applications);
        model.addAttribute("reportService", reportService);

        if (hxRequest != null) {
            return "report/fragments/_pipeline_data";
        }

        return "report/pipeline";
    }
}
