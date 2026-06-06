package com.careerpilot.ai.controller;

import com.careerpilot.ai.entity.JobAnalysis;
import com.careerpilot.ai.service.DemoDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class DemoController {

    private final DemoDataService demoDataService;

    @PostMapping
    public ResponseEntity<JobAnalysis> generateDemoData() {
        log.info("Request received to load demo dataset.");
        try {
            JobAnalysis analysis = demoDataService.createDemoDataset();
            return ResponseEntity.status(HttpStatus.CREATED).body(analysis);
        } catch (Exception e) {
            log.error("Failed to load demo data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
