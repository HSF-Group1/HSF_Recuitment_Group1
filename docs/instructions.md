context
You are an expert Backend Developer and software architect specialized in Java Spring Boot, Spring Data JPA, and database design.

I have a system model specification defined in my project [entities.md](file;file:///c%3A/Users/ADMIN/Downloads/recruitment/recruitment/docs/entities.md) . Your task is to generate ALL Java Entity classes based on that specification while strictly adhering to the following professional conventions and architecture requirements:

### 1. Project Package Structure Requirements
You must organize the classes into separate packages. Do NOT embed Enum declarations inside the Entity classes. Every Enum and Entity must be a standalone Java file.
- **Entity Package (`com.example.talenthub.entity` or `.entity`):** Contains only JPA Entity classes.
- **Enum Package (`com.example.talenthub.enums` or `.enums`):** Contains only Java Enum files. Inside the Entities, import these Enums and use the `@Enumerated(EnumType.STRING)` annotation above the enum fields.

### 2. Java & JPA Coding Conventions (NO Validation, NO AI)
- Use standard Java PascalCase for Class/Enum names and camelCase for field names.
- Table names and Column names MUST be explicitly defined using `@Table(name = "...")` and `@Column(name = "...")` in snake_case (e.g., `job_postings`, `candidate_id`).
- Use Lombok annotations for Entities to reduce boilerplate code: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder`. (DO NOT use `@Data` to avoid performance issues with JPA lazy loading).
- Use proper JPA annotations: `@Entity`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- CRITICAL: DO NOT add any validation annotations (like `@NotBlank`, `@NotNull`, `@Size`, `@Email`) in these Entity classes. Validation will be handled strictly at the DTO layer.

### 3. Smart Time Management (Java 8 Time API)
Please apply the correct Time types based on business logic:
- Use `LocalDateTime` for creation/audit/system timestamps (e.g., `created_at`, `submitted_at`, `timestamp`).
- Use `LocalDate` for strict calendar dates without time (specifically `application_deadline` in JobPosting and `interview_date` in Interview).
- Use `LocalTime` for strict clock time (specifically `interview_time` in Interview).

### 4. CRITICAL: Many-to-Many (N-N) Relationship Requirement
- DO NOT use the implicit `@JoinTable` annotation for Many-to-Many relationships.
- For EVERY Many-to-Many relationship (specifically Candidate and Skill via `CandidateSkill`), you MUST explicitly create a dedicated bridge/intermediary Entity class (e.g., `CandidateSkill`).
- This bridge entity must be treated as a fully managed JPA entity with its own auto-increment Primary Key (`id`), and two separate `@ManyToOne` relationships pointing back to the parent entities.
- In the parent entities, map this connection using a `@OneToMany(mappedBy = "...")` relationship pointing to the bridge entity.

### 5. Other Relationships Configuration
- For `@ManyToOne` relationships (including the connection from `JobPosting` to `Category`), always use `@JoinColumn(name = "foreign_key_id")` and configure lazy loading: `fetch = FetchType.LAZY`.
- For `@OneToOne` relationships (such as Candidate to CandidateProfile, Company to CompanyProfile, and Interview to Evaluation), specify the owner side and the inverse side correctly using `mappedBy`.

### 6. Output Expectations
- Clearly specify the package name at the top of each file (e.g., `package com.example.talenthub.model.enums;` or `package com.example.talenthub.model.entity;`).
- Generate the full set of core Enums first, then generate the full set of core Entities (`Role`, `User`, `Candidate`, `CandidateProfile`, `Skill`, `CandidateSkill`, `Company`, `CompanyProfile`, `Category`, `JobPosting`, `Application`, `Interview`, `Evaluation`, `InternalNote`, `ActivityLog`). 
- Do NOT include any AI-specific entities.

Please read the entity specification from my documentation now and generate the clean Java source code according to these package rules.