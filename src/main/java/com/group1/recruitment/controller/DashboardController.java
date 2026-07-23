package com.group1.recruitment.controller;

import com.group1.recruitment.entity.ActivityLog;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.repository.ActivityLogRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final JobPostingRepository jobRepo;
    private final ApplicationRepository appRepo;
    private final UserRepository userRepo;
    private final InterviewRepository interviewRepo;
    private final ActivityLogRepository activityLogRepo;

    public DashboardController(JobPostingRepository jobRepo, ApplicationRepository appRepo,
                               UserRepository userRepo, InterviewRepository interviewRepo,
                               ActivityLogRepository activityLogRepo) {
        this.jobRepo = jobRepo;
        this.appRepo = appRepo;
        this.userRepo = userRepo;
        this.interviewRepo = interviewRepo;
        this.activityLogRepo = activityLogRepo;
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

        // Fetch last 7 days of logs to calculate activity counts
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<ActivityLog> recentLogs = activityLogRepo.findByTimestampGreaterThanEqual(sevenDaysAgo);

        Map<String, Long> countsByDay = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            countsByDay.put(day.name(), 0L);
        }
        for (ActivityLog log : recentLogs) {
            String dayName = log.getTimestamp().getDayOfWeek().name();
            countsByDay.put(dayName, countsByDay.get(dayName) + 1);
        }

        long maxCount = 0;
        for (long count : countsByDay.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }

        Map<String, Integer> heights = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            long count = countsByDay.get(day.name());
            int height = 0;
            if (count > 0) {
                if (maxCount > 0) {
                    height = (int) (5 + (count * 175.0 / maxCount)); // max height is 180px
                } else {
                    height = 5;
                }
            }
            heights.put(day.name(), height);
        }

        model.addAttribute("activityHeights", heights);
        model.addAttribute("activityCounts", countsByDay);

        model.addAttribute("title", "Admin Dashboard");
        return "dashboard/admin";
    }
}
