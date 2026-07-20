package com.group1.recruitment.service;

import com.group1.recruitment.entity.Application;
import com.group1.recruitment.entity.InternalNote;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.repository.ApplicationRepository;
import com.group1.recruitment.repository.InternalNoteRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InternalNoteService {

    private final InternalNoteRepository internalNoteRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public InternalNoteService(InternalNoteRepository internalNoteRepository,
                               ApplicationRepository applicationRepository,
                               UserRepository userRepository) {
        this.internalNoteRepository = internalNoteRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<InternalNote> getNotesForApplication(Long applicationId) {
        return internalNoteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }

    @Transactional
    public InternalNote addNote(Long applicationId, String content, SessionUser actor) {
        if (actor == null) {
            throw new AccessDeniedException("User must be logged in.");
        }
        if (!actor.isHrOrAdmin()) {
            throw new AccessDeniedException("Only Admin or HR Managers can add internal notes.");
        }

        Application application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found with ID: " + applicationId));

        // Ownership / security check
        if (actor.isHr()) {
            if (application.getJobPosting() == null ||
                    application.getJobPosting().getCreatedBy() == null ||
                    !application.getJobPosting().getCreatedBy().getId().equals(actor.getId())) {
                throw new AccessDeniedException("You do not have permission to add notes to this application.");
            }
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Note content cannot be empty.");
        }

        User author = userRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + actor.getId()));

        InternalNote note = new InternalNote();
        note.setApplication(application);
        note.setAuthor(author);
        note.setContent(content.trim());
        note.setCreatedAt(LocalDateTime.now());

        return internalNoteRepository.save(note);
    }
}
