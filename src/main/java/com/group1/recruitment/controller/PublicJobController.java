package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.JobPosting;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.JobPostingRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicJobController {
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationService applicationService;

    public PublicJobController(JobPostingRepository jobPostingRepository, UserRepository userRepository,
            CandidateRepository candidateRepository, ApplicationService applicationService) {
        this.jobPostingRepository = jobPostingRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.applicationService = applicationService;
    }

    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {
        SessionUser currentUser = SessionUtil.current(session);
        if (currentUser != null && !currentUser.isCandidate()) {
            return "redirect:/error/403";
        }
        model.addAttribute("jobs", jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.ACTIVE));
        return "public/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser currentUser = SessionUtil.current(session);
        if (currentUser != null && !currentUser.isCandidate()) {
            return "redirect:/error/403";
        }
        JobPosting job = activeJobOrThrow(id);
        model.addAttribute("job", job);
        return "public/job-detail";
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id,
            @RequestParam(value = "cvFile", required = false) MultipartFile cvFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        SessionUser currentUser = SessionUtil.current(session);
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        if (!currentUser.isCandidate()) {
            return "redirect:/error/403";
        }

        JobPosting job = activeJobOrThrow(id);
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found."));
        Candidate candidate = candidateRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Candidate profile not found."));

        String cvFileUrl = cvFile != null && !cvFile.isEmpty() ? cvFile.getOriginalFilename() : null;
        try {
            applicationService.applyToJob(candidate, job, cvFileUrl);
            redirectAttributes.addFlashAttribute("flash", "Your application was submitted.");
        } catch (ValidationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    ex.getErrors().getOrDefault("global", "Unable to submit application."));
        }
        return "redirect:/jobs/" + id;
    }

    private JobPosting activeJobOrThrow(Long id) {
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job not found."));
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new NotFoundException("Job not found.");
        }
        return job;
    }
}
