package com.careerpilot.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String education; // JSON Array of Education objects

    @Column(columnDefinition = "TEXT")
    private String skills; // JSON Array of Skills

    @Column(columnDefinition = "TEXT")
    private String projects; // JSON Array of Projects

    @Column(columnDefinition = "TEXT")
    private String experience; // JSON Array of Experiences

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText; // Extracted raw text from PDF

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
