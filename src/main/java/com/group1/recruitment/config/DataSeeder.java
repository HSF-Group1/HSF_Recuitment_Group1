package com.group1.recruitment.config;

import com.group1.recruitment.entity.*;
import com.group1.recruitment.enums.*;
import com.group1.recruitment.security.PasswordUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataSeeder implements CommandLineRunner {

        private final PasswordUtil passwordUtil;
        private final String password;

        public DataSeeder(PasswordUtil passwordUtil) {
                this.passwordUtil = passwordUtil;
                this.password = passwordUtil.hash("password123");
        }

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public void run(String... args) {
                Long userCount = em.createQuery("select count(u) from User u", Long.class).getSingleResult();
                if (userCount > 0) {
                        System.out.println(
                                        "[DataSeeder] Users already present (" + userCount + ") - skipping demo seed.");
                        return;
                }
                System.out.println("[DataSeeder] Empty database detected - seeding demo data...");

                // ---------- Roles ----------
                Role roleAdmin = role("ROLE_ADMIN", "System administrator - manages users and configuration");
                Role roleHr = role("ROLE_HR", "HR Manager - manages job postings and the recruitment pipeline");
                Role roleInterviewer = role("ROLE_INTERVIEWER", "Interviewer - evaluates assigned candidates");
                Role roleCandidate = role("ROLE_CANDIDATE", "Candidate - applies for jobs and tracks applications");

                // ---------- Users ----------
                User admin = user("Admin HSF", "admin", "admin@hsf.com", roleAdmin, AccountStatus.ACTIVE);
                User hr = user("Nguyen Thi Huong", "hr_huong", "huong.hr@hsf.com", roleHr, AccountStatus.ACTIVE);
                User interviewer1 = user("Tran Van Khoa", "khoa_iv", "khoa.iv@hsf.com", roleInterviewer,
                                AccountStatus.ACTIVE);
                User interviewer2 = user("Le Minh Tuan", "tuan_iv", "tuan.iv@hsf.com", roleInterviewer,
                                AccountStatus.ACTIVE);

                User candUser1 = user("Tran Thi Mai", "mai_tran", "mai.tran@gmail.com", roleCandidate,
                                AccountStatus.ACTIVE);
                User candUser2 = user("Le Van Nam", "nam_le", "nam.le@gmail.com", roleCandidate, AccountStatus.ACTIVE);
                User candUser3 = user("Pham Hoang Long", "long_pham", "long.pham@gmail.com", roleCandidate,
                                AccountStatus.ACTIVE);
                User candUser4 = user("Vo Thi Lan", "lan_vo", "lan.vo@gmail.com", roleCandidate, AccountStatus.LOCKED); // demo
                                                                                                                        // locked
                                                                                                                        // account

                // ---------- Company + profile ----------
                Company company = new Company();
                company.setName("HSF Technology JSC");
                company.setIndustry("Information Technology");
                company.setWebsiteUrl("https://hsf-tech.example.com");
                company.setCreatedAt(LocalDateTime.now().minusMonths(6));
                em.persist(company);

                CompanyProfile companyProfile = new CompanyProfile();
                companyProfile.setCompany(company);
                companyProfile.setLogoUrl("/images/hsf-logo.png");
                companyProfile.setDescription(
                                "HSF Technology is a software product company building recruitment and HR solutions for the Vietnamese market.");
                companyProfile.setLocation("Ha Noi, Vietnam");
                companyProfile.setCompanySize("50-200");
                companyProfile.setBenefits(
                                "Competitive salary; 13th month bonus; Premium health insurance; Hybrid working; Annual company trip");
                em.persist(companyProfile);

                // ---------- Categories ----------
                Category catIt = category("Information Technology", "Software, data and infrastructure roles");
                Category catMarketing = category("Marketing", "Digital marketing, content and branding");
                Category catSales = category("Sales", "Sales and business development");
                Category catDesign = category("Design", "UI/UX and graphic design");

                // ---------- Skills ----------
                Skill sJava = skill("Java", "Hard skill");
                Skill sSpring = skill("Spring Boot", "Framework");
                Skill sReact = skill("React", "Framework");
                Skill sSql = skill("SQL", "Hard skill");
                Skill sComm = skill("Communication", "Soft skill");
                Skill sFigma = skill("Figma", "Tool");

                // ---------- Candidates + profiles + skills ----------
                Candidate cand1 = candidate(candUser1);
                candidateProfile(cand1, "0901234567", "Cau Giay, Ha Noi",
                                "Backend developer with 3 years of Java/Spring experience.",
                                3, "FPT University", "https://github.com/maitran", "https://linkedin.com/in/maitran",
                                "/cv/mai_tran.pdf");
                candidateSkill(cand1, sJava, ProficiencyLevel.ADVANCED, 3);
                candidateSkill(cand1, sSpring, ProficiencyLevel.ADVANCED, 3);
                candidateSkill(cand1, sSql, ProficiencyLevel.INTERMEDIATE, 3);

                Candidate cand2 = candidate(candUser2);
                candidateProfile(cand2, "0902345678", "Hai Ba Trung, Ha Noi",
                                "Frontend developer passionate about React and UX.",
                                2, "Hanoi University of Science and Technology", "https://github.com/namle",
                                "https://linkedin.com/in/namle", "/cv/nam_le.pdf");
                candidateSkill(cand2, sReact, ProficiencyLevel.ADVANCED, 2);
                candidateSkill(cand2, sComm, ProficiencyLevel.INTERMEDIATE, 2);

                Candidate cand3 = candidate(candUser3);
                candidateProfile(cand3, "0903456789", "Dong Da, Ha Noi",
                                "Fresh graduate, strong fundamentals in Java and databases.",
                                0, "National Economics University", "https://github.com/longpham", null,
                                "/cv/long_pham.pdf");
                candidateSkill(cand3, sJava, ProficiencyLevel.BEGINNER, 1);
                candidateSkill(cand3, sSql, ProficiencyLevel.BEGINNER, 1);

                Candidate cand4 = candidate(candUser4);
                candidateProfile(cand4, "0904567890", "Thanh Xuan, Ha Noi",
                                "UI/UX designer with an eye for clean, usable interfaces.",
                                4, "University of Fine Arts", null, "https://linkedin.com/in/lanvo", "/cv/lan_vo.pdf");
                candidateSkill(cand4, sFigma, ProficiencyLevel.EXPERT, 4);
                candidateSkill(cand4, sComm, ProficiencyLevel.ADVANCED, 4);

                // ---------- Job postings (all statuses) ----------
                JobPosting job1 = job(company, catIt, hr, "Java Backend Developer", "Engineering", "Ha Noi",
                                "Build and maintain REST APIs for the recruitment platform using Spring Boot.",
                                "3+ years Java; Spring Boot; MySQL; REST API design.", "20-30M VND",
                                LocalDate.now().plusDays(20), JobStatus.ACTIVE, 40);

                JobPosting job2 = job(company, catIt, hr, "Frontend React Developer", "Engineering", "Ha Noi",
                                "Develop responsive UIs with React and integrate with backend services.",
                                "2+ years React; HTML/CSS; TypeScript is a plus.", "18-28M VND",
                                LocalDate.now().plusDays(15), JobStatus.ACTIVE, 30);

                JobPosting job3 = job(company, catMarketing, hr, "Digital Marketing Executive", "Marketing", "Ha Noi",
                                "Plan and run digital campaigns across social and search channels.",
                                "1+ year digital marketing; SEO/SEM basics.", "12-18M VND",
                                LocalDate.now().plusDays(10), JobStatus.ACTIVE, 25);

                JobPosting job4 = job(company, catDesign, hr, "UI/UX Designer", "Product", "Remote",
                                "Design intuitive interfaces and prototypes for web and mobile.",
                                "Portfolio required; Figma proficiency.", "15-22M VND",
                                null, JobStatus.DRAFT, 20); // draft, no deadline

                JobPosting job5 = job(company, catSales, hr, "Sales Executive", "Sales", "Ho Chi Minh City",
                                "Drive B2B sales for HR software products.",
                                "Sales experience; strong communication.", "Negotiable",
                                LocalDate.now().minusDays(5), JobStatus.CLOSED, 35);

                // ---------- Applications (full pipeline coverage) ----------
                Application app1 = application(cand1, job1, ApplicationStatus.INTERVIEW,
                                LocalDateTime.now().minusDays(9),
                                "/cv/mai_tran.pdf");
                Application app2 = application(cand2, job1, ApplicationStatus.SCREENING,
                                LocalDateTime.now().minusDays(7),
                                "/cv/nam_le.pdf");
                Application app3 = application(cand3, job1, ApplicationStatus.APPLIED, LocalDateTime.now().minusDays(2),
                                "/cv/long_pham.pdf");
                Application app4 = application(cand1, job2, ApplicationStatus.APPLIED, LocalDateTime.now().minusDays(3),
                                "/cv/mai_tran.pdf");
                Application app5 = application(cand2, job3, ApplicationStatus.OFFER, LocalDateTime.now().minusDays(12),
                                "/cv/nam_le.pdf");
                Application app6 = application(cand4, job2, ApplicationStatus.HIRED, LocalDateTime.now().minusDays(20),
                                "/cv/lan_vo.pdf");
                Application app7 = application(cand3, job3, ApplicationStatus.REJECTED,
                                LocalDateTime.now().minusDays(11),
                                "/cv/long_pham.pdf");
                Application app8 = application(cand4, job1, ApplicationStatus.WITHDRAWN,
                                LocalDateTime.now().minusDays(8),
                                "/cv/lan_vo.pdf");

                // ---------- Interviews + evaluations ----------
                // app1 -> scheduled interview in the future (not yet evaluated)
                interview(app1, interviewer1, LocalDate.now().plusDays(3), LocalTime.of(10, 0),
                                "Meeting room A / https://meet.example.com/abc", InterviewStatus.SCHEDULED, null);

                // app5 -> evaluated interview (candidate reached OFFER)
                Interview iv5 = interview(app5, interviewer2, LocalDate.now().minusDays(6), LocalTime.of(14, 30),
                                "https://meet.example.com/xyz", InterviewStatus.EVALUATED, null);
                evaluation(iv5, 4, "Strong marketing instincts and good communication. Recommend moving to offer.",
                                LocalDateTime.now().minusDays(6).plusHours(1));

                // app6 -> evaluated interview (candidate hired)
                Interview iv6 = interview(app6, interviewer1, LocalDate.now().minusDays(18), LocalTime.of(9, 0),
                                "Meeting room B", InterviewStatus.EVALUATED, null);
                evaluation(iv6, 5, "Excellent portfolio and design thinking. Clear hire.",
                                LocalDateTime.now().minusDays(18).plusHours(1));

                // ---------- Internal notes ----------
                internalNote(app1, hr, "Good backend fundamentals. Scheduled a technical interview with Khoa.",
                                LocalDateTime.now().minusDays(5));
                internalNote(app2, hr, "CV looks promising, screening call went well. Consider for interview.",
                                LocalDateTime.now().minusDays(6));
                internalNote(app5, hr, "Approved offer at 16M VND. Awaiting candidate response.",
                                LocalDateTime.now().minusDays(5));

                // ---------- Activity logs ----------
                activityLog(admin, EventType.SIGN_IN_SUCCESS, "Admin signed in", "192.168.1.10",
                                LocalDateTime.now().minusDays(1));
                activityLog(admin, EventType.ACCOUNT_CREATED, "Created HR account: hr_huong", "192.168.1.10",
                                LocalDateTime.now().minusMonths(3));
                activityLog(admin, EventType.ACCOUNT_LOCKED, "Account locked after failed attempts: lan_vo",
                                "192.168.1.10",
                                LocalDateTime.now().minusDays(2));
                activityLog(hr, EventType.SIGN_IN_SUCCESS, "HR signed in", "192.168.1.22",
                                LocalDateTime.now().minusDays(1));
                activityLog(hr, EventType.APPLICATION_STATUS_CHANGED, "Application #app1 moved to INTERVIEW",
                                "192.168.1.22",
                                LocalDateTime.now().minusDays(5));
                activityLog(interviewer2, EventType.EVALUATION_SUBMITTED, "Evaluation submitted for application #app5",
                                "192.168.1.33", LocalDateTime.now().minusDays(6));

                em.flush();
                System.out.println(
                                "[DataSeeder] Demo data seeded successfully. Login password for all demo users: password123");
        }

        // ---------------- helper methods ----------------

        private Role role(String name, String description) {
                Role r = new Role();
                r.setName(name);
                r.setDescription(description);
                em.persist(r);
                return r;
        }

        private User user(String fullName, String username, String email, Role role, AccountStatus status) {
                User u = new User();
                u.setFullName(fullName);
                u.setUsername(username);
                u.setEmail(email);
                u.setPasswordHash(password);
                u.setRole(role);
                u.setStatus(status);
                u.setCreatedAt(LocalDateTime.now().minusMonths(2));
                em.persist(u);
                return u;
        }

        private Category category(String name, String description) {
                Category c = new Category();
                c.setName(name);
                c.setDescription(description);
                em.persist(c);
                return c;
        }

        private Skill skill(String name, String category) {
                Skill s = new Skill();
                s.setName(name);
                s.setCategory(category);
                em.persist(s);
                return s;
        }

        private Candidate candidate(User user) {
                Candidate c = new Candidate();
                c.setUser(user);
                em.persist(c);
                return c;
        }

        private CandidateProfile candidateProfile(Candidate candidate, String phone, String address, String summary,
                        Integer experienceYears, String education, String githubUrl,
                        String linkedinUrl, String cvUrl) {
                CandidateProfile p = new CandidateProfile();
                p.setCandidate(candidate);
                p.setPhone(phone);
                p.setAddress(address);
                p.setSummary(summary);
                p.setExperienceYears(experienceYears);
                p.setEducation(education);
                p.setGithubUrl(githubUrl);
                p.setLinkedinUrl(linkedinUrl);
                p.setCvUrl(cvUrl);
                em.persist(p);
                return p;
        }

        private CandidateSkill candidateSkill(Candidate candidate, Skill skill, ProficiencyLevel level, Integer years) {
                CandidateSkill cs = new CandidateSkill();
                cs.setCandidate(candidate);
                cs.setSkill(skill);
                cs.setProficiencyLevel(level);
                cs.setYearsOfExperience(years);
                em.persist(cs);
                return cs;
        }

        private JobPosting job(Company company, Category category, User createdBy, String title, String department,
                        String location, String description, String requirements, String salaryRange,
                        LocalDate deadline, JobStatus status, int createdDaysAgo) {
                JobPosting j = new JobPosting();
                j.setCompany(company);
                j.setCategory(category);
                j.setCreatedBy(createdBy);
                j.setTitle(title);
                j.setDepartment(department);
                j.setLocation(location);
                j.setDescription(description);
                j.setRequirements(requirements);
                j.setSalaryRange(salaryRange);
                j.setApplicationDeadline(deadline);
                j.setStatus(status);
                j.setCreatedAt(LocalDateTime.now().minusDays(createdDaysAgo));
                em.persist(j);
                return j;
        }

        private Application application(Candidate candidate, JobPosting job, ApplicationStatus status,
                        LocalDateTime submittedAt, String cvUrl) {
                Application a = new Application();
                a.setCandidate(candidate);
                a.setJobPosting(job);
                a.setStatus(status);
                a.setSubmissionDate(submittedAt);
                a.setCvFileUrl(cvUrl);
                em.persist(a);
                return a;
        }

        private Interview interview(Application application, User interviewer, LocalDate date, LocalTime time,
                        String locationOrLink, InterviewStatus status, Evaluation evaluation) {
                Interview i = new Interview();
                i.setApplication(application);
                i.setInterviewer(interviewer);
                i.setInterviewDate(date);
                i.setInterviewTime(time);
                i.setLocationOrLink(locationOrLink);
                i.setStatus(status);
                i.setEvaluation(evaluation);
                em.persist(i);
                return i;
        }

        private Evaluation evaluation(Interview interview, int rating, String feedback, LocalDateTime submittedAt) {
                Evaluation e = new Evaluation();
                e.setInterview(interview);
                e.setRating(rating);
                e.setFeedback(feedback);
                e.setSubmittedAt(submittedAt);
                em.persist(e);
                return e;
        }

        private InternalNote internalNote(Application application, User author, String content,
                        LocalDateTime createdAt) {
                InternalNote n = new InternalNote();
                n.setApplication(application);
                n.setAuthor(author);
                n.setContent(content);
                n.setCreatedAt(createdAt);
                em.persist(n);
                return n;
        }

        private ActivityLog activityLog(User user, EventType eventType, String description, String ip,
                        LocalDateTime ts) {
                ActivityLog log = new ActivityLog();
                log.setUser(user);
                log.setEventType(eventType);
                log.setDescription(description);
                log.setIpAddress(ip);
                log.setTimestamp(ts);
                em.persist(log);
                return log;
        }
}
