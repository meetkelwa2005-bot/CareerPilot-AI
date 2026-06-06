package com.careerpilot.ai.service;

import com.careerpilot.ai.entity.JobAnalysis;
import com.careerpilot.ai.entity.LearningRoadmap;
import com.careerpilot.ai.repository.JobAnalysisRepository;
import com.careerpilot.ai.repository.LearningRoadmapRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class RoadmapService {

    private final GeminiClient geminiClient;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final LearningRoadmapRepository learningRoadmapRepository;
    private final ObjectMapper objectMapper;

    public RoadmapService(GeminiClient geminiClient, JobAnalysisRepository jobAnalysisRepository,
                          LearningRoadmapRepository learningRoadmapRepository, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.learningRoadmapRepository = learningRoadmapRepository;
        this.objectMapper = objectMapper;
    }

    public LearningRoadmap getOrCreateRoadmap(Long jobAnalysisId) {
        // Check if roadmap already exists
        return learningRoadmapRepository.findByJobAnalysisId(jobAnalysisId)
                .orElseGet(() -> generateAndSaveRoadmap(jobAnalysisId));
    }

    private LearningRoadmap generateAndSaveRoadmap(Long jobAnalysisId) {
        JobAnalysis analysis = jobAnalysisRepository.findById(jobAnalysisId)
                .orElseThrow(() -> new IllegalArgumentException("JobAnalysis not found with ID: " + jobAnalysisId));

        String prompt = """
                [GENERATE_ROADMAP]
                You are a senior technical educator and software engineering coach.
                Create a highly detailed 30-Day Learning Roadmap for a final year student to acquire the missing skills for a target job.
                Your response MUST be a single, valid JSON object matching the schema below exactly, and contain NO markdown code blocks, backticks, or extra text. Do not wrap the JSON in ```json ... ```.

                Target Job Title: %s
                Candidate Missing Skills: %s
                Target Tech Stack: %s

                Output JSON Schema:
                {
                  "readinessTimeline": "30 Days",
                  "learningPlan30Days": [
                    { "day": 1, "topic": "Docker Basics", "details": "Study containers, host-ports, and write a simple Docker run command." },
                    { "day": 5, "topic": "Writing custom Dockerfiles", "details": "Learn configuration files for Java and Node apps." }
                    // Include 6 to 9 distinct milestones spreading over the 30-day timeline
                  ],
                  "weeklyMilestones": [
                    { "week": 1, "goal": "Weekly Target Goal", "deliverable": "Tangible thing they will build/demo at week's end" }
                    // Expose exactly 4 weekly milestones (Week 1, Week 2, Week 3, Week 4)
                  ],
                  "learningSequence": [
                    "Topic A", "Topic B", "Topic C"
                    // General high-level sequence of major skill milestones
                  ]
                }
                """.formatted(analysis.getJobTitle(), analysis.getMissingSkills(), analysis.getMissingTechnologies());

        String resultJson = geminiClient.generateContent(prompt, true);
        String cleanedJson = JsonUtils.cleanJsonString(resultJson);

        try {
            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            LearningRoadmap roadmap = LearningRoadmap.builder()
                    .jobAnalysis(analysis)
                    .learningPlan30Days(rootNode.has("learningPlan30Days") ? rootNode.get("learningPlan30Days").toString() : "[]")
                    .weeklyMilestones(rootNode.has("weeklyMilestones") ? rootNode.get("weeklyMilestones").toString() : "[]")
                    .learningSequence(rootNode.has("learningSequence") ? rootNode.get("learningSequence").toString() : "[]")
                    .readinessTimeline(rootNode.has("readinessTimeline") ? rootNode.get("readinessTimeline").asText("30 Days") : "30 Days")
                    .createdAt(LocalDateTime.now())
                    .build();

            return learningRoadmapRepository.save(roadmap);

        } catch (Exception e) {
            log.error("Failed to parse the Gemini JSON response for learning roadmap: {}", resultJson, e);
            // Fallback roadmap
            LearningRoadmap fallbackRoadmap = LearningRoadmap.builder()
                    .jobAnalysis(analysis)
                    .learningPlan30Days("[{\"day\":1,\"topic\":\"Docker Containers\",\"details\":\"Basic docker setup\"}]")
                    .weeklyMilestones("[{\"week\":1,\"goal\":\"Understand Container basics\",\"deliverable\":\"Run simple container\"}]")
                    .learningSequence("[\"Docker\"]")
                    .readinessTimeline("30 Days")
                    .createdAt(LocalDateTime.now())
                    .build();
            return learningRoadmapRepository.save(fallbackRoadmap);
        }
    }
}
