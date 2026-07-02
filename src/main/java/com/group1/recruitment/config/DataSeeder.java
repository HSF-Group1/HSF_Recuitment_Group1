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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Seeds the four roles and demo accounts so the auth screens can be tested
 * right after a clean clone. All demo passwords are "Password123" (hashed
 * with PasswordUtil — nothing is stored in plaintext). Runs only once: it
 * skips seeding when any user already exists.
 */
@Component
public class DataSeeder implements CommandLineRunner {

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

    public static final String DEMO_PASSWORD = "Password123";

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = role(SessionUser.ROLE_ADMIN, "System administrator");
        Role hr = role(SessionUser.ROLE_HR, "HR manager");
        Role interviewer = role(SessionUser.ROLE_INTERVIEWER, "Interviewer");
        Role candidate = role(SessionUser.ROLE_CANDIDATE, "Candidate");

        if (userRepository.count() > 0) {
            return; // already seeded
        }

        user("Admin HSF", "admin", "admin@hsf.com", admin, AccountStatus.ACTIVE);
        user("Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", hr, AccountStatus.ACTIVE);
        user("Tran Van Khoa", "khoa_iv", "khoa.iv@hsf.com", interviewer, AccountStatus.ACTIVE);
        candidate(user("Tran Thi Mai", "mai_tran", "mai.tran@gmail.com", candidate, AccountStatus.ACTIVE));
        candidate(user("Vo Thi Lan", "lan_vo", "lan.vo@gmail.com", candidate, AccountStatus.LOCKED));
    }

    private Role role(String name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            r.setDescription(description);
            return roleRepository.save(r);
        });
    }

    private User user(String fullName, String username, String email, Role role, AccountStatus status) {
        User u = new User();
        u.setFullName(fullName);
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(passwordUtil.hash(DEMO_PASSWORD));
        u.setRole(role);
        u.setStatus(status);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private void candidate(User user) {
        Candidate c = new Candidate();
        c.setUser(user);
        candidateRepository.save(c);
    }
}
