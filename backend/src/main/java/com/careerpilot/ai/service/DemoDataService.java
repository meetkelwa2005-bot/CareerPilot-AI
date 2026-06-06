package com.careerpilot.ai.service;

import com.careerpilot.ai.entity.*;
import com.careerpilot.ai.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
public class DemoDataService {

    private final ResumeRepository resumeRepository;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final LearningRoadmapRepository learningRoadmapRepository;
    private final InterviewPackRepository interviewPackRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    public DemoDataService(ResumeRepository resumeRepository,
                           JobAnalysisRepository jobAnalysisRepository,
                           LearningRoadmapRepository learningRoadmapRepository,
                           InterviewPackRepository interviewPackRepository,
                           ChatHistoryRepository chatHistoryRepository) {
        this.resumeRepository = resumeRepository;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.learningRoadmapRepository = learningRoadmapRepository;
        this.interviewPackRepository = interviewPackRepository;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Transactional
    public JobAnalysis createDemoDataset() {
        log.info("Generating standard CareerPilot AI Demo Dataset...");

        // 1. Create Sample Resume
        Resume resume = Resume.builder()
                .name("Alex Mercer")
                .email("alex.mercer@stateu.edu")
                .phone("+1-555-0144")
                .education("""
                        [
                          {
                            "degree": "B.Tech in Computer Science & Engineering",
                            "institution": "State Technical Institute",
                            "duration": "2022 - 2026"
                          }
                        ]
                        """)
                .skills("[\"Java\", \"Spring Boot\", \"React\", \"JavaScript\", \"SQL\", \"REST APIs\", \"Git\", \"HTML/CSS\"]")
                .projects("""
                        [
                          {
                            "title": "Campus Marketplace Platform",
                            "description": "Built a peer-to-peer trading system for students using React and Spring Boot. Integrated JWT authentication and a PostgreSQL DB.",
                            "technologies": ["Java", "Spring Boot", "React", "PostgreSQL", "JWT"]
                          }
                        ]
                        """)
                .experience("""
                        [
                          {
                            "role": "Backend Intern",
                            "company": "AppForge Technologies",
                            "duration": "May 2025 - July 2025 (3 months)",
                            "description": "Designed database schemas and optimized JPA queries. Wrote REST APIs for user verification modules."
                          }
                        ]
                        """)
                .rawText("Alex Mercer B.Tech Computer Science student at State Technical Institute. Experienced in Java, Spring Boot, React, SQL. Interned as Backend Developer at AppForge. Created Campus Marketplace.")
                .createdAt(LocalDateTime.now())
                .build();
        resume = resumeRepository.save(resume);

        // 2. Create Sample Job Analysis (Matching Junior Fullstack Developer)
        String jobTitle = "Junior Fullstack Developer (Java & React)";
        String jobDescription = """
                We are looking for a Junior Full Stack Developer to join our agile engineering team.
                Requirements:
                - Strong programming skills in Java, Spring Boot, and SQL database design.
                - Hands-on frontend experience building responsive interfaces with React.
                - Knowledge of containerization using Docker is highly preferred.
                - Familiarity with TypeScript and modern CI/CD systems (GitHub Actions) is a plus.
                - Deployment experience with cloud environments (AWS, Heroku or Render).
                """;

        JobAnalysis analysis = JobAnalysis.builder()
                .resume(resume)
                .jobTitle(jobTitle)
                .jobDescription(jobDescription)
                .matchScore(76)
                .matchedSkills("[\"Java\", \"Spring Boot\", \"React\", \"JavaScript\", \"SQL\", \"Git\"]")
                .missingSkills("[\"Docker\", \"TypeScript\", \"AWS\", \"CI/CD Pipelines\"]")
                .strengths("""
                        [
                          "Strong alignment with backend requirements (Java & Spring Boot)",
                          "Direct experience with React for front-end interface development",
                          "Prior database layout experience in a real-world internship setting"
                        ]
                        """)
                .weaknesses("""
                        [
                          "No containerization details (Docker) present on the resume",
                          "Missing cloud deployment details and infrastructure awareness",
                          "No experience with typed frontend development (TypeScript)"
                        ]
                        """)
                .atsScore(72)
                .missingKeywords("[\"Docker\", \"TypeScript\", \"AWS\", \"CI/CD\", \"Agile\"]")
                .resumeImprovements("""
                        [
                          "Add a dedicated 'Infrastructure' section in your project highlighting PostgreSQL and Docker setup.",
                          "Specify that your Campus Marketplace app is deployed (e.g. mention Render/Vercel URL).",
                          "Rewrite internship metrics using the STAR method (e.g., 'Optimized JPA queries, lowering average latency by 20%')."
                        ]
                        """)
                .atsRecommendations("""
                        [
                          "Keep your resume to a single-page layout to match graduate standards.",
                          "Avoid fancy layouts or graphics that confuse older parsing algorithms.",
                          "Use standard bullet points instead of custom paragraph texts."
                        ]
                        """)
                .missingTechnologies("[\"Docker\", \"TypeScript\", \"AWS Basics\", \"GitHub Actions\"]")
                .skillPriorityRanking("""
                        [
                          { "skill": "Docker", "priority": "High", "timeToLearn": "3-4 days" },
                          { "skill": "TypeScript", "priority": "Medium", "timeToLearn": "5 days" },
                          { "skill": "AWS (EC2/S3)", "priority": "Medium", "timeToLearn": "4 days" },
                          { "skill": "GitHub Actions", "priority": "Low", "timeToLearn": "3 days" }
                        ]
                        """)
                .explanationOfImportance("Docker is essential for setting up local development databases and microservice instances consistently. TypeScript is widely preferred in modern React codebases to enforce type safety. Cloud and CI/CD skills enable automated deployment pipelines.")
                .careerLevelCurrent("Entry-Level / Graduate Intern")
                .careerLevelTarget("Junior Fullstack Developer")
                .careerMissingCompetencies("[\"Containerization (Docker)\", \"Cloud hosting & environments\", \"Frontend Type Safety\"]")
                .careerTimeToReach("30 Days")
                .applicationReadinessScore(74)
                .createdAt(LocalDateTime.now())
                .build();
        analysis = jobAnalysisRepository.save(analysis);

        // 3. Create Sample Learning Roadmap
        LearningRoadmap roadmap = LearningRoadmap.builder()
                .jobAnalysis(analysis)
                .learningPlan30Days("""
                        [
                          { "day": 1, "topic": "Docker Fundamentals", "details": "Understand container concepts. Run standard PostgreSQL and Nginx containers locally." },
                          { "day": 4, "topic": "Writing custom Dockerfiles", "details": "Create Dockerfile configurations to containerize a Spring Boot application and a React Vite build." },
                          { "day": 7, "topic": "Docker Compose Integration", "details": "Create a docker-compose.yml file linking your React frontend, Spring Boot backend, and a PostgreSQL database." },
                          { "day": 11, "topic": "TypeScript Core Concepts", "details": "Learn variables, interfaces, types, and strict compilation checks in TypeScript." },
                          { "day": 15, "topic": "React with TypeScript", "details": "Configure React components with types for props, state, and event handlers. Build a simple dashboard." },
                          { "day": 20, "topic": "AWS Essentials", "details": "Learn EC2 instance configuration, S3 bucket storage, and basic IAM security controls." },
                          { "day": 25, "topic": "CI/CD Pipeline with GitHub Actions", "details": "Write a workflow that compiles code, runs test suites, and builds a docker image on every repository git push." },
                          { "day": 30, "topic": "Final Deployment and Portfolio Update", "details": "Host your marketplace application on Render or AWS EC2, and update your resume with these additions." }
                        ]
                        """)
                .weeklyMilestones("""
                        [
                          { "week": 1, "goal": "Containerization with Docker", "deliverable": "Create a multi-container local stack utilizing Docker Compose." },
                          { "week": 2, "goal": "Frontend Type-safety with TypeScript", "deliverable": "Refactor your React-based Campus Marketplace components to TypeScript." },
                          { "week": 3, "goal": "CI/CD & Cloud Basics", "deliverable": "Configure a GitHub Actions pipeline to run tests automatically." },
                          { "week": 4, "goal": "Production Deployments", "deliverable": "Host the entire stack live on Render with an active PostgreSQL database." }
                        ]
                        """)
                .learningSequence("[\"Docker\", \"TypeScript\", \"AWS Basics\", \"GitHub Actions CI/CD\"]")
                .readinessTimeline("30 Days")
                .createdAt(LocalDateTime.now())
                .build();
        learningRoadmapRepository.save(roadmap);

        // 4. Create Sample Interview Pack
        InterviewPack pack = InterviewPack.builder()
                .jobAnalysis(analysis)
                .technicalQuestions("""
                        [
                          {
                            "question": "What is the difference between constructor injection and setter injection in Spring, and which is preferred?",
                            "answer": "Constructor injection is preferred because it allows making dependencies immutable (using final fields) and guarantees that the object is fully initialized upon construction. Setter injection is only useful for optional dependencies that can be changed later."
                          },
                          {
                            "question": "Explain React's Virtual DOM and how key props help the reconciliation process.",
                            "answer": "React maintains a virtual representation of the UI. During updates, it creates a new virtual DOM tree and diffs it with the old one. The 'key' prop helps React uniquely identify elements in lists so it only re-renders modified components rather than re-creating the entire list."
                          },
                          {
                            "question": "What is a Docker image vs a Docker container, and how do you optimize a Dockerfile?",
                            "answer": "A Docker image is a read-only template containing instructions to build a container. A container is a runnable instance of an image. To optimize a Dockerfile, you can use multi-stage builds (e.g., compiling Java code with a JDK image and running it with a lightweight JRE image) and structure commands to utilize layer caching."
                          }
                        ]
                        """)
                .hrQuestions("""
                        [
                          {
                            "question": "How do you handle a situation where you are asked to work with a technology you have never used before?",
                            "answer": "I would start by reading official documentation and building a simple 'Hello World' proof-of-concept. I would check the team's existing codebase for implementation patterns and ask senior developers for guidance to ensure I follow best practices."
                          },
                          {
                            "question": "Tell me about a time you had to manage tight deadlines for a university project.",
                            "answer": "Focus on planning and task delegation. Describe how you broke down requirements, tracked progress daily, communicated early when blockers arose, and focused on delivering a functional MVP first."
                          }
                        ]
                        """)
                .projectQuestions("""
                        [
                          {
                            "question": "In your Campus Marketplace project, how did you implement and secure JWT authentication?",
                            "answer": "We implemented a custom Spring Security filter that intercepts requests, extracts the JWT from the Authorization header, validates the token using a secret key, and sets the Authentication context in SecurityContextHolder."
                          },
                          {
                            "question": "What database design considerations did you implement in the Campus Marketplace database?",
                            "answer": "We configured a relational database schema in PostgreSQL. We set up tables for Users, Products, and Transactions with foreign keys to enforce referential integrity. We also added indices on product categories and search query columns to optimize retrieval times."
                          }
                        ]
                        """)
                .createdAt(LocalDateTime.now())
                .build();
        interviewPackRepository.save(pack);

        // 5. Add welcome message from Career Twin Assistant
        ChatHistory welcomeChat = ChatHistory.builder()
                .jobAnalysis(analysis)
                .sender("ASSISTANT")
                .message("Hello Alex! I am your **CareerTwin Assistant**. I have analyzed your resume against the **Junior Fullstack Developer** role. Your current readiness is **74%**, and you're very close to qualifying. Ask me questions like: *'How can I improve my ATS score?'* or *'What is the highest priority skill I should learn first?'* to get started!")
                .createdAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(welcomeChat);

        return analysis;
    }
}
