package com.group1.recruitment.config;

import com.group1.recruitment.entity.Candidate;
import com.group1.recruitment.entity.Role;
import com.group1.recruitment.entity.User;
import com.group1.recruitment.enums.AccountStatus;
import com.group1.recruitment.repository.CandidateRepository;
import com.group1.recruitment.repository.RoleRepository;
import com.group1.recruitment.repository.UserRepository;
import com.group1.recruitment.security.SessionUser;
import com.group1.recruitment.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the four roles and a set of demo accounts on startup so the auth
 * screens can be exercised immediately. Idempotent: roles are ensured on every
 * boot, demo users are only created when the users table is empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Password123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordUtil passwordUtil;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository,
                      CandidateRepository candidateRepository, PasswordUtil passwordUtil) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.passwordUtil = passwordUtil;
    }

    @Override
    public void run(String... args) {
        Role admin = ensureRole(SessionUser.ADMIN, "System administrator");
        Role hr = ensureRole(SessionUser.HR_MANAGER, "HR manager");
        Role interviewer = ensureRole(SessionUser.INTERVIEWER, "Interviewer");
        Role candidate = ensureRole(SessionUser.CANDIDATE, "Job candidate");

        if (userRepository.count() > 0) {
            return; // demo users already seeded
        }

        createStaff("Admin User", "admin", "admin@hsf.com", admin, AccountStatus.ACTIVE);
        createStaff("Huong Nguyen", "hr_huong", "huong.hr@hsf.com", hr, AccountStatus.ACTIVE);
        createStaff("Khoa Le", "khoa_iv", "khoa.iv@hsf.com", interviewer, AccountStatus.ACTIVE);
        createCandidate("Mai Tran", "mai_tran", "mai.tran@gmail.com", candidate, AccountStatus.ACTIVE);
        createCandidate("Lan Vo", "lan_vo", "lan.vo@gmail.com", candidate, AccountStatus.LOCKED);
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }

    private User createStaff(String fullName, String username, String email, Role role, AccountStatus status) {
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordUtil.hash(DEMO_PASSWORD));
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private void createCandidate(String fullName, String username, String email, Role role, AccountStatus status) {
        User user = createStaff(fullName, username, email, role, status);
        Candidate candidate = new Candidate();
        candidate.setUser(user);
        candidateRepository.save(candidate);
    }
}
