package com.careerpilot.ai.controller;

import com.careerpilot.ai.dto.JobAnalysisRequest;
import com.careerpilot.ai.entity.InterviewPack;
import com.careerpilot.ai.entity.JobAnalysis;
import com.careerpilot.ai.entity.LearningRoadmap;
import com.careerpilot.ai.repository.JobAnalysisRepository;
import com.careerpilot.ai.service.AnalysisService;
import com.careerpilot.ai.service.InterviewService;
import com.careerpilot.ai.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class AnalysisController {

    private final AnalysisService analysisService;
    private final RoadmapService roadmapService;
    private final InterviewService interviewService;
    private final JobAnalysisRepository jobAnalysisRepository;

    @PostMapping
    public ResponseEntity<JobAnalysis> analyzeJob(@Valid @RequestBody JobAnalysisRequest request) {
        log.info("Received request to analyze job: title={}, resumeId={}", request.getJobTitle(), request.getResumeId());
        try {
            JobAnalysis analysis = analysisService.analyzeJobAndResume(
                    request.getResumeId(),
                    request.getJobTitle(),
                    request.getJobDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(analysis);
        } catch (Exception e) {
            log.error("Failed to run job analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<JobAnalysis>> getAllAnalyses() {
        return ResponseEntity.ok(jobAnalysisRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobAnalysis> getAnalysisById(@PathVariable Long id) {
        return jobAnalysisRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/roadmap")
    public ResponseEntity<LearningRoadmap> getRoadmap(@PathVariable Long id) {
        log.info("Request for roadmap linked to analysis: {}", id);
        try {
            LearningRoadmap roadmap = roadmapService.getOrCreateRoadmap(id);
            return ResponseEntity.ok(roadmap);
        } catch (Exception e) {
            log.error("Failed to generate learning roadmap", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/interview-pack")
    public ResponseEntity<InterviewPack> getInterviewPack(@PathVariable Long id) {
        log.info("Request for interview pack linked to analysis: {}", id);
        try {
            InterviewPack pack = interviewService.getOrCreateInterviewPack(id);
            return ResponseEntity.ok(pack);
        } catch (Exception e) {
            log.error("Failed to generate interview pack", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
