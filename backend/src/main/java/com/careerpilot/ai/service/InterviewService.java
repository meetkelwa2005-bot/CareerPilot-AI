package com.careerpilot.ai.service;

import com.careerpilot.ai.entity.InterviewPack;
import com.careerpilot.ai.entity.JobAnalysis;
import com.careerpilot.ai.repository.InterviewPackRepository;
import com.careerpilot.ai.repository.JobAnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@Slf4j
public class InterviewService {

    private final GeminiClient geminiClient;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final InterviewPackRepository interviewPackRepository;
    private final ObjectMapper objectMapper;

    public InterviewService(GeminiClient geminiClient, JobAnalysisRepository jobAnalysisRepository,
                            InterviewPackRepository interviewPackRepository, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.interviewPackRepository = interviewPackRepository;
        this.objectMapper = objectMapper;
    }

    public InterviewPack getOrCreateInterviewPack(Long jobAnalysisId) {
        return interviewPackRepository.findByJobAnalysisId(jobAnalysisId)
                .orElseGet(() -> generateAndSaveInterviewPack(jobAnalysisId));
    }

    private InterviewPack generateAndSaveInterviewPack(Long jobAnalysisId) {
        JobAnalysis analysis = jobAnalysisRepository.findById(jobAnalysisId)
                .orElseThrow(() -> new IllegalArgumentException("JobAnalysis not found with ID: " + jobAnalysisId));

        // Incorporate resume info if needed
        String resumeProjects = analysis.getResume().getProjects();
        String resumeSkills = analysis.getResume().getSkills();

        String prompt = """
                [GENERATE_INTERVIEW_PACK]
                You are a senior hiring manager at a top tech company.
                Generate a targeted Interview Preparation Pack including Technical, HR, and Project-specific questions tailored to the candidate's profile and job description.
                Your response MUST be a single, valid JSON object matching the schema below exactly, and contain NO markdown code blocks, backticks, or extra text. Do not wrap the JSON in ```json ... ```.

                Target Job Title: %s
                Target Job Description: %s
                Candidate Skills: %s
                Candidate Projects: %s

                Output JSON Schema:
                {
                  "technicalQuestions": [
                    {
                      "question": "What is the difference between REST and GraphQL, and when would you use each?",
                      "answer": "REST is resource-based where each URL represents a resource. GraphQL has a single endpoint and allows clients to query for specific fields, preventing over-fetching."
                    }
                    // Expose 3 to 4 core technical questions targeting the JD stack
                  ],
                  "hrQuestions": [
                    {
                      "question": "How do you handle conflict or differing technical opinions in a development team?",
                      "answer": "Focus on data and objectivity. Listen to their reasoning, explain yours clearly, run a small POC if needed, and defer to the team lead's decision if a deadlock occurs."
                    }
                    // Expose 2 to 3 standard engineering behavioral questions
                  ],
                  "projectQuestions": [
                    {
                      "question": "Can you walk us through the system architecture of your project and justify your tech choices?",
                      "answer": "Be prepared to walk through your components, databases, and how services communicate (e.g., REST, WebSockets) and details on scaling or security implemented."
                    }
                    // Expose 2 targeted questions specifically asking about the candidate's E-commerce or project details
                  ]
                }
                """.formatted(analysis.getJobTitle(), analysis.getJobDescription(), resumeSkills, resumeProjects);

        String resultJson = geminiClient.generateContent(prompt, true);
        String cleanedJson = JsonUtils.cleanJsonString(resultJson);

        try {
            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            InterviewPack pack = InterviewPack.builder()
                    .jobAnalysis(analysis)
                    .technicalQuestions(rootNode.has("technicalQuestions") ? rootNode.get("technicalQuestions").toString() : "[]")
                    .hrQuestions(rootNode.has("hrQuestions") ? rootNode.get("hrQuestions").toString() : "[]")
                    .projectQuestions(rootNode.has("projectQuestions") ? rootNode.get("projectQuestions").toString() : "[]")
                    .createdAt(LocalDateTime.now())
                    .build();

            return interviewPackRepository.save(pack);

        } catch (Exception e) {
            log.error("Failed to parse the Gemini JSON response for interview pack: {}", resultJson, e);
            // Fallback pack
            InterviewPack fallbackPack = InterviewPack.builder()
                    .jobAnalysis(analysis)
                    .technicalQuestions("[{\"question\":\"Explain core Java concepts.\",\"answer\":\"Inheritance, polymorphism, encapsulation, abstraction.\"}]")
                    .hrQuestions("[{\"question\":\"Why do you want to join?\",\"answer\":\"To grow my engineering skills.\"}]")
                    .projectQuestions("[{\"question\":\"Describe your tech stack.\",\"answer\":\"Used React and Spring Boot for client-server architecture.\"}]")
                    .createdAt(LocalDateTime.now())
                    .build();
            return interviewPackRepository.save(fallbackPack);
        }
    }
}
