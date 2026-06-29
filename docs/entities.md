## 1. Entities

### 1.1 Role (Phân quyền)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `name`: Tên role (Ví dụ: ROLE_ADMIN, ROLE_HR, ROLE_INTERVIEWER, ROLE_CANDIDATE).
  - `description`: Mô tả vai trò.
- **Mối quan hệ:**
  - Có thể được gán cho nhiều `User`.

### 1.2 User (Tài khoản người dùng chung)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `full_name`: Họ và tên (Bắt buộc).
  - `username`: Tên đăng nhập (Bắt buộc, duy nhất).
  - `email`: Địa chỉ email (Bắt buộc, duy nhất).
  - `password_hash`: Mật khẩu (đã mã hóa).
  - `role_id`: Khóa ngoại tham chiếu đến bảng `Role`.
  - `status`: Trạng thái tài khoản (Enum: Active, Locked, Inactive).
  - `created_at`: Ngày tạo.
- **Mối quan hệ:**
  - Thuộc về một `Role`.
  - Có thể có một `Candidate` (nếu là ứng viên).
  - Một `User (HR Manager)` có thể tạo nhiều `JobPosting` và `Company`.
  - Một `User (Interviewer)` có thể được phân công nhiều `Interview`.
  - Một `User (Admin/HR)` có thể tạo nhiều `Internal Note`.
  - Một `User` có thể có nhiều `Activity Log`.

### 1.3 Candidate (Ứng viên)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `user_id`: Khóa ngoại tham chiếu đến bảng `User` (1-1).
- **Mối quan hệ:**
  - Liên kết 1-1 với `User`.
  - Liên kết 1-1 với `CandidateProfile`.
  - Có thể tạo nhiều `Application`.
  - Có thể có nhiều `Skill` (Thông qua bảng trung gian `CandidateSkill`).

### 1.4 CandidateProfile (Hồ sơ chi tiết của ứng viên)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `candidate_id`: Khóa ngoại tham chiếu đến bảng `Candidate` (1-1).
  - `phone`: Số điện thoại.
  - `address`: Địa chỉ.
  - `summary`: Giới thiệu bản thân.
  - `experience_years`: Số năm kinh nghiệm.
  - `education`: Trình độ học vấn / Trường học.
  - `github_url` / `linkedin_url`: Các link mạng xã hội / portfolio.
  - `cv_url`: Đường dẫn tới file CV mặc định của ứng viên.

### 1.5 Skill (Kỹ năng)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `name`: Tên kỹ năng (Ví dụ: Java, React, Python, Communication).
  - `category`: Loại kỹ năng (Soft skill, Hard skill, Framework...).
- **Mối quan hệ:**
  - Thuộc về nhiều `Candidate` (nhiều-nhiều).

### 1.6 CandidateSkill (Bảng trung gian Ứng viên - Kỹ năng)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `candidate_id`: Khóa ngoại tham chiếu `Candidate`.
  - `skill_id`: Khóa ngoại tham chiếu `Skill`.
  - `proficiency_level`: Mức độ thành thạo (Enum: Beginner, Intermediate, Advanced, Expert).
  - `years_of_experience`: Số năm kinh nghiệm với kỹ năng này.

### 1.7 Company (Công ty)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `name`: Tên công ty.
  - `industry`: Lĩnh vực hoạt động.
  - `website_url`: Trang web công ty.
  - `created_at`: Ngày tạo.
- **Mối quan hệ:**
  - Liên kết 1-1 với `CompanyProfile`.
  - Có thể có nhiều `JobPosting`.

### 1.8 CompanyProfile (Hồ sơ chi tiết của Công ty)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `company_id`: Khóa ngoại tham chiếu `Company` (1-1).
  - `logo_url`: Đường dẫn logo công ty.
  - `description`: Bài giới thiệu về công ty.
  - `location`: Địa chỉ trụ sở / văn phòng.
  - `company_size`: Quy mô công ty (Ví dụ: 1-50, 50-200...).
  - `benefits`: Các phúc lợi dành cho nhân viên (Text hoặc JSON).

### 1.9 Category (Danh mục ngành nghề)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `name`: Tên danh mục (Ví dụ: IT, Marketing, Sales, Design).
  - `description`: Mô tả danh mục.
- **Mối quan hệ:**
  - Có thể có nhiều `JobPosting`.

