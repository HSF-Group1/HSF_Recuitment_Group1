-- SQL Seed script to initialize the TalentHub database with demo records.
-- All user passwords default to 'Password123@' (BCrypt hashed below).

-- 1. Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'System administrator - manages users and configuration'),
(2, 'HR_MANAGER', 'HR Manager - manages job postings and the recruitment pipeline'),
(3, 'INTERVIEWER', 'Interviewer - evaluates assigned candidates'),
(4, 'CANDIDATE', 'Candidate - applies for jobs and tracks applications');

-- 2. Users (Password: Password123@)
INSERT INTO users (id, full_name, username, email, password_hash, role_id, status, created_at) VALUES
(1, 'Admin HSF', 'admin', 'admin@hsf.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 1, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(2, 'Nguyen Thi Huong', 'hr_huong', 'huong.hr@hsf.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 2, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(3, 'Nguyen Minh Hieu', 'hr_hieu', 'hieu.hr@hsf.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 2, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(4, 'Tran Van Khoa', 'khoa_iv', 'khoa.iv@hsf.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 3, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(5, 'Le Minh Tuan', 'tuan_iv', 'tuan.iv@hsf.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 3, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(6, 'Tran Thi Mai', 'mai_tran', 'mai.tran@gmail.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 4, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(7, 'Le Van Nam', 'nam_le', 'nam.le@gmail.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 4, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(8, 'Pham Hoang Long', 'long_pham', 'long.pham@gmail.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 4, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH)),
(9, 'Vo Thi Lan', 'lan_vo', 'lan.vo@gmail.com', '$2a$10$tM5iU80eK7D4N8h4F2w7Oe.a4B5o/4mQ6g4R5r7T3r2L4f8h1o8zG', 4, 'LOCKED', DATE_SUB(NOW(), INTERVAL 2 MONTH));

-- 3. Companies
INSERT INTO companies (id, name, industry, website_url, created_at) VALUES
(1, 'HSF Technology JSC', 'Information Technology', 'https://hsf-tech.example.com', DATE_SUB(NOW(), INTERVAL 6 MONTH));

-- 4. Company Profiles
INSERT INTO company_profiles (id, company_id, logo_url, description, location, company_size, benefits) VALUES
(1, 1, '/images/hsf-logo.png', 'HSF Technology is a software product company building recruitment and HR solutions for the Vietnamese market.', 'Ha Noi, Vietnam', '50-200', 'Competitive salary; 13th month bonus; Premium health insurance; Hybrid working; Annual company trip');

-- 5. Categories
INSERT INTO categories (id, name, description) VALUES
(1, 'Information Technology', 'Software, data and infrastructure roles'),
(2, 'Marketing', 'Digital marketing, content and branding'),
(3, 'Sales', 'Sales and business development'),
(4, 'Design', 'UI/UX and graphic design');

-- 6. Skills
INSERT INTO skills (id, name, category) VALUES
(1, 'Java', 'Hard skill'),
(2, 'Spring Boot', 'Framework'),
(3, 'React', 'Framework'),
(4, 'SQL', 'Hard skill'),
(5, 'Communication', 'Soft skill'),
(6, 'Figma', 'Tool');

-- 7. Candidates
INSERT INTO candidates (id, user_id) VALUES
(1, 6),
(2, 7),
(3, 8),
(4, 9);

-- 8. Candidate Profiles
INSERT INTO candidate_profiles (id, candidate_id, phone, address, summary, experience_years, education, github_url, linkedin_url, cv_url) VALUES
(1, 1, '0901234567', 'Cau Giay, Ha Noi', 'Backend developer with 3 years of Java/Spring experience.', 3, 'FPT University', 'https://github.com/maitran', 'https://linkedin.com/in/maitran', '/cv/mai_tran.pdf'),
(2, 2, '0902345678', 'Hai Ba Trung, Ha Noi', 'Frontend developer passionate about React and UX.', 2, 'Hanoi University of Science and Technology', 'https://github.com/namle', 'https://linkedin.com/in/namle', '/cv/nam_le.pdf'),
(3, 3, '0903456789', 'Dong Da, Ha Noi', 'Fresh graduate, strong fundamentals in Java and databases.', 0, 'National Economics University', 'https://github.com/longpham', NULL, '/cv/long_pham.pdf'),
(4, 4, '0904567890', 'Thanh Xuan, Ha Noi', 'UI/UX designer with an eye for clean, usable interfaces.', 4, 'University of Fine Arts', NULL, 'https://linkedin.com/in/lanvo', '/cv/lan_vo.pdf');

-- 9. Candidate Skills
INSERT INTO candidate_skills (id, candidate_id, skill_id, proficiency_level, years_of_experience) VALUES
(1, 1, 1, 'ADVANCED', 3),
(2, 1, 2, 'ADVANCED', 3),
(3, 1, 4, 'INTERMEDIATE', 3),
(4, 2, 3, 'ADVANCED', 2),
(5, 2, 5, 'INTERMEDIATE', 2),
(6, 3, 1, 'BEGINNER', 1),
(7, 3, 4, 'BEGINNER', 1),
(8, 4, 6, 'EXPERT', 4),
(9, 4, 5, 'ADVANCED', 4);

-- 10. Job Postings
INSERT INTO job_postings (id, company_id, category_id, title, department, location, description, requirements, salary_range, application_deadline, status, created_by_id, created_at) VALUES
(1, 1, 1, 'Java Backend Developer', 'Engineering', 'Ha Noi', 'Build and maintain REST APIs for the recruitment platform using Spring Boot.', '3+ years Java; Spring Boot; MySQL; REST API design.', '20-30M VND', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'ACTIVE', 3, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(2, 1, 1, 'Frontend React Developer', 'Engineering', 'Ha Noi', 'Develop responsive UIs with React and integrate with backend services.', '2+ years React; HTML/CSS; TypeScript is a plus.', '18-28M VND', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'ACTIVE', 2, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3, 1, 2, 'Digital Marketing Executive', 'Marketing', 'Ha Noi', 'Plan and run digital campaigns across social and search channels.', '1+ year digital marketing; SEO/SEM basics.', '12-18M VND', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'ACTIVE', 2, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, 1, 4, 'UI/UX Designer', 'Product', 'Remote', 'Design intuitive interfaces and prototypes for web and mobile.', 'Portfolio required; Figma proficiency.', '15-22M VND', NULL, 'DRAFT', 2, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, 1, 3, 'Sales Executive', 'Sales', 'Ho Chi Minh City', 'Drive B2B sales for HR software products.', 'Sales experience; strong communication.', 'Negotiable', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'CLOSED', 2, DATE_SUB(NOW(), INTERVAL 35 DAY));

-- 11. Applications
INSERT INTO applications (id, candidate_id, job_posting_id, submission_date, status, rejected_at_stage, cv_file_url, status_updated_at) VALUES
(1, 1, 1, DATE_SUB(NOW(), INTERVAL 9 DAY), 'INTERVIEW', NULL, '/cv/mai_tran.pdf', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2, 2, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), 'SCREENING', NULL, '/cv/nam_le.pdf', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 3, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'APPLIED', NULL, '/cv/long_pham.pdf', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 1, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 'APPLIED', NULL, '/cv/mai_tran.pdf', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 2, 3, DATE_SUB(NOW(), INTERVAL 12 DAY), 'OFFER', NULL, '/cv/nam_le.pdf', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(6, 4, 2, DATE_SUB(NOW(), INTERVAL 20 DAY), 'HIRED', NULL, '/cv/lan_vo.pdf', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(7, 3, 3, DATE_SUB(NOW(), INTERVAL 11 DAY), 'REJECTED', 'SCREENING', '/cv/long_pham.pdf', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(8, 4, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), 'WITHDRAWN', NULL, '/cv/lan_vo.pdf', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- 12. Interviews
INSERT INTO interviews (id, application_id, interviewer_id, interview_date, interview_time, location_or_link, status, notes) VALUES
(1, 1, 4, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', 'Meeting room A / https://meet.example.com/abc', 'SCHEDULED', NULL),
(2, 5, 5, DATE_SUB(CURDATE(), INTERVAL 6 DAY), '14:30:00', 'https://meet.example.com/xyz', 'EVALUATED', NULL),
(3, 6, 4, DATE_SUB(CURDATE(), INTERVAL 18 DAY), '09:00:00', 'Meeting room B', 'EVALUATED', NULL);

-- 13. Evaluations
INSERT INTO evaluations (id, interview_id, rating, feedback, submitted_at) VALUES
(1, 2, 4, 'Strong marketing instincts and good communication. Recommend moving to offer.', DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 1 HOUR)),
(2, 3, 5, 'Excellent portfolio and design thinking. Clear hire.', DATE_ADD(DATE_SUB(NOW(), INTERVAL 18 DAY), INTERVAL 1 HOUR));

-- 14. Internal Notes
INSERT INTO internal_notes (id, application_id, author_id, content, created_at) VALUES
(1, 1, 2, 'Good backend fundamentals. Scheduled a technical interview with Khoa.', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 2, 'CV looks promising, screening call went well. Consider for interview.', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 5, 2, 'Approved offer at 16M VND. Awaiting candidate response.', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 15. Activity Logs
INSERT INTO activity_logs (id, user_id, event_type, description, ip_address, timestamp) VALUES
(1, 1, 'SIGN_IN_SUCCESS', 'Admin signed in', '192.168.1.10', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 1, 'ACCOUNT_CREATED', 'Created HR account: hr_huong', '192.168.1.10', DATE_SUB(NOW(), INTERVAL 3 MONTH)),
(3, 1, 'ACCOUNT_LOCKED', 'Account locked after failed attempts: lan_vo', '192.168.1.10', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 2, 'SIGN_IN_SUCCESS', 'HR signed in', '192.168.1.22', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 2, 'APPLICATION_STATUS_CHANGED', 'Application #app1 moved to INTERVIEW', '192.168.1.22', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 5, 'EVALUATION_SUBMITTED', 'Evaluation submitted for application #app5', '192.168.1.33', DATE_SUB(NOW(), INTERVAL 6 DAY));
