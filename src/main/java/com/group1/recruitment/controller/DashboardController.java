package com.group1.recruitment.controller;

import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.repository.InterviewRepository;
import com.group1.recruitment.repository.JobPostingRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    private final JobPostingRepository jobRepo;
    private final ApplicationRepository appRepo;
    private final UserRepository userRepo;
    private final InterviewRepository interviewRepo;

    public DashboardController(JobPostingRepository jobRepo, ApplicationRepository appRepo, UserRepository userRepo, InterviewRepository interviewRepo) {
        this.jobRepo = jobRepo;
        this.appRepo = appRepo;
        this.userRepo = userRepo;
        this.interviewRepo = interviewRepo;
    }

    @GetMapping("/hr/dashboard")
    public String hrDashboard(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        
        // Fetch HR's user entity
        User hrEntity = userRepo.findById(user.getId()).orElse(null);
        if (hrEntity != null) {
            long activeJobs = jobRepo.countByCreatedByAndStatus(hrEntity, JobStatus.ACTIVE);
            long newApps = appRepo.countByJobPosting_CreatedByAndStatus(hrEntity, ApplicationStatus.APPLIED);
            long interviewsToday = interviewRepo.countByHrAndDate(hrEntity, LocalDate.now());
            
            model.addAttribute("activeJobs", activeJobs);
            model.addAttribute("newApps", newApps);
            model.addAttribute("interviewsToday", interviewsToday);
        } else {
            model.addAttribute("activeJobs", 0);
            model.addAttribute("newApps", 0);
            model.addAttribute("interviewsToday", 0);
        }

        model.addAttribute("title", "HR Dashboard");
        return "dashboard/hr";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        if (!user.isAdmin()) {
            return "redirect:/home";
        }
        
        long hrManagersCount = userRepo.countByRole_Name(SessionUser.HR_MANAGER);
        long totalUsers = userRepo.count();
        long totalJobs = jobRepo.count();

        model.addAttribute("hrManagersCount", hrManagersCount);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalJobs", totalJobs);

        model.addAttribute("title", "Admin Dashboard");
        return "dashboard/admin";
    }
}
