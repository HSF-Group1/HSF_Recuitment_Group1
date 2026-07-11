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
import java.time.LocalDateTime;

@Controller
public class DashboardController {

    private final JobPostingRepository jobRepo;
    private final ApplicationRepository appRepo;
    private final UserRepository userRepo;
    private final InterviewRepository interviewRepo;

    public DashboardController(JobPostingRepository jobRepo,
                               ApplicationRepository appRepo,
                               UserRepository userRepo,
                               InterviewRepository interviewRepo) {
        this.jobRepo       = jobRepo;
        this.appRepo       = appRepo;
        this.userRepo      = userRepo;
        this.interviewRepo = interviewRepo;
    }

    // ------------------------------------------------------------------ HR ---
    @GetMapping("/hr/dashboard")
    public String hrDashboard(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);

        User hrEntity = userRepo.findById(user.getId()).orElse(null);
        if (hrEntity != null) {
            long activeJobs      = jobRepo.countByCreatedByAndStatus(hrEntity, JobStatus.ACTIVE);
            long newApps         = appRepo.countByJobPosting_CreatedByAndStatus(hrEntity, ApplicationStatus.APPLIED);
            long interviewsToday = interviewRepo.countByHrAndDate(hrEntity, LocalDate.now());

            model.addAttribute("activeJobs",      activeJobs);
            model.addAttribute("newApps",         newApps);
            model.addAttribute("interviewsToday", interviewsToday);

            // --- Grouped bar chart: applications + interviews per day for last 7 days ---
            LocalDate today = LocalDate.now();
            StringBuilder labels    = new StringBuilder("[");
            StringBuilder appCounts = new StringBuilder("[");
            StringBuilder ivCounts  = new StringBuilder("[");

            for (int i = 6; i >= 0; i--) {
                LocalDate day      = today.minusDays(i);
                LocalDateTime from = day.atStartOfDay();
                LocalDateTime to   = day.plusDays(1).atStartOfDay();

                long apps = appRepo.countByHrAndSubmissionDateBetween(hrEntity, from, to);
                long ivs  = interviewRepo.countByHrAndExactDate(hrEntity, day);

                labels.append("\"").append(day.getDayOfWeek().name(), 0, 3).append("\"");
                appCounts.append(apps);
                ivCounts.append(ivs);
                if (i > 0) {
                    labels.append(",");
                    appCounts.append(",");
                    ivCounts.append(",");
                }
            }
            labels.append("]");
            appCounts.append("]");
            ivCounts.append("]");

            model.addAttribute("chartLabels",    labels.toString());
            model.addAttribute("chartAppCounts", appCounts.toString());
            model.addAttribute("chartIvCounts",  ivCounts.toString());

        } else {
            model.addAttribute("activeJobs",      0);
            model.addAttribute("newApps",         0);
            model.addAttribute("interviewsToday", 0);
            model.addAttribute("chartLabels",    "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\",\"SAT\",\"SUN\"]");
            model.addAttribute("chartAppCounts", "[0,0,0,0,0,0,0]");
            model.addAttribute("chartIvCounts",  "[0,0,0,0,0,0,0]");
        }

        model.addAttribute("title", "HR Dashboard");
        return "dashboard/hr";
    }

    // --------------------------------------------------------------- Admin ---
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        SessionUser user = SessionUtil.require(session);
        if (!user.isAdmin()) {
            return "redirect:/home";
        }

        long hrManagersCount = userRepo.countByRole_Name(SessionUser.HR_MANAGER);
        long totalUsers      = userRepo.count();
        long totalJobs       = jobRepo.count();
        long totalApps       = appRepo.count();

        model.addAttribute("hrManagersCount", hrManagersCount);
        model.addAttribute("totalUsers",      totalUsers);
        model.addAttribute("totalJobs",       totalJobs);
        model.addAttribute("totalApps",       totalApps);

        // --- Doughnut Chart: Global Application Statuses ---
        long applied   = appRepo.countByStatus(ApplicationStatus.APPLIED);
        long screening = appRepo.countByStatus(ApplicationStatus.SCREENING);
        long interview = appRepo.countByStatus(ApplicationStatus.INTERVIEW);
        long offer     = appRepo.countByStatus(ApplicationStatus.OFFER);
        long rejected  = appRepo.countByStatus(ApplicationStatus.REJECTED);
        String statusData = "[" + applied + "," + screening + "," + interview + "," + offer + "," + rejected + "]";
        model.addAttribute("statusData", statusData);

        // --- Line Chart: New Users vs New Applications for last 7 days ---
        LocalDate today = LocalDate.now();
        StringBuilder labels    = new StringBuilder("[");
        StringBuilder appCounts = new StringBuilder("[");
        StringBuilder userCounts = new StringBuilder("[");

        for (int i = 6; i >= 0; i--) {
            LocalDate day      = today.minusDays(i);
            LocalDateTime from = day.atStartOfDay();
            LocalDateTime to   = day.plusDays(1).atStartOfDay();
            
            long cntApps  = appRepo.countBySubmissionDateBetween(from, to);
            long cntUsers = userRepo.countByCreatedAtBetween(from, to);
            
            labels.append("\"").append(day.getDayOfWeek().name(), 0, 3).append("\"");
            appCounts.append(cntApps);
            userCounts.append(cntUsers);
            if (i > 0) {
                labels.append(",");
                appCounts.append(",");
                userCounts.append(",");
            }
        }
        labels.append("]");
        appCounts.append("]");
        userCounts.append("]");

        model.addAttribute("activityLabels", labels.toString());
        model.addAttribute("activityAppCounts", appCounts.toString());
        model.addAttribute("activityUserCounts", userCounts.toString());

        model.addAttribute("title", "Admin Dashboard");
        return "dashboard/admin";
    }
}
