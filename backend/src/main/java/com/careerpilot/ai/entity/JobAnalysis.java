package com.careerpilot.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_analyses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    @Column(name = "match_score")
    private Integer matchScore; // Job Match Score (0-100)

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills; // JSON Array of strings

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills; // JSON Array of strings

    @Column(columnDefinition = "TEXT")
    private String strengths; // JSON Array of strings

    @Column(columnDefinition = "TEXT")
    private String weaknesses; // JSON Array of strings

    @Column(name = "ats_score")
    private Integer atsScore; // ATS Score (0-100)

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords; // JSON Array of strings

    @Column(name = "resume_improvements", columnDefinition = "TEXT")
    private String resumeImprovements; // JSON Array of strings/objects

    @Column(name = "ats_recommendations", columnDefinition = "TEXT")
    private String atsRecommendations; // JSON Array of strings/objects

    @Column(name = "missing_technologies", columnDefinition = "TEXT")
    private String missingTechnologies; // JSON Array of strings

    @Column(name = "skill_priority_ranking", columnDefinition = "TEXT")
    private String skillPriorityRanking; // JSON Array/Object of prioritized skills

    @Column(name = "explanation_of_importance", columnDefinition = "TEXT")
    private String explanationOfImportance;

    @Column(name = "career_level_current")
    private String careerLevelCurrent;

    @Column(name = "career_level_target")
    private String careerLevelTarget;

    @Column(name = "career_missing_competencies", columnDefinition = "TEXT")
    private String careerMissingCompetencies; // JSON Array of missing competencies

    @Column(name = "career_time_to_reach")
    private String careerTimeToReach;

    @Column(name = "application_readiness_score")
    private Integer applicationReadinessScore; // Overall Career Readiness Score (0-100)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
