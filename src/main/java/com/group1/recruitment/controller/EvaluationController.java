package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Evaluation;
import com.group1.recruitment.entity.Interview;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.InterviewRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.EvaluationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/interviews")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final InterviewRepository interviewRepository;

    public EvaluationController(EvaluationService evaluationService, InterviewRepository interviewRepository) {
        this.evaluationService = evaluationService;
        this.interviewRepository = interviewRepository;
    }

    @GetMapping("/evaluate/{interviewId}")
    public String evaluateForm(@PathVariable Long interviewId, HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found with ID: " + interviewId));

        boolean isAssignedInterviewer = interview.getInterviewer() != null && 
                interview.getInterviewer().getId().equals(sessionUser.getId());
        boolean isHrOrAdmin = sessionUser.isHr() || sessionUser.isAdmin();

        // Access Control
        if (!isAssignedInterviewer && !isHrOrAdmin) {
            throw new AccessDeniedException("You do not have permission to view this evaluation");
        }

        // If not evaluated and user is NOT the interviewer, they cannot access the form
        boolean evaluated = evaluationService.hasEvaluation(interviewId);
        if (!evaluated && !isAssignedInterviewer) {
            throw new AccessDeniedException("Only the assigned interviewer can access the evaluation form");
        }

        model.addAttribute("interview", interview);
        model.addAttribute("app", interview.getApplication());

        if (evaluated) {
            Evaluation evaluation = evaluationService.getByInterviewId(interviewId);
            model.addAttribute("evaluation", evaluation);
        }

        return "interview/evaluate";
    }

    @PostMapping("/evaluate/{interviewId}")
    public String submitEvaluation(@PathVariable Long interviewId,
                                   @RequestParam(required = false) Integer rating,
                                   @RequestParam(required = false) String feedback,
                                   HttpSession session,
                                   Model model,
                                   HttpServletRequest request,
                                   @RequestHeader(value = "HX-Request", required = false) String hxRequest) {
        SessionUser sessionUser = SessionUtil.require(session);

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new NotFoundException("Interview not found with ID: " + interviewId));

        try {
            Evaluation evaluation = evaluationService.submitEvaluation(
                    interviewId, rating, feedback, sessionUser, request.getRemoteAddr());

            model.addAttribute("evaluation", evaluation);
            model.addAttribute("interview", interview);
            model.addAttribute("app", interview.getApplication());

            if (hxRequest != null) {
                return "interview/fragments/_evaluation_result";
            }
            return "redirect:/applications/" + interview.getApplication().getId();

        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("interview", interview);
            model.addAttribute("app", interview.getApplication());
            model.addAttribute("rating", rating);
            model.addAttribute("feedback", feedback);

            if (hxRequest != null) {
                return "interview/fragments/_evaluation_form";
            }
            return "interview/evaluate";
        }
    }
}
