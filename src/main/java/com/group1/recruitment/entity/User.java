package com.group1.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.group1.recruitment.enums.AccountStatus;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
