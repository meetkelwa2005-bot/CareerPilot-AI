package com.careerpilot.ai.controller;

import com.careerpilot.ai.entity.Resume;
import com.careerpilot.ai.repository.ResumeRepository;
import com.careerpilot.ai.service.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ResumeController {

    private final ResumeParserService resumeParserService;
    private final ResumeRepository resumeRepository;

    @PostMapping("/upload")
    public ResponseEntity<Resume> uploadResume(@RequestParam("file") MultipartFile file) {
        log.info("Received request to upload resume: name={}, size={}", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Resume savedResume = resumeParserService.parseAndSaveResume(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResume);
        } catch (Exception e) {
            log.error("Failed to parse and save resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Resume>> getAllResumes() {
        return ResponseEntity.ok(resumeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeById(@PathVariable Long id) {
        return resumeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
