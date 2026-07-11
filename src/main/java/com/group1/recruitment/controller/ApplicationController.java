package com.group1.recruitment.controller;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.enums.ApplicationStatus;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.security.SessionUtil;
import com.group1.recruitment.service.ApplicationService;
import com.group1.recruitment.service.ApplicationWorkflowService;
import com.group1.recruitment.service.InternalNoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationWorkflowService workflowService;
    private final InternalNoteService internalNoteService;

    public ApplicationController(ApplicationService applicationService,
            ApplicationWorkflowService workflowService,
            InternalNoteService internalNoteService) {
        this.applicationService = applicationService;
        this.workflowService = workflowService;
        this.internalNoteService = internalNoteService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        Application application = applicationService.getOrThrow(id);

        // Security check
        checkAccess(application, sessionUser);

        model.addAttribute("currentApplication", application);
        model.addAttribute("currentUserRole", sessionUser.getRoleName());
        model.addAttribute("applicationStatus", application.getStatus());
        model.addAttribute("allowedTransitions", workflowService.getAllowedTransitions(application.getStatus()));

        if (sessionUser.isHrOrAdmin()) {
            model.addAttribute("internalNotes", internalNoteService.getNotesForApplication(id));
        }

        // Check if CV download is permitted
        boolean canDownload = sessionUser.isAdmin() || sessionUser.isHr() || sessionUser.isCandidate() ||
                (sessionUser.isInterviewer() && application.getInterviews() != null &&
                        application.getInterviews().stream()
                                .anyMatch(i -> i.getInterviewer() != null
                                        && i.getInterviewer().getId().equals(sessionUser.getId())));
        model.addAttribute("canDownloadCv", canDownload);

        return "application/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam ApplicationStatus status,
            HttpSession session,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        Application application = applicationService.getOrThrow(id);

        // Only Admin or HR Managers can update status
        if (!sessionUser.isAdmin() && !sessionUser.isHr()) {
            throw new AccessDeniedException("Only Admin or HR Managers can update status.");
        }
        checkAccess(application, sessionUser);

        applicationService.updateApplicationStatus(id, status, sessionUser);

        if (hxRequest != null) {
            // Fetch the updated state
            application = applicationService.getOrThrow(id);
            model.addAttribute("currentApplication", application);
            model.addAttribute("currentUserRole", sessionUser.getRoleName());
            model.addAttribute("applicationStatus", application.getStatus());
            model.addAttribute("allowedTransitions", workflowService.getAllowedTransitions(application.getStatus()));

            if (sessionUser.isHrOrAdmin()) {
                model.addAttribute("internalNotes", internalNoteService.getNotesForApplication(id));
            }

            boolean canDownload = sessionUser.isAdmin() || sessionUser.isHr() || sessionUser.isCandidate() ||
                    (sessionUser.isInterviewer() && application.getInterviews() != null &&
                            application.getInterviews().stream()
                                    .anyMatch(i -> i.getInterviewer() != null
                                            && i.getInterviewer().getId().equals(sessionUser.getId())));
            model.addAttribute("canDownloadCv", canDownload);

            return "application/detail :: #application-detail-panels";
        }

        return "redirect:/applications/" + id;
    }

    @PostMapping("/{id}/notes")
    public String addNote(@PathVariable Long id,
            @RequestParam String content,
            HttpSession session,
            Model model) {
        SessionUser sessionUser = SessionUtil.require(session);
        Application application = applicationService.getOrThrow(id);

        // Security check
        checkAccess(application, sessionUser);

        internalNoteService.addNote(id, content, sessionUser);

        // Fetch application for model
        model.addAttribute("currentApplication", application);
        model.addAttribute("currentUserRole", sessionUser.getRoleName());
        model.addAttribute("internalNotes", internalNoteService.getNotesForApplication(id));

        return "application/detail :: #notes-panel";
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> downloadCv(@PathVariable Long id, HttpSession session) {
        SessionUser sessionUser = SessionUtil.require(session);
        Application application = applicationService.getOrThrow(id);

        // Check if CV download is permitted
        boolean canDownload = sessionUser.isAdmin() || sessionUser.isHr() || sessionUser.isCandidate() ||
                (sessionUser.isInterviewer() && application.getInterviews() != null &&
                        application.getInterviews().stream()
                                .anyMatch(i -> i.getInterviewer() != null
                                        && i.getInterviewer().getId().equals(sessionUser.getId())));

        if (!canDownload) {
            throw new AccessDeniedException("You do not have permission to download this CV.");
        }

        // Additional HR ownership check
        if (sessionUser.isHr()) {
            if (application.getJobPosting() == null ||
                    application.getJobPosting().getCreatedBy() == null ||
                    !application.getJobPosting().getCreatedBy().getId().equals(sessionUser.getId())) {
                throw new AccessDeniedException("You do not have permission to download this CV.");
            }
        }

        // Additional Candidate ownership check
        if (sessionUser.isCandidate()) {
            if (application.getCandidate() == null ||
                    application.getCandidate().getUser() == null ||
                    !application.getCandidate().getUser().getId().equals(sessionUser.getId())) {
                throw new AccessDeniedException("You do not have permission to download this CV.");
            }
        }

        String cvUrl = application.getCvFileUrl();
        if (cvUrl == null || cvUrl.trim().isEmpty()) {
            throw new NotFoundException("CV file not found for this application.");
        }

        // Generate dummy PDF byte stream for testing safely
        byte[] fileBytes;
        String filename = "cv_" + id + ".pdf";
        if (cvUrl.contains("/")) {
            filename = cvUrl.substring(cvUrl.lastIndexOf("/") + 1);
        }

        String fullName = (application.getCandidate() != null && application.getCandidate().getUser() != null)
                ? application.getCandidate().getUser().getFullName()
                : "Candidate";

        String dummyPdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [ 3 0 R ] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << >> /Contents 4 0 R >>\nendobj\n" +
                "4 0 obj\n<< /Length 120 >>\nstream\n" +
                "BT\n/F1 18 Tf\n50 700 Td\n(CV for Candidate: " + fullName + ") Tj\n" +
                "0 -30 Td\n(Application ID: " + id + ") Tj\n" +
                "0 -30 Td\n(Job Title: "
                + (application.getJobPosting() != null ? application.getJobPosting().getTitle() : "N/A") + ") Tj\n" +
                "ET\nstream\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000056 00000 n\n0000000111 00000 n\n0000000192 00000 n\ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n360\n%%EOF";

        fileBytes = dummyPdf.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(fileBytes.length);

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
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
            throw new AccessDeniedException("You do not have access to this application.");
        }
        if (sessionUser.isInterviewer()) {
            boolean isAssigned = application.getInterviews() != null &&
                    application.getInterviews().stream()
                            .anyMatch(i -> i.getInterviewer() != null
                                    && i.getInterviewer().getId().equals(sessionUser.getId()));
            if (isAssigned) {
                return;
            }
            throw new AccessDeniedException("You are not assigned to this application.");
        }
        if (sessionUser.isCandidate()) {
            if (application.getCandidate() != null &&
                    application.getCandidate().getUser() != null &&
                    application.getCandidate().getUser().getId().equals(sessionUser.getId())) {
                return;
            }
            throw new AccessDeniedException("You do not have access to this application.");
        }
        throw new AccessDeniedException("Access denied.");
    }
}
