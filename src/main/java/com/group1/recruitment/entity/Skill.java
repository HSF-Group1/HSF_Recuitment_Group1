package com.group1.recruitment.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "category")
    private String category;

    @OneToMany(mappedBy = "skill")
    private List<CandidateSkill> candidateSkills;

    // No-args constructor
    public Skill() {
    }

    // All-args constructor
    public Skill(Long id, String name, String category, List<CandidateSkill> candidateSkills) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.candidateSkills = candidateSkills;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<CandidateSkill> getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(List<CandidateSkill> candidateSkills) {
        this.candidateSkills = candidateSkills;
    }
}
