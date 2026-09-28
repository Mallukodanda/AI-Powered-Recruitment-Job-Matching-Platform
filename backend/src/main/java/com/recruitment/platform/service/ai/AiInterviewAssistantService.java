package com.recruitment.platform.service.ai;

import com.recruitment.platform.dto.InterviewPrepDto;
import com.recruitment.platform.dto.InterviewPrepQuestionDto;
import com.recruitment.platform.exception.ResourceNotFoundException;
import com.recruitment.platform.model.Candidate;
import com.recruitment.platform.model.Job;
import com.recruitment.platform.repository.CandidateRepository;
import com.recruitment.platform.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AiInterviewAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiInterviewAssistantService.class);

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;

    public AiInterviewAssistantService(JobRepository jobRepository,
                                       CandidateRepository candidateRepository) {
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Generates a comprehensive interview preparation guide with role-specific, technical,
     * behavioral HR, and probing follow-up questions tailored to the candidate and job requisition.
     */
    public InterviewPrepDto generateInterviewPrep(Long jobId, Long candidateId) {
        log.info("Generating AI interview preparation guide for job ID {} and candidate ID {}", jobId, candidateId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));

        return buildStructuredInterviewGuide(job, candidate);
    }

    private InterviewPrepDto buildStructuredInterviewGuide(Job job, Candidate candidate) {
        InterviewPrepDto dto = new InterviewPrepDto();
        dto.setCandidateId(candidate.getId());
        dto.setCandidateName(candidate.getFullName());
        dto.setCandidateTitle(candidate.getCurrentTitle());
        dto.setYearsExperience(candidate.getYearsExperience() != null ? candidate.getYearsExperience() : 0);
        dto.setJobId(job.getId());
        dto.setJobTitle(job.getTitle());

        // Parse skills from Job and Candidate
        Set<String> candidateSkills = parseSkills(candidate.getSkillsSummary());
        if (candidate.getResumeText() != null) {
            candidateSkills.addAll(extractKeywords(candidate.getResumeText()));
        }

        Set<String> jobSkills = parseSkills(job.getSkills());
        if (job.getRequirements() != null) {
            jobSkills.addAll(extractKeywords(job.getRequirements()));
        }

        // Match vs Missing
        List<String> matched = jobSkills.stream()
                .filter(req -> candidateSkills.stream().anyMatch(cs -> cs.equalsIgnoreCase(req)))
                .sorted()
                .collect(Collectors.toList());

        List<String> missing = jobSkills.stream()
                .filter(req -> candidateSkills.stream().noneMatch(cs -> cs.equalsIgnoreCase(req)))
                .sorted()
                .collect(Collectors.toList());

        dto.setMatchedSkills(matched);
        dto.setMissingOrGrowthSkills(missing);

        // Determine candidate seniority tier
        int exp = dto.getYearsExperience();
        String seniority = (exp >= 8) ? "STAFF" : (exp >= 5) ? "ADVANCED" : (exp >= 2) ? "INTERMEDIATE" : "BEGINNER";

        // 1. Preparation Topics
        List<String> topics = new ArrayList<>();
        topics.add(job.getTitle() + " Architecture & Design Patterns");
        if (!matched.isEmpty()) {
            topics.add("In-depth evaluation of primary matched core skills: " + String.join(", ", matched));
        }
        topics.add("Distributed Systems Resilience, Fault Tolerance & Performance Tuning");
        topics.add("Database Concurrency, Transaction Isolation & Query Plan Optimization");
        if (!missing.isEmpty()) {
            topics.add("Growth adaptability and architectural fundamentals on: " + String.join(", ", missing));
        }
        topics.add("Behavioral Alignment: Cross-functional Collaboration, Mentorship, and Incident Management");
        dto.setPreparationTopics(topics);

        // 2. Technical Questions (tailored to matched skills)
        List<InterviewPrepQuestionDto> techQuestions = new ArrayList<>();
        int qId = 1;
        for (String skill : matched) {
            techQuestions.add(createTechnicalQuestion(qId++, skill, seniority));
        }
        if (techQuestions.isEmpty()) {
            techQuestions.add(createTechnicalQuestion(qId++, "Core Software Engineering", seniority));
            techQuestions.add(createTechnicalQuestion(qId++, "API Design", seniority));
        }
        dto.setTechnicalQuestions(techQuestions);

        // 3. Role-Specific Questions (tailored to job title and requisition context)
        List<InterviewPrepQuestionDto> roleQuestions = new ArrayList<>();
        roleQuestions.add(new InterviewPrepQuestionDto(
                "RQ-" + (qId++),
                "ROLE_SPECIFIC",
                String.format("As a %s, how would you architect a zero-downtime database schema migration in an active production microservices ecosystem?", job.getTitle()),
                "System Architecture & Migration",
                seniority,
                List.of("Blue-Green deployment", "Expand and Contract pattern", "Backward compatibility", "Database locks minimization"),
                "Evaluates candidate's practical experience with enterprise production systems and distributed database evolution.",
                "Directly maps to production stability requirements for " + job.getTitle()
        ));
        roleQuestions.add(new InterviewPrepQuestionDto(
                "RQ-" + (qId++),
                "ROLE_SPECIFIC",
                String.format("Walk through an end-to-end incident troubleshooting scenario where latency in '%s' spiked by 400%% under peak traffic.", job.getTitle()),
                "Observability & Production Diagnostics",
                seniority,
                List.of("Metrics & APM tracing", "Thread dumps / CPU profiling", "Database connection pool exhaustion", "Network partition"),
                "Differentiates candidates who rely on guesswork from engineers who apply systematic telemetry and root-cause analysis.",
                "Ensures the candidate can safeguard production SLA commitments."
        ));
        dto.setRoleSpecificQuestions(roleQuestions);

        // 4. Behavioral & HR Questions (STAR framework)
        List<InterviewPrepQuestionDto> hrQuestions = new ArrayList<>();
        hrQuestions.add(new InterviewPrepQuestionDto(
                "HR-1",
                "BEHAVIORAL_HR",
                "Tell me about a time when you strongly disagreed with a technical or architectural decision proposed by your team lead. How did you handle the discussion, and what was the outcome?",
                "Constructive Disagreement & Collaboration",
                seniority,
                List.of("STAR framework", "Data-driven persuasion", "Disagree and commit", "Team psychological safety"),
                "Candidate should demonstrate high emotional intelligence, intellectual humility, and commitment to the team's shared goals.",
                "Critical for high-performing engineering culture."
        ));
        hrQuestions.add(new InterviewPrepQuestionDto(
                "HR-2",
                "BEHAVIORAL_HR",
                "Describe a situation where a project deliverable was at severe risk of missing an agreed business deadline. What steps did you take to manage expectations and deliver value?",
                "Prioritization & Stakeholder Communication",
                seniority,
                List.of("Scope negotiation", "Early stakeholder transparency", "Risk mitigation", "Preventing burnout"),
                "Assesses proactive accountability versus silent failure or blame shifting.",
                "Crucial for predictability and cross-functional trust."
        ));
        dto.setHrQuestions(hrQuestions);

        // 5. Follow-Up Probing Questions (target resume statements & skill gaps)
        List<InterviewPrepQuestionDto> followUpQuestions = new ArrayList<>();
        if (!missing.isEmpty()) {
            String gapSkill = missing.get(0);
            followUpQuestions.add(new InterviewPrepQuestionDto(
                    "FQ-1",
                    "FOLLOW_UP_PROBE",
                    String.format("The job highlights '%s', which does not appear prominently in your recent background. Have you worked with similar paradigms, and how would you bridge that gap within the first 30 days?", gapSkill),
                    "Adaptability & Technical Learning Velocity",
                    seniority,
                    List.of("Transferable conceptual patterns", "First-principles engineering", "Rapid onboarding plan"),
                    "Tests learning velocity, curiosity, and adaptability to new technology stacks.",
                    "Addresses identified skill gap: " + gapSkill
            ));
        }

        String candidateTitle = candidate.getCurrentTitle() != null ? candidate.getCurrentTitle() : "Software Engineer";
        followUpQuestions.add(new InterviewPrepQuestionDto(
                "FQ-2",
                "FOLLOW_UP_PROBE",
                String.format("In your role as %s, what was the most complex technical trade-off you had to make between delivery speed and technical debt? What would you do differently in retrospect?", candidateTitle),
                "Reflective Engineering Judgement",
                seniority,
                List.of("Technical debt management", "Engineering velocity", "Retrospective insight"),
                "Evaluates candidate's maturity, ability to learn from past trade-offs, and engineering rigor.",
                "Validates depth of experience claimed in resume profile."
        ));
        dto.setFollowUpQuestions(followUpQuestions);

        return dto;
    }

    private InterviewPrepQuestionDto createTechnicalQuestion(int id, String skill, String difficulty) {
        String cleanSkill = skill.trim();
        List<String> expected;
        String question;
        String criteria;

        switch (cleanSkill.toLowerCase()) {
            case "java":
                question = "Explain how the Java Memory Model handles visibility across threads via the 'volatile' keyword and compare it with Atomic references and synchronized blocks.";
                expected = List.of("Happens-before guarantee", "CPU memory cache invalidation", "Instruction reordering prevention", "CAS (Compare-and-Swap)");
                criteria = "Candidate should explain memory barriers, instruction reordering, and why volatile does not guarantee compound atomicity (e.g., count++).";
                break;
            case "spring boot":
                question = "Describe how Spring handles transaction propagation (e.g. REQUIRED vs REQUIRES_NEW) and what happens when a transactional method is called from within the same class.";
                expected = List.of("Spring AOP dynamic proxies", "Self-invocation bypass", "Transaction synchronization manager", "Rollback rules on unchecked vs checked exceptions");
                criteria = "Must explain the CGLIB/JDK dynamic proxy mechanism and how internal self-calls bypass the interceptor.";
                break;
            case "postgresql":
            case "sql":
                question = "How do B-Tree indexes work in relational databases, and what causes an index scan to degrade into a sequential table scan?";
                expected = List.of("Index cardinality", "Index-only scans vs heap fetches", "Functions on indexed columns", "High table selectivity threshold");
                criteria = "Candidate should discuss EXPLAIN ANALYZE output, index coverage, and planner cost estimates.";
                break;
            case "docker":
            case "kubernetes":
                question = "How do you design a containerized service with Kubernetes health probes (liveness, readiness, startup) to ensure zero dropped requests during rolling updates?";
                expected = List.of("SIGTERM graceful shutdown", "Readiness probe endpoint isolation", "Connection draining", "Pod termination lifecycle");
                criteria = "Candidate must describe how readiness probes control kube-proxy iptables endpoint routing.";
                break;
            case "kafka":
            case "rabbitmq":
                question = "Explain the trade-offs between at-least-once, at-most-once, and exactly-once processing semantics in distributed event streaming. How would you handle duplicate consumer deliveries?";
                expected = List.of("Consumer offset commit timing", "Idempotent consumer / deduplication table", "Transactional outbox", "Dead-letter queues");
                criteria = "Candidate should recognize that exactly-once end-to-end requires idempotent state storage.";
                break;
            default:
                question = String.format("Describe the architectural best practices and common performance pitfalls you have experienced when implementing %s in production systems.", cleanSkill);
                expected = List.of("Scalability bottlenecks", "Concurrency / locking", "Error handling & retries", "Profiling & telemetry");
                criteria = "Evaluates practical mastery, architectural depth, and failure-mode awareness.";
                break;
        }

        return new InterviewPrepQuestionDto(
                "TQ-" + id,
                "TECHNICAL",
                question,
                cleanSkill,
                difficulty,
                expected,
                criteria,
                "Assesses depth of core expertise in " + cleanSkill + " required for this position."
        );
    }

    private Set<String> parseSkills(String raw) {
        if (raw == null || raw.isBlank()) return new HashSet<>();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) return new HashSet<>();
        List<String> known = List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes",
                "Kafka", "Redis", "TypeScript", "React", "AWS", "Git", "REST API", "Microservices");
        Set<String> found = new HashSet<>();
        String lower = text.toLowerCase();
        for (String k : known) {
            if (lower.contains(k.toLowerCase())) {
                found.add(k);
            }
        }
        return found;
    }
}
