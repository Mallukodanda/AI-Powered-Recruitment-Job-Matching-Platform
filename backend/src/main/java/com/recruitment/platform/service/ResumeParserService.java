package com.recruitment.platform.service;

import com.recruitment.platform.dto.ResumeParseResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}", Pattern.CASE_INSENSITIVE);

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");

    private static final Pattern EXP_PATTERN = Pattern.compile(
            "(\\d{1,2})\\+?\\s*(?:years?|yrs?)(?:\\s+of\\s+experience)?", Pattern.CASE_INSENSITIVE);

    // Canonical skill dictionary and aliases
    private static final Map<String, String> SKILL_ALIASES = new LinkedHashMap<>();

    static {
        // Backend & Languages
        addSkill("Java", "java", "core java", "j2ee");
        addSkill("Spring Boot", "spring boot", "springboot", "spring-boot", "spring framework", "spring");
        addSkill("Python", "python", "python3", "py");
        addSkill("JavaScript", "javascript", "js", "ecmascript");
        addSkill("TypeScript", "typescript", "ts");
        addSkill("C++", "c++", "cpp");
        addSkill("Go", "golang", "go");
        addSkill("Rust", "rust");
        addSkill("C#", "c#", "csharp", ".net");

        // Frontend
        addSkill("React", "react", "react.js", "reactjs");
        addSkill("Vue.js", "vue", "vue.js", "vuejs");
        addSkill("Angular", "angular", "angularjs");
        addSkill("HTML5", "html", "html5");
        addSkill("CSS3", "css", "css3", "sass", "scss");
        addSkill("Vite", "vite", "vitejs");
        addSkill("Redux", "redux", "redux toolkit");
        addSkill("TailwindCSS", "tailwind", "tailwindcss");

        // Cloud & DevOps
        addSkill("Docker", "docker", "containerization");
        addSkill("Kubernetes", "kubernetes", "k8s");
        addSkill("AWS", "aws", "amazon web services");
        addSkill("GCP", "gcp", "google cloud", "google cloud platform");
        addSkill("Azure", "azure", "microsoft azure");
        addSkill("Terraform", "terraform", "iac");
        addSkill("CI/CD", "ci/cd", "cicd", "jenkins", "github actions", "gitlab ci");
        addSkill("Linux", "linux", "unix", "ubuntu");
        addSkill("Prometheus", "prometheus");
        addSkill("Grafana", "grafana");

        // Databases
        addSkill("PostgreSQL", "postgresql", "postgres");
        addSkill("MySQL", "mysql");
        addSkill("MongoDB", "mongodb", "mongo");
        addSkill("Redis", "redis");
        addSkill("Elasticsearch", "elasticsearch");

        // Architecture & APIs
        addSkill("REST API", "rest", "restful", "rest api", "restful api");
        addSkill("Microservices", "microservices", "microservice architecture");
        addSkill("GraphQL", "graphql");
        addSkill("Kafka", "kafka", "apache kafka");
        addSkill("RabbitMQ", "rabbitmq");
        addSkill("Git", "git", "github", "gitlab");

        // AI / ML / Data Science
        addSkill("Machine Learning", "machine learning", "ml");
        addSkill("Deep Learning", "deep learning", "dl");
        addSkill("NLP", "nlp", "natural language processing");
        addSkill("PyTorch", "pytorch");
        addSkill("TensorFlow", "tensorflow");
        addSkill("Transformers", "transformers", "huggingface");
        addSkill("LLMs", "llm", "llms", "large language models");
        addSkill("Vector Databases", "vector database", "vector databases", "pinecone", "chromadb", "weaviate", "qdrant");
        addSkill("Semantic Search", "semantic search");
    }

    private static void addSkill(String canonical, String... aliases) {
        SKILL_ALIASES.put(canonical.toLowerCase(), canonical);
        for (String alias : aliases) {
            SKILL_ALIASES.put(alias.toLowerCase(), canonical);
        }
    }

    /**
     * Parse text from uploaded MultipartFile (PDF, DOCX, or TXT)
     */
    public ResumeParseResult parseResumeFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String filename = originalFilename != null ? originalFilename.toLowerCase() : "";
        String text;

        if (filename.endsWith(".pdf")) {
            text = extractTextFromPdf(file.getInputStream());
        } else if (filename.endsWith(".docx")) {
            text = extractTextFromDocx(file.getInputStream());
        } else {
            text = new String(file.getBytes());
        }

        return parseResumeText(text);
    }

    /**
     * Parse structured data from raw resume text
     */
    public ResumeParseResult parseResumeText(String text) {
        ResumeParseResult result = new ResumeParseResult();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        result.setRawText(text);

        // Extract Email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            result.setExtractedEmail(emailMatcher.group().trim());
        }

        // Extract Phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            result.setExtractedPhone(phoneMatcher.group().trim());
        }

        // Extract Years of Experience
        Matcher expMatcher = EXP_PATTERN.matcher(text);
        int maxYears = 0;
        while (expMatcher.find()) {
            try {
                int years = Integer.parseInt(expMatcher.group(1));
                if (years > maxYears && years <= 40) {
                    maxYears = years;
                }
            } catch (NumberFormatException ignored) {}
        }
        result.setEstimatedExperienceYears(maxYears > 0 ? maxYears : 2);

        // Extract Education
        String lowerText = text.toLowerCase();
        if (lowerText.contains("ph.d") || lowerText.contains("doctorate") || lowerText.contains("phd")) {
            result.setExtractedEducation("Ph.D. / Doctorate");
        } else if (lowerText.contains("master") || lowerText.contains("m.s.") || lowerText.contains("msc") || lowerText.contains("m.tech")) {
            result.setExtractedEducation("Master's Degree");
        } else if (lowerText.contains("bachelor") || lowerText.contains("b.s.") || lowerText.contains("bsc") || lowerText.contains("b.tech") || lowerText.contains("b.e.")) {
            result.setExtractedEducation("Bachelor's Degree");
        } else {
            result.setExtractedEducation("Higher Education / Certificate");
        }

        // Extract Skills
        Set<String> detectedSkills = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : SKILL_ALIASES.entrySet()) {
            String keyword = entry.getKey();
            Pattern wordPattern = Pattern.compile("(?:^|[^a-zA-Z0-9+#])" + Pattern.quote(keyword) + "(?:$|[^a-zA-Z0-9+#])", Pattern.CASE_INSENSITIVE);
            if (wordPattern.matcher(text).find()) {
                detectedSkills.add(entry.getValue());
            }
        }
        result.setExtractedSkills(new ArrayList<>(detectedSkills));

        // Infer Name from first lines if available
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 2 && trimmed.length() < 50 && !trimmed.contains("@") && !trimmed.contains("http") && !trimmed.matches(".*\\d.*")) {
                result.setExtractedName(trimmed);
                break;
            }
        }
        if (result.getExtractedName() == null && result.getExtractedEmail() != null) {
            result.setExtractedName(result.getExtractedEmail().split("@")[0].replace(".", " "));
        }

        // Infer Title
        if (lowerText.contains("full-stack") || lowerText.contains("full stack")) {
            result.setExtractedTitle("Full-Stack Software Engineer");
        } else if (lowerText.contains("machine learning") || lowerText.contains("nlp") || lowerText.contains("data scientist")) {
            result.setExtractedTitle("AI / Machine Learning Engineer");
        } else if (lowerText.contains("devops") || lowerText.contains("cloud architect") || lowerText.contains("sre")) {
            result.setExtractedTitle("Cloud DevOps Engineer");
        } else if (lowerText.contains("frontend") || lowerText.contains("ui/ux")) {
            result.setExtractedTitle("Frontend Software Engineer");
        } else {
            result.setExtractedTitle("Software Engineer");
        }

        return result;
    }

    private String extractTextFromPdf(InputStream is) throws Exception {
        try (PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream is) throws Exception {
        try (XWPFDocument document = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : document.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }
}
