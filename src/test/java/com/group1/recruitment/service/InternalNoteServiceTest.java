package com.group1.recruitment.service;

import com.group1.recruitment.entity.InternalNote;
import com.group1.recruitment.exception.AccessDeniedException;
import com.group1.recruitment.exception.NotFoundException;
import com.group1.recruitment.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InternalNoteServiceTest {

    @Autowired
    private InternalNoteService internalNoteService;

    @Autowired
    private ApplicationService applicationService;

    @Test
    void testGetNotesForApplicationSuccess() {
        // App 1 has seeded notes
        List<InternalNote> notes = internalNoteService.getNotesForApplication(1L);
        assertNotNull(notes);
        assertFalse(notes.isEmpty());
        // Verify chronological order (newest first - if multiple)
        if (notes.size() > 1) {
            assertTrue(notes.get(0).getCreatedAt().isAfter(notes.get(1).getCreatedAt())
                    || notes.get(0).getCreatedAt().isEqual(notes.get(1).getCreatedAt()));
        }
    }

    @Test
    void testAddNoteSuccessByHr() {
        // App 1 is associated with Job 1 (created by HR manager 2 'hr_hieu', ID 3)
        SessionUser hrOwner = new SessionUser(3L, "Nguyen Minh Hieu", "hr_hieu", "hieu.hr@hsf.com", "HR_MANAGER");

        int initialSize = internalNoteService.getNotesForApplication(1L).size();

        InternalNote note = internalNoteService.addNote(1L, "This is a new internal note by HR owner.", hrOwner);

        assertNotNull(note);
        assertEquals("This is a new internal note by HR owner.", note.getContent());
        assertEquals(3L, note.getAuthor().getId());

        List<InternalNote> updatedNotes = internalNoteService.getNotesForApplication(1L);
        assertEquals(initialSize + 1, updatedNotes.size());
        assertEquals("This is a new internal note by HR owner.", updatedNotes.get(0).getContent());
    }

    @Test
    void testAddNoteSuccessByAdmin() {
        // Admin (ID 1) should be able to add notes to any application
        SessionUser admin = new SessionUser(1L, "Admin HSF", "admin", "admin@hsf.com", "ADMIN");

        int initialSize = internalNoteService.getNotesForApplication(2L).size();

        InternalNote note = internalNoteService.addNote(2L, "Admin note.", admin);

        assertNotNull(note);
        assertEquals("Admin note.", note.getContent());

        List<InternalNote> updatedNotes = internalNoteService.getNotesForApplication(2L);
        assertEquals(initialSize + 1, updatedNotes.size());
        assertEquals("Admin note.", updatedNotes.get(0).getContent());
    }

    @Test
    void testAddNoteFailByOtherHr() {
        // App 1 is associated with Job 1 (created by HR manager 'hr_hieu', ID 3)
        // HR manager 'hr_huong' (ID 2) is a different HR manager and does not own the
        // job posting
        SessionUser otherHr = new SessionUser(2L, "Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", "HR_MANAGER");

        assertThrows(AccessDeniedException.class, () -> {
            internalNoteService.addNote(1L, "Note by unauthorized HR.", otherHr);
        });
    }

    @Test
    void testAddNoteFailByInterviewer() {
        SessionUser interviewer = new SessionUser(4L, "Tran Van Khoa", "khoa_iv", "khoa.iv@hsf.com", "INTERVIEWER");

        assertThrows(AccessDeniedException.class, () -> {
            internalNoteService.addNote(1L, "Interviewer trying to add note.", interviewer);
        });
    }

    @Test
    void testAddNoteFailByCandidate() {
        SessionUser candidate = new SessionUser(6L, "Tran Thi Mai", "mai_tran", "mai.tran@gmail.com", "CANDIDATE");

        assertThrows(AccessDeniedException.class, () -> {
            internalNoteService.addNote(1L, "Candidate trying to add note.", candidate);
        });
    }

    @Test
    void testAddNoteFailEmptyContent() {
        SessionUser admin = new SessionUser(1L, "Admin HSF", "admin", "admin@hsf.com", "ADMIN");

        assertThrows(IllegalArgumentException.class, () -> {
            internalNoteService.addNote(1L, "   ", admin);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            internalNoteService.addNote(1L, null, admin);
        });
    }

    @Test
    void testAddNoteFailApplicationNotFound() {
        SessionUser admin = new SessionUser(1L, "Admin HSF", "admin", "admin@hsf.com", "ADMIN");

        assertThrows(NotFoundException.class, () -> {
            internalNoteService.addNote(999L, "Note content", admin);
        });
    }
}
