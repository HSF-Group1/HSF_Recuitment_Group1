package com.group1.recruitment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.group1.recruitment.enums.AccountStatus;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user")
    private Candidate candidate;

    @OneToMany(mappedBy = "createdBy")
    private List<JobPosting> createdJobs;

    @OneToMany(mappedBy = "interviewer")
    private List<Interview> assignedInterviews;

    @OneToMany(mappedBy = "author")
    private List<InternalNote> internalNotes;

    @OneToMany(mappedBy = "user")
    private List<ActivityLog> activityLogs;

    // No-args constructor
    public User() {
    }

    // All-args constructor
    public User(Long id, String fullName, String username, String email, String passwordHash, Role role, AccountStatus status, LocalDateTime createdAt, Candidate candidate, List<JobPosting> createdJobs, List<Interview> assignedInterviews, List<InternalNote> internalNotes, List<ActivityLog> activityLogs) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.candidate = candidate;
        this.createdJobs = createdJobs;
        this.assignedInterviews = assignedInterviews;
        this.internalNotes = internalNotes;
        this.activityLogs = activityLogs;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public List<JobPosting> getCreatedJobs() {
        return createdJobs;
    }

    public void setCreatedJobs(List<JobPosting> createdJobs) {
        this.createdJobs = createdJobs;
    }

    public List<Interview> getAssignedInterviews() {
        return assignedInterviews;
    }

    public void setAssignedInterviews(List<Interview> assignedInterviews) {
        this.assignedInterviews = assignedInterviews;
    }

    public List<InternalNote> getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(List<InternalNote> internalNotes) {
        this.internalNotes = internalNotes;
    }

    public List<ActivityLog> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(List<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
    }
}
