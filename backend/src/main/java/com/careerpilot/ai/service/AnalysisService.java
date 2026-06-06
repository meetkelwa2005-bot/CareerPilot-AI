package com.careerpilot.ai.service;

import com.careerpilot.ai.entity.JobAnalysis;
import com.careerpilot.ai.entity.Resume;
import com.careerpilot.ai.repository.JobAnalysisRepository;
import com.careerpilot.ai.repository.ResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class AnalysisService {

    private final GeminiClient geminiClient;
    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final ObjectMapper objectMapper;

    public AnalysisService(GeminiClient geminiClient, ResumeRepository resumeRepository,
                           JobAnalysisRepository jobAnalysisRepository, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.resumeRepository = resumeRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.objectMapper = objectMapper;
    }

    public JobAnalysis analyzeJobAndResume(Long resumeId, String jobTitle, String jobDescription) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found with ID: " + resumeId));

        // Format candidate profile details to present to the AI
        String resumeJsonString = String.format(
                "{\"name\":\"%s\",\"skills\":%s,\"projects\":%s,\"experience\":%s,\"education\":%s}",
                resume.getName(), resume.getSkills(), resume.getProjects(), resume.getExperience(), resume.getEducation()
        );

        String prompt = """
                [ANALYZE_JOB]
                You are a senior technical recruiter and ATS expert.
                Analyze the candidate's resume (provided in JSON format) against the target Job Title and Job Description.
                Your response MUST be a single, valid JSON object matching the schema below exactly, and contain NO markdown code blocks, surrounding backticks, or extra text. Do not wrap the JSON in ```json ... ```.

                Resume JSON Profile:
                %s

                Target Job Title: %s
                Target Job Description:
                %s

                Output JSON Schema:
                {
                  "matchScore": 75,
                  "matchedSkills": ["Java", "Spring Boot", "SQL"],
                  "missingSkills": ["Docker", "Kubernetes", "AWS", "TypeScript"],
                  "strengths": [
                    "Strong foundation in core Java programming",
                    "Practical microservices project experience"
                  ],
                  "weaknesses": [
                    "No experience in containerization and modern cloud platforms",
                    "Lack of automated test suites and CI/CD pipelines"
                  ],
                  "atsScore": 65,
                  "missingKeywords": ["Docker", "CI/CD", "AWS", "Kubernetes"],
                  "resumeImprovements": [
                    "Add detailed bullet points describing your microservice project's database choices.",
                    "Highlight cloud deployments or hosting solutions used."
                  ],
                  "atsRecommendations": [
                    "Use a clean chronological formatting scheme.",
                    "Format bullet points with action verbs."
                  ],
                  "missingTechnologies": ["Docker", "AWS", "TypeScript"],
                  "skillPriorityRanking": [
                    { "skill": "Docker", "priority": "High", "timeToLearn": "3-5 days" },
                    { "skill": "AWS basics", "priority": "Medium", "timeToLearn": "5 days" },
                    { "skill": "TypeScript", "priority": "Low", "timeToLearn": "7 days" }
                  ],
                  "explanationOfImportance": "Docker is mandatory for modern backend architectures to ensure consistency across environments. AWS is standard for junior developer deployments.",
                  "careerLevelCurrent": "Entry-Level / Intern",
                  "careerLevelTarget": "Junior Software Engineer",
                  "careerMissingCompetencies": ["Containerization", "Cloud Deployments", "Frontend Type-safety"],
                  "careerTimeToReach": "30 Days",
                  "applicationReadinessScore": 70
                }
                """.formatted(resumeJsonString, jobTitle, jobDescription);

        String resultJson = geminiClient.generateContent(prompt, true);
        String cleanedJson = JsonUtils.cleanJsonString(resultJson);

        try {
            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            JobAnalysis analysis = JobAnalysis.builder()
                    .resume(resume)
                    .jobTitle(jobTitle)
                    .jobDescription(jobDescription)
                    .matchScore(getFieldAsInt(rootNode, "matchScore", 50))
                    .matchedSkills(rootNode.has("matchedSkills") ? rootNode.get("matchedSkills").toString() : "[]")
                    .missingSkills(rootNode.has("missingSkills") ? rootNode.get("missingSkills").toString() : "[]")
                    .strengths(rootNode.has("strengths") ? rootNode.get("strengths").toString() : "[]")
                    .weaknesses(rootNode.has("weaknesses") ? rootNode.get("weaknesses").toString() : "[]")
                    .atsScore(getFieldAsInt(rootNode, "atsScore", 50))
                    .missingKeywords(rootNode.has("missingKeywords") ? rootNode.get("missingKeywords").toString() : "[]")
                    .resumeImprovements(rootNode.has("resumeImprovements") ? rootNode.get("resumeImprovements").toString() : "[]")
                    .atsRecommendations(rootNode.has("atsRecommendations") ? rootNode.get("atsRecommendations").toString() : "[]")
                    .missingTechnologies(rootNode.has("missingTechnologies") ? rootNode.get("missingTechnologies").toString() : "[]")
                    .skillPriorityRanking(rootNode.has("skillPriorityRanking") ? rootNode.get("skillPriorityRanking").toString() : "[]")
                    .explanationOfImportance(getFieldAsString(rootNode, "explanationOfImportance", ""))
                    .careerLevelCurrent(getFieldAsString(rootNode, "careerLevelCurrent", "Entry-Level"))
                    .careerLevelTarget(getFieldAsString(rootNode, "careerLevelTarget", "Junior Developer"))
                    .careerMissingCompetencies(rootNode.has("careerMissingCompetencies") ? rootNode.get("careerMissingCompetencies").toString() : "[]")
                    .careerTimeToReach(getFieldAsString(rootNode, "careerTimeToReach", "30 Days"))
                    .applicationReadinessScore(getFieldAsInt(rootNode, "applicationReadinessScore", 50))
                    .createdAt(LocalDateTime.now())
                    .build();

            return jobAnalysisRepository.save(analysis);

        } catch (Exception e) {
            log.error("Failed to parse the Gemini JSON response for job analysis: {}", resultJson, e);
            // Fallback object to prevent system crash
            JobAnalysis fallbackAnalysis = JobAnalysis.builder()
                    .resume(resume)
                    .jobTitle(jobTitle)
                    .jobDescription(jobDescription)
                    .matchScore(60)
                    .matchedSkills("[\"Java\",\"Spring Boot\"]")
                    .missingSkills("[\"Docker\",\"AWS\"]")
                    .strengths("[\"Core Java experience\"]")
                    .weaknesses("[\"No containerization experience\"]")
                    .atsScore(55)
                    .missingKeywords("[\"Docker\",\"AWS\"]")
                    .resumeImprovements("[\"Optimize project descriptions\"]")
                    .atsRecommendations("[\"Clean layout\"]")
                    .missingTechnologies("[\"Docker\",\"AWS\"]")
                    .skillPriorityRanking("[{\"skill\":\"Docker\",\"priority\":\"High\",\"timeToLearn\":\"3 days\"}]")
                    .explanationOfImportance("Crucial for local and staging environments.")
                    .careerLevelCurrent("Graduate")
                    .careerLevelTarget("Junior Developer")
                    .careerMissingCompetencies("[\"DevOps\"]")
                    .careerTimeToReach("30 Days")
                    .applicationReadinessScore(60)
                    .createdAt(LocalDateTime.now())
                    .build();
            return jobAnalysisRepository.save(fallbackAnalysis);
        }
    }

    private int getFieldAsInt(JsonNode node, String fieldName, int defaultValue) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asInt();
        }
        return defaultValue;
    }

    private String getFieldAsString(JsonNode node, String fieldName, String defaultValue) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return defaultValue;
    }
}