### 1.10 JobPosting (Bài đăng tuyển dụng)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `company_id`: Khóa ngoại tham chiếu `Company`.
  - `category_id`: Khóa ngoại tham chiếu `Category` (Phân loại công việc).
  - `title`: Tiêu đề công việc (Bắt buộc).
  - `department`: Phòng ban (Bắt buộc).
  - `location`: Địa điểm (Bắt buộc).
  - `description`: Mô tả công việc (Bắt buộc).
  - `requirements`: Yêu cầu công việc (Tùy chọn).
  - `salary_range`: Mức lương (Tùy chọn).
  - `application_deadline`: Hạn nộp hồ sơ (Tùy chọn).
  - `status`: Trạng thái (Enum: Draft, Active, Closed, REJECTED).
  - `created_by`: Khóa ngoại tham chiếu `User` (HR đã tạo).
  - `created_at`: Ngày tạo.
- **Mối quan hệ:**
  - Thuộc về một `Company`.
  - Thuộc về một `Category`.
  - Có thể có nhiều `Application`.

### 1.11 Application (Hồ sơ ứng tuyển cho Job cụ thể)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `candidate_id`: Khóa ngoại tham chiếu `Candidate`.
  - `job_posting_id`: Khóa ngoại tham chiếu `JobPosting`.
  - `submission_date`: Ngày nộp.
  - `status`: Trạng thái hồ sơ (Enum: Applied, Screening, Interview, Offer, Hired, Rejected, Withdrawn).
  - `cv_file_url`: Đường dẫn tới file CV được sử dụng riêng cho lần nộp này.
- **Mối quan hệ:**
  - Thuộc về một `Candidate`.
  - Thuộc về một `JobPosting`.
  - Có thể có nhiều `Interview`.
  - Có thể có nhiều `Internal Note`.

### 1.12 Interview (Lịch phỏng vấn)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `application_id`: Khóa ngoại tham chiếu `Application`.
  - `interviewer_id`: Khóa ngoại tham chiếu `User` (Interviewer).
  - `interview_date`: Ngày phỏng vấn.
  - `interview_time`: Giờ phỏng vấn.
  - `location_or_link`: Địa điểm hoặc link họp (Tùy chọn).
  - `status`: Trạng thái (Enum: Scheduled, Evaluated).
- **Mối quan hệ:**
  - Thuộc về một `Application`.
  - Phân công cho một `User` (Interviewer).
  - Liên kết 1-1 với `Evaluation`.

### 1.13 Evaluation (Đánh giá phỏng vấn)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `interview_id`: Khóa ngoại tham chiếu `Interview`.
  - `rating`: Đánh giá sao (Integer 1-5).
  - `feedback`: Nhận xét chi tiết (Text).
  - `submitted_at`: Ngày giờ nộp đánh giá.
- **Mối quan hệ:**
  - Thuộc về một `Interview` (Mối quan hệ 1-1).

### 1.14 Internal Note (Ghi chú nội bộ)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `application_id`: Khóa ngoại tham chiếu `Application`.
  - `author_id`: Khóa ngoại tham chiếu `User` (HR / Admin tạo).
  - `content`: Nội dung ghi chú.
  - `created_at`: Ngày giờ tạo.
- **Mối quan hệ:**
  - Thuộc về một `Application`.
  - Thuộc về một `User`.

### 1.15 Activity Log (Nhật ký hoạt động)
- **Thuộc tính:**
  - `id`: Định danh duy nhất.
  - `user_id`: Khóa ngoại tham chiếu `User` (Actor).
  - `event_type`: Loại sự kiện (Enum: Sign-in success, Sign-in failure, Account created, ...).
  - `description`: Chi tiết sự kiện.
  - `ip_address`: Địa chỉ IP.
  - `timestamp`: Thời gian xảy ra.
- **Mối quan hệ:**
  - Thuộc về một `User` (Actor).

## 2. Relationships Summary (ERD Overview)

- **Role [1]** ---- **[N] User**
- **User [1]** ---- **[1] Candidate**
- **Candidate [1]** ---- **[1] CandidateProfile**
- **Candidate [N]** ---- **[M] Skill** (Thông qua `CandidateSkill`)
- **Company [1]** ---- **[1] CompanyProfile**
- **Company [1]** ---- **[N] JobPosting**
- **Category [1]** ---- **[N] JobPosting**
- **User (HR Manager) [1]** ---- **[N] JobPosting**
- **Candidate [1]** ---- **[N] Application**
- **JobPosting [1]** ---- **[N] Application**
- **Application [1]** ---- **[N] Interview**
- **User (Interviewer) [1]** ---- **[N] Interview**
- **Interview [1]** ---- **[1] Evaluation**
- **Application [1]** ---- **[N] Internal Note**
- **User (Admin/HR) [1]** ---- **[N] Internal Note**
- **User [1]** ---- **[N] Activity Log**
