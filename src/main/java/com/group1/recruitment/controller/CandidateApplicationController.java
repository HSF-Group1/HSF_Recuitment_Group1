package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.exception.ValidationException;
import com.group1.recruitment.repository.CandidateRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CandidateApplicationController {
    private final ApplicationService applicationService;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    public CandidateApplicationController(ApplicationService applicationService, UserRepository userRepository, CandidateRepository candidateRepository){
        this.applicationService = applicationService;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
    }

    @GetMapping("/profile/my-applications")
    public String myApplication(HttpSession session, Model model){
        SessionUser currentUser = SessionUtil.require(session);

        if(!currentUser.isCandidate()){
            return "redirect:/error/403";
        }
        Candidate candidate = candidateFrom(currentUser);
        List<Application> applications = applicationService.listForCandidate(candidate);
        model.addAttribute("applications", applications);
        return "profile/my-applications";
    }
    @PostMapping("/profile/my-applications/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        SessionUser currentUser = SessionUtil.require(session);

        if (!currentUser.isCandidate()) {
            return "redirect:/error/403";
        }

        Candidate candidate = candidateFrom(currentUser);

        try {
            applicationService.withdraw(id, candidate);
            redirectAttributes.addFlashAttribute("flash", "Application withdrawn.");
        } catch (ValidationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    ex.getErrors().getOrDefault("global", "Unable to withdraw application."));
        }

        return "redirect:/profile/my-applications";
    }

    private Candidate candidateFrom(SessionUser currentUser){
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new NotFoundException("User not found."));
        return candidateRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Candidate profile not found."));
    }
}
