package com.careerpilot.ai.service;

import com.careerpilot.ai.entity.ChatHistory;
import com.careerpilot.ai.entity.JobAnalysis;
import com.careerpilot.ai.repository.ChatHistoryRepository;
import com.careerpilot.ai.repository.JobAnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatAssistantService {

    private final GeminiClient geminiClient;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ObjectMapper objectMapper;

    public ChatAssistantService(GeminiClient geminiClient, JobAnalysisRepository jobAnalysisRepository,
                                ChatHistoryRepository chatHistoryRepository, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public List<ChatHistory> getChatHistory(Long jobAnalysisId) {
        return chatHistoryRepository.findByJobAnalysisIdOrderByCreatedAtAsc(jobAnalysisId);
    }

    public ChatHistory answerQuestion(Long jobAnalysisId, String userMessage) {
        JobAnalysis analysis = jobAnalysisRepository.findById(jobAnalysisId)
                .orElseThrow(() -> new IllegalArgumentException("JobAnalysis not found with ID: " + jobAnalysisId));

        // Save User Message
        ChatHistory userChat = ChatHistory.builder()
                .jobAnalysis(analysis)
                .sender("USER")
                .message(userMessage)
                .createdAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(userChat);

        // Fetch past history
        List<ChatHistory> history = chatHistoryRepository.findByJobAnalysisIdOrderByCreatedAtAsc(jobAnalysisId);
        String formattedHistory = history.stream()
                .map(chat -> chat.getSender() + ": " + chat.getMessage())
                .collect(Collectors.joining("\n"));

        // Compile prompt with context
        String prompt = """
                [CHAT_ASSISTANT]
                You are "CareerPilot AI", a premium, context-aware AI Career Advisor for final-year engineering students and fresh graduates.
                Answer the user's questions objectively, professionally, and encouragement-focused.
                Use the candidate's profile, job analysis, ATS score, missing skills, and learning plan to back up your answer.
                Return your answer inside a valid JSON object matching the schema below exactly, and contain NO formatting blocks, backticks, or extra text. Do not wrap the JSON in ```json ... ```.

                Candidate Context:
                - Name: %s
                - Target Job: %s
                - Match Score: %d/100
                - ATS Score: %d/100
                - Application Readiness Score: %d/100
                - Matched Skills: %s
                - Missing Skills: %s
                - Resume Improvement Suggestions: %s
                - Career Level: Current is %s, Target is %s. Estimated preparation needed: %s.

                Chat History:
                %s

                Latest User Question:
                %s

                Output JSON Schema:
                {
                  "answer": "Your detailed answer. You can use markdown bolding (*text*), line breaks, or bullet points in the string text to format your answer professionally."
                }
                """.formatted(
                        analysis.getResume().getName(),
                        analysis.getJobTitle(),
                        analysis.getMatchScore(),
                        analysis.getAtsScore(),
                        analysis.getApplicationReadinessScore(),
                        analysis.getMatchedSkills(),
                        analysis.getMissingSkills(),
                        analysis.getResumeImprovements(),
                        analysis.getCareerLevelCurrent(),
                        analysis.getCareerLevelTarget(),
                        analysis.getCareerTimeToReach(),
                        formattedHistory,
                        userMessage
                );

        String resultJson = geminiClient.generateContent(prompt, true);
        String cleanedJson = JsonUtils.cleanJsonString(resultJson);
        String answer = "I apologize, but I am having trouble compiling an answer right now. Please try again.";

        try {
            JsonNode rootNode = objectMapper.readTree(cleanedJson);
            if (rootNode.has("answer")) {
                answer = rootNode.get("answer").asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response for chat: {}", resultJson, e);
            // Default response
            answer = "Based on your scores (Match: " + analysis.getMatchScore() + "%, ATS: " + analysis.getAtsScore() + "%), you should focus on adding missing technologies like " + analysis.getMissingTechnologies() + " to your project list. Follow your 30-day roadmap closely!";
        }

        // Save and return Assistant Message
        ChatHistory assistantChat = ChatHistory.builder()
                .jobAnalysis(analysis)
                .sender("ASSISTANT")
                .message(answer)
                .createdAt(LocalDateTime.now())
                .build();
        return chatHistoryRepository.save(assistantChat);
    }
}
