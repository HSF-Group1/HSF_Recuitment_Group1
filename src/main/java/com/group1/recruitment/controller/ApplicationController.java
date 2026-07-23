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

        String jobTitle = (application != null && application.getJobPosting() != null)
                ? application.getJobPosting().getTitle()
                : "N/A";

        // Định nghĩa nội dung hiển thị trong Stream của PDF
        String pdfContent = "BT\n" +
                "/F1 20 Tf 50 770 Td (" + fullName + ") Tj\n" +
                "/F1 12 Tf 0 -22 Td (Job Position: " + jobTitle + " | Application ID: " + id + ") Tj\n" +
                "0 -15 Td (Email: candidate@example.com | Phone: +84 123 456 789) Tj\n" +
                "0 -25 Td (____________________________________________________________________) Tj\n" +

                "0 -30 Td /F1 14 Tf (SUMMARY) Tj\n" +
                "0 -18 Td /F1 10 Tf (Motivated and detail-oriented professional with experience in software development) Tj\n"
                +
                "0 -14 Td (and system architecture. Passionate about building scalable applications and learning new tech.) Tj\n"
                +

                "0 -28 Td /F1 14 Tf (WORK EXPERIENCE) Tj\n" +
                "0 -18 Td /F1 11 Tf (Software Engineer | Tech Company) Tj\n" +
                "0 -14 Td /F1 10 Tf (2023 - Present) Tj\n" +
                "0 -14 Td (- Developed RESTful APIs using Spring Boot and Java.) Tj\n" +
                "0 -14 Td (- Managed database schemas, triggers, and optimized SQL queries.) Tj\n" +
                "0 -14 Td (- Integrated WebSocket and OAuth2 for secure real-time communication.) Tj\n" +

                "0 -25 Td /F1 11 Tf (Junior Developer | Software Solutions) Tj\n" +
                "0 -14 Td /F1 10 Tf (2022 - 2023) Tj\n" +
                "0 -14 Td (- Collaborated with cross-functional teams to build responsive web interfaces.) Tj\n" +
                "0 -14 Td (- Assisted in microservices migration and unit testing.) Tj\n" +

                "0 -28 Td /F1 14 Tf (EDUCATION) Tj\n" +
                "0 -18 Td /F1 11 Tf (Bachelor of Science in Computer Science) Tj\n" +
                "0 -14 Td /F1 10 Tf (University of Technology | 2019 - 2023) Tj\n" +

                "0 -28 Td /F1 14 Tf (SKILLS) Tj\n" +
                "0 -18 Td /F1 10 Tf (Languages: Java, JavaScript, SQL, C++) Tj\n" +
                "0 -14 Td (Frameworks: Spring Boot, React, Vite, Bootstrap) Tj\n" +
                "0 -14 Td (Tools & Databases: Git, PostgreSQL, Docker, WebSocket) Tj\n" +
                "ET\n";

        int streamLength = pdfContent.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        // Khai báo đối tượng PDF chuẩn
        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [ 3 0 R ] /Count 1 >>\nendobj\n";
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> /Contents 4 0 R >>\nendobj\n";
        String obj4Header = "4 0 obj\n<< /Length " + streamLength + " >>\nstream\n";
        String obj4End = "endstream\nendobj\n";

        // Tính toán Offset vị trí từng Object trong file PDF (Bắt buộc chuẩn để PDF
        // không hỏng)
        int offset1 = 9; // Sau "%PDF-1.4\n"
        int offset2 = offset1 + obj1.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int offset3 = offset2 + obj2.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int offset4 = offset3 + obj3.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        String fullStream = obj4Header + pdfContent + obj4End;
        int startXref = offset4 + fullStream.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;

        String xrefAndTrailer = String.format(
                "xref\n0 5\n0000000000 65535 f\n%010d 00000 n\n%010d 00000 n\n%010d 00000 n\n%010d 00000 n\n" +
                        "trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n%d\n%%EOF",
                offset1, offset2, offset3, offset4, startXref);

        // Chuỗi PDF hoàn chỉnh
        String dummyPdf = "%PDF-1.4\n" + obj1 + obj2 + obj3 + fullStream + xrefAndTrailer;

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
