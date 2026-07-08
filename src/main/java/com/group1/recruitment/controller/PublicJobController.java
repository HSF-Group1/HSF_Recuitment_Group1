package com.group1.recruitment.controller;

import com.group1.recruitment.enums.JobStatus;
import com.group1.recruitment.repository.JobPostingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicJobController {
    private final JobPostingRepository jobPostingRepository;

    public PublicJobController(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {
        model.addAttribute("jobs", jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.ACTIVE));
        return "public/jobs";
    }
}
