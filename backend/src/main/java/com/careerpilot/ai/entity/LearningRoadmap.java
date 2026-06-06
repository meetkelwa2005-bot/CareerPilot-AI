package com.careerpilot.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_roadmaps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_analysis_id", nullable = false, unique = true)
    private JobAnalysis jobAnalysis;

    @Column(name = "learning_plan_30_days", columnDefinition = "TEXT")
    private String learningPlan30Days; // JSON Array or Object detailing day-by-day plan

    @Column(name = "weekly_milestones", columnDefinition = "TEXT")
    private String weeklyMilestones; // JSON Array of weekly targets

    @Column(name = "learning_sequence", columnDefinition = "TEXT")
    private String learningSequence; // JSON Array of topics

    @Column(name = "readiness_timeline")
    private String readinessTimeline; // Estimated time to reach role

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
