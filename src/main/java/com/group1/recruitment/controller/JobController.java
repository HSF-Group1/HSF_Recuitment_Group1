package com.group1.recruitment.controller;

import com.group1.recruitment.dto.JobForm;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manage/jobs")
public class JobController {

    private final User user;

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public JobController(JobService jobService, ApplicationService applicationService,
            UserRepository userRepository) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userRepository = userRepository;

        this.user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // private User managed(SessionUser user) {
    // return userRepository.findById(user.getId()).orElse(null);
    // }

    // ---------------- SCR-10 Job List ----------------

    @GetMapping
    public String list(@RequestParam(required = false) String status,
            @RequestParam(required = false) String q, Model model) {
        List<JobPosting> all = jobService.listForManager(user);

        JobStatus tab = parseStatus(status);
        String term = q == null ? null : q.trim().toLowerCase();

        List<JobPosting> filtered = all.stream()
                .filter(j -> tab == null || j.getStatus() == tab)
                .filter(j -> term == null || term.isEmpty()
                        || (j.getTitle() != null && j.getTitle().toLowerCase().contains(term)))
                .toList();

        Map<Long, Long> appCounts = new LinkedHashMap<>();
        for (JobPosting j : filtered) {
            appCounts.put(j.getId(), jobService.applicationCount(j));
        }

        model.addAttribute("jobs", filtered);
        model.addAttribute("appCounts", appCounts);
        model.addAttribute("activeTab", tab == null ? "ALL" : tab.name());
        model.addAttribute("q", q);
        model.addAttribute("tabCounts", tabCounts(all));
        return "job/list";
    }

    private Map<String, Long> tabCounts(List<JobPosting> all) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("ALL", (long) all.size());
        counts.put("DRAFT", all.stream().filter(j -> j.getStatus() == JobStatus.DRAFT).count());
        counts.put("ACTIVE", all.stream().filter(j -> j.getStatus() == JobStatus.ACTIVE).count());
        counts.put("CLOSED", all.stream().filter(j -> j.getStatus() == JobStatus.CLOSED).count());
        return counts;
    }

    // ---------------- SCR-11 Job Form ----------------

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("jobForm")) {
            model.addAttribute("jobForm", new JobForm());
        }
        model.addAttribute("editMode", false);
        return "job/form";
    }

    @PostMapping
    public String create(@ModelAttribute JobForm jobForm,
            RedirectAttributes ra, Model model) {
        try {
            JobPosting job = jobService.create(jobForm, user);
            ra.addFlashAttribute("flash", "Job posting saved as Draft.");
            return "redirect:/manage/jobs/" + job.getId();
        } catch (ValidationException ex) {
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("editMode", false);
            return "job/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        JobPosting job = jobService.getOrThrow(id);
        jobService.assertCanManage(job, user);
        if (!model.containsAttribute("jobForm")) {
            model.addAttribute("jobForm", JobForm.from(job));
        }
        model.addAttribute("editMode", true);
        model.addAttribute("job", job);
        return "job/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute JobForm jobForm, RedirectAttributes ra, Model model) {
        try {
            jobService.update(id, jobForm, user);
            ra.addFlashAttribute("flash", "Job posting updated.");
            return "redirect:/manage/jobs/" + id;
        } catch (ValidationException ex) {
            JobPosting job = jobService.getOrThrow(id);
            model.addAttribute("errors", ex.getErrors());
            model.addAttribute("editMode", true);
            model.addAttribute("job", job);
            return "job/form";
        }
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id, RedirectAttributes ra) {
        actAndFlash(id, ra, () -> jobService.publish(id, user), "Job published.");
        return "redirect:/manage/jobs/" + id;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes ra) {
        actAndFlash(id, ra, () -> jobService.close(id, user), "Job closed.");
        return "redirect:/manage/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            jobService.delete(id, user);
            ra.addFlashAttribute("flash", "Job posting deleted.");
            return "redirect:/manage/jobs";
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Unable to delete job."));
            return "redirect:/manage/jobs/" + id;
        }
    }

    // ---------------- SCR-12 Job Detail ----------------

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobPosting job = jobService.getOrThrow(id);
        if (!jobService.canManage(job, user)) {
            throw new AccessDeniedException("You cannot view this job posting.");
        }
        model.addAttribute("job", job);
        model.addAttribute("applicationCount", jobService.applicationCount(job));
        model.addAttribute("pipeline", applicationService.pipelineCounts(job));
        return "job/detail";
    }

    private void actAndFlash(Long id, RedirectAttributes ra, Runnable action, String ok) {
        try {
            action.run();
            ra.addFlashAttribute("flash", ok);
        } catch (ValidationException ex) {
            ra.addFlashAttribute("error", ex.getErrors().getOrDefault("global", "Action failed."));
        }
    }

    private JobStatus parseStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL"))
            return null;
        try {
            return JobStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
