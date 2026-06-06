package com.careerpilot.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiClient {

    private final RestTemplate restTemplate;
    
    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("${GEMINI_API_KEY}");
    }

    public String generateContent(String prompt, boolean requireJson) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("${GEMINI_API_KEY}")) {
            log.warn("GEMINI_API_KEY is not configured. Falling back to mock response.");
            return getMockResponse(prompt);
        }

        try {
            String url = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Constructing request body
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            if (requireJson) {
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                requestBody.put("generationConfig", generationConfig);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                Map<String, Object> body = responseEntity.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> candidateContent = (Map<String, Object>) candidate.get("content");
                    if (candidateContent != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) candidateContent.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            throw new RuntimeException("Empty response from Gemini API");
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}. Falling back to mock response.", e.getMessage());
            return getMockResponse(prompt);
        }
    }

    private String getMockResponse(String prompt) {
        // Simple heuristic to determine which prompt we are answering
        if (prompt.contains("PARSE_RESUME")) {
            return """
            {
              "name": "Jane Doe",
              "email": "jane.doe@example.com",
              "phone": "+1-555-0199",
              "education": [
                {
                  "degree": "Bachelor of Technology in Computer Science",
                  "institution": "State Technical University",
                  "duration": "2022 - 2026"
                }
              ],
              "skills": ["Java", "Spring Boot", "React", "JavaScript", "SQL", "Git", "HTML/CSS"],
              "projects": [
                {
                  "title": "E-Commerce Microservices Platform",
                  "description": "Developed a scalable e-commerce application using Spring Boot, Spring Cloud, and React. Configured Eureka Discovery and API Gateway.",
                  "technologies": ["Java", "Spring Boot", "Spring Cloud", "React", "PostgreSQL"]
                }
              ],
              "experience": [
                {
                  "role": "Software Engineering Intern",
                  "company": "Tech Solutions Inc.",
                  "duration": "Summer 2025",
                  "description": "Assisted in building REST APIs using Spring Boot and Hibernate. Worked on writing unit tests using JUnit."
                }
              ]
            }
            """;
        } else if (prompt.contains("ANALYZE_JOB")) {
            return """
            {
              "matchScore": 75,
              "matchedSkills": ["Java", "Spring Boot", "React", "JavaScript", "SQL", "Git"],
              "missingSkills": ["Docker", "Kubernetes", "AWS", "TypeScript", "CI/CD"],
              "strengths": [
                "Solid foundation in Core Java and Spring Framework",
                "Practical experience building REST APIs with PostgreSQL",
                "Hands-on project work with microservices architecture"
              ],
              "weaknesses": [
                "Lack of deployment and containerization experience (Docker/Kubernetes)",
                "No evidence of CI/CD pipeline configuration",
                "Limited familiarity with TypeScript and cloud providers like AWS"
              ],
              "atsScore": 68,
              "missingKeywords": ["Docker", "Containerization", "CI/CD", "AWS", "TypeScript", "Agile"],
              "resumeImprovements": [
                "Incorporate deployment details in projects (e.g. mention if hosted on Render or AWS)",
                "Explicitly list agile methodologies or team collaboration experience",
                "Add TypeScript and Docker keywords after learning them"
              ],
              "atsRecommendations": [
                "Use a standard, single-column chronological resume format",
                "Quantify achievements in your intern role (e.g. 'improved API response time by 15%')",
                "Avoid complex graphics, charts, or side-by-side columns"
              ],
              "missingTechnologies": ["Docker", "AWS (EC2/S3)", "GitHub Actions (CI/CD)", "TypeScript"],
              "skillPriorityRanking": [
                { "skill": "Docker", "priority": "High", "timeToLearn": "3-5 days" },
                { "skill": "TypeScript", "priority": "Medium", "timeToLearn": "5-7 days" },
                { "skill": "AWS Basics", "priority": "Medium", "timeToLearn": "4-6 days" },
                { "skill": "CI/CD Pipelines", "priority": "Low", "timeToLearn": "3 days" }
              ],
              "explanationOfImportance": "Docker is crucial for containerizing microservices. TypeScript is heavily preferred for modern frontend React applications. AWS and CI/CD represent professional deployment and operations knowledge expected of fresh graduates.",
              "careerLevelCurrent": "Entry-Level / Intern",
              "careerLevelTarget": "Junior Software Engineer",
              "careerMissingCompetencies": ["Containerization", "Cloud Deployments", "Frontend Type Safety (TypeScript)"],
              "careerTimeToReach": "30 Days",
              "applicationReadinessScore": 72
            }
            """;
        } else if (prompt.contains("GENERATE_ROADMAP")) {
            return """
            {
              "readinessTimeline": "30 Days",
              "learningPlan30Days": [
                { "day": 1, "topic": "Docker Basics", "details": "Understand images, containers, and run simple node/postgres containers." },
                { "day": 3, "topic": "Dockerfile Creation", "details": "Write Dockerfiles for Spring Boot and React applications." },
                { "day": 5, "topic": "Docker Compose", "details": "Learn multi-container configuration and run backend + database locally." },
                { "day": 8, "topic": "TypeScript Fundamentals", "details": "Learn syntax, interfaces, types, and generic typing." },
                { "day": 12, "topic": "React with TypeScript", "details": "Migrate a small React application to TypeScript." },
                { "day": 15, "topic": "AWS EC2 and S3", "details": "Launch instances, host files, and configure security groups." },
                { "day": 20, "topic": "GitHub Actions", "details": "Create simple workflows to build, test, and package application code." },
                { "day": 25, "topic": "End-to-end Deployments", "details": "Configure Render or fly.io deployments with database integrations." },
                { "day": 30, "topic": "Final Project Refactoring", "details": "Apply Docker, TS, and CI/CD to your E-Commerce project." }
              ],
              "weeklyMilestones": [
                { "week": 1, "goal": "Master Containerization with Docker", "deliverable": "Run a multi-container Docker Compose stack containing Spring Boot and Postgres" },
                { "week": 2, "goal": "Bridge Frontend with TypeScript", "deliverable": "Refactor React components with type definitions and build successfully" },
                { "week": 3, "goal": "Learn Cloud & CI/CD", "deliverable": "Build a CI/CD build pipeline that tests and reports status on git push" },
                { "week": 4, "goal": "Deployment & Review", "deliverable": "Host the application on Render with automated DB migrations" }
              ],
              "learningSequence": [
                "Containerization (Docker)",
                "Frontend Safety (TypeScript)",
                "Cloud Basics (AWS)",
                "DevOps Automation (CI/CD)"
              ]
            }
            """;
        } else if (prompt.contains("GENERATE_INTERVIEW_PACK")) {
            return """
            {
              "technicalQuestions": [
                {
                  "question": "What is the difference between @Component, @Service, and @Repository in Spring Boot?",
                  "answer": "@Component is the generic stereotype for any Spring-managed component. @Service and @Repository are specializations of @Component. @Service is used in the service layer (business logic), and @Repository is used in the persistence layer (encapsulates database storage and translates DB exceptions)."
                },
                {
                  "question": "Explain how JPA manages entity states (Transient, Managed, Detached, Removed).",
                  "answer": "Transient entities are created in memory but not associated with a database session. Managed entities are active in the current Hibernate Session, and changes are tracked. Detached entities were managed but their session closed. Removed entities are scheduled for deletion on commit."
                }
              ],
              "hrQuestions": [
                {
                  "question": "Tell me about a challenging technical bug you faced and how you solved it.",
                  "answer": "Answer structure: State the Context (e.g. Intern project API slow), the Task (find memory leak), the Action (used VisualVM to profile heaps and found unclosed resources), and the Result (fixed connection pool settings, API response time improved by 40%)."
                }
              ],
              "projectQuestions": [
                {
                  "question": "How did you ensure microservices communication in your E-Commerce project?",
                  "answer": "We implemented synchronous HTTP REST communication using Spring Cloud OpenFeign, and handled service discovery using Netflix Eureka. We also configured a Gateway to act as the single entry point routing requests."
                }
              ]
            }
            """;
        } else {
            return """
            {
              "answer": "Based on your current career report, you have a solid foundation in Spring Boot and Java. Your primary barrier to a Junior Software Engineer position is deployment and containerization (Docker, AWS) and frontend TypeScript. I recommend completing the 30-day learning roadmap to secure these skills and updating your resume to showcase active deployment configurations."
            }
            """;
        }
    }
}
