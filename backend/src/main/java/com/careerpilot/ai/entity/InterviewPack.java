package com.careerpilot.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_packs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_analysis_id", nullable = false, unique = true)
    private JobAnalysis jobAnalysis;

    @Column(name = "technical_questions", columnDefinition = "TEXT")
    private String technicalQuestions; // JSON Array of Question-Answer objects

    @Column(name = "hr_questions", columnDefinition = "TEXT")
    private String hrQuestions; // JSON Array of Question-Answer objects

    @Column(name = "project_questions", columnDefinition = "TEXT")
    private String projectQuestions; // JSON Array of Question-Answer objects

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
