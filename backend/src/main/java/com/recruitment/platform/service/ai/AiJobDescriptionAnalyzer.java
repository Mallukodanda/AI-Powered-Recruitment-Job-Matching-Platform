package com.recruitment.platform.service.ai;

import com.recruitment.platform.dto.StructuredJobData;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AiJobDescriptionAnalyzer implements JobDescriptionAnalyzer {

    private static final List<String> TECH_TAXONOMY = List.of(
            "Java", "Spring Boot", "Python", "React", "TypeScript", "JavaScript", "Docker", "Kubernetes",
            "AWS", "GCP", "Azure", "PostgreSQL", "MySQL", "MongoDB", "Redis", "Kafka", "Microservices",
            "REST API", "GraphQL", "Git", "CI/CD", "Linux", "Terraform", "Machine Learning", "NLP",
            "PyTorch", "TensorFlow", "Node.js", "C#", "C++", "Go", "Rust", "Angular", "Vue.js",
            "SQL", "Elasticsearch", "RabbitMQ", "Spark", "Hadoop", "Flink", "Snowflake"
    );

    private static final Pattern EXP_YEARS_PATTERN = Pattern.compile(
            "(\\d{1,2})\\+?\\s*(?:-|to)?\\s*(\\d{1,2})?\\s*(?:years?|yrs?)(?:\\s+of\\s+(?:relevant\\s+)?experience)?",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getProviderName() {
        return "ENTERPRISE_STRUCTURAL_AI";
    }

    @Override
    public String getModelName() {
        return "job-decomposer-v2";
    }

    @Override
    public StructuredJobData analyzeJob(String title,
                                        String description,
                                        String requirements,
                                        String rawSkills,
                                        Integer minExp,
                                        String education) {
        StructuredJobData data = new StructuredJobData();
        data.setJobTitle(title != null ? title.trim() : "Software Role");

        String combinedText = ((title != null ? title : "") + "\n" +
                (description != null ? description : "") + "\n" +
                (requirements != null ? requirements : "") + "\n" +
                (rawSkills != null ? rawSkills : "")).trim();

        // 1. Role Summary
        data.setRoleSummary(buildRoleSummary(title, description));

        // 2. Extract Technologies & Skills
        Set<String> allDetectedSkills = extractTaxonomySkills(combinedText);
        if (rawSkills != null && !rawSkills.isBlank()) {
            for (String raw : rawSkills.split(",")) {
                if (!raw.isBlank()) allDetectedSkills.add(raw.trim());
            }
        }

        // Partition into required vs preferred
        partitionSkills(allDetectedSkills, description, requirements, rawSkills, data);

        // All matched tools are technologies
        data.setTechnologies(new ArrayList<>(allDetectedSkills));

        // 3. Experience extraction
        extractExperience(combinedText, minExp, title, data);

        // 4. Education requirement
        extractEducation(combinedText, education, data);

        // 5. Responsibilities
        extractResponsibilities(description, data);

        // 6. Important requirements
        extractImportantRequirements(requirements, combinedText, data);

        return data;
    }

    private String buildRoleSummary(String title, String description) {
        if (description != null && !description.isBlank()) {
            String firstSentence = description.split("[.\\n]")[0].trim();
            if (firstSentence.length() > 20 && firstSentence.length() < 200) {
                return firstSentence;
            }
        }
        return "Key contributor role focusing on " + (title != null ? title : "engineering execution") + " within the organization.";
    }

    private Set<String> extractTaxonomySkills(String text) {
        Set<String> detected = new LinkedHashSet<>();
        for (String skill : TECH_TAXONOMY) {
            Pattern pattern = Pattern.compile("(?:^|[^a-zA-Z0-9+#])" + Pattern.quote(skill) + "(?:$|[^a-zA-Z0-9+#])", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                detected.add(skill);
            }
        }
        return detected;
    }

    private void partitionSkills(Set<String> skills, String desc, String reqs, String rawSkills, StructuredJobData data) {
        String lowerDesc = (desc != null ? desc.toLowerCase() : "");
        String lowerReqs = (reqs != null ? reqs.toLowerCase() : "");
        Set<String> rawSet = rawSkills != null
                ? Arrays.stream(rawSkills.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<String> required = new LinkedHashSet<>();
        Set<String> preferred = new LinkedHashSet<>();

        for (String skill : skills) {
            String lowerSkill = skill.toLowerCase();
            // Check if context nearby is "nice to have", "plus", "preferred"
            boolean isPreferred = isContextPreferred(lowerDesc, lowerSkill) || isContextPreferred(lowerReqs, lowerSkill);

            if (isPreferred && !rawSet.contains(skill)) {
                preferred.add(skill);
            } else {
                required.add(skill);
            }
        }

        if (required.isEmpty() && !skills.isEmpty()) {
            required.addAll(skills);
        }

        data.setRequiredSkills(new ArrayList<>(required));
        data.setPreferredSkills(new ArrayList<>(preferred));
    }

    private boolean isContextPreferred(String text, String skill) {
        if (text == null || text.isEmpty()) return false;
        int idx = text.indexOf(skill);
        if (idx == -1) return false;

        int start = Math.max(0, idx - 80);
        int end = Math.min(text.length(), idx + skill.length() + 80);
        String surrounding = text.substring(start, end);

        return surrounding.contains("preferred") ||
                surrounding.contains("nice to have") ||
                surrounding.contains("bonus") ||
                surrounding.contains("plus") ||
                surrounding.contains("desired");
    }

    private void extractExperience(String text, Integer minExp, String title, StructuredJobData data) {
        if (minExp != null && minExp > 0) {
            data.setMinExperienceYears(minExp);
        } else {
            Matcher m = EXP_YEARS_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    data.setMinExperienceYears(Integer.parseInt(m.group(1)));
                } catch (NumberFormatException ignored) {
                    data.setMinExperienceYears(2);
                }
            } else {
                data.setMinExperienceYears(2);
            }
        }

        int years = data.getMinExperienceYears();
        String lowerTitle = (title != null ? title.toLowerCase() : "");
        if (years >= 8 || lowerTitle.contains("principal") || lowerTitle.contains("lead") || lowerTitle.contains("architect")) {
            data.setTargetExperienceLevel("LEAD");
        } else if (years >= 5 || lowerTitle.contains("senior")) {
            data.setTargetExperienceLevel("SENIOR");
        } else if (years >= 2 || lowerTitle.contains("mid")) {
            data.setTargetExperienceLevel("MID");
        } else {
            data.setTargetExperienceLevel("JUNIOR");
        }
    }

    private void extractEducation(String text, String education, StructuredJobData data) {
        if (education != null && !education.isBlank()) {
            data.setEducationRequirement(education.trim().toUpperCase());
            return;
        }

        String lower = text.toLowerCase();
        if (lower.contains("ph.d") || lower.contains("doctorate")) {
            data.setEducationRequirement("PHD");
        } else if (lower.contains("master") || lower.contains("m.s.") || lower.contains("msc")) {
            data.setEducationRequirement("MASTER");
        } else if (lower.contains("bachelor") || lower.contains("b.s.") || lower.contains("bsc") || lower.contains("degree in computer science")) {
            data.setEducationRequirement("BACHELOR");
        } else {
            data.setEducationRequirement("ANY");
        }
    }

    private void extractResponsibilities(String description, StructuredJobData data) {
        List<String> list = new ArrayList<>();
        if (description != null) {
            String[] lines = description.split("\\r?\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    trimmed = trimmed.substring(1).trim();
                }
                if (trimmed.length() > 15 && trimmed.length() < 250 && isResponsibilityVerb(trimmed)) {
                    list.add(trimmed);
                    if (list.size() >= 5) break;
                }
            }
        }
        if (list.isEmpty()) {
            list.add("Design, develop, and maintain robust scalable services.");
            list.add("Collaborate cross-functionally with product managers and engineers.");
            list.add("Write clean, tested, and documented maintainable code.");
        }
        data.setResponsibilities(list);
    }

    private boolean isResponsibilityVerb(String text) {
        String lower = text.toLowerCase();
        return lower.startsWith("design") || lower.startsWith("develop") || lower.startsWith("build") ||
                lower.startsWith("lead") || lower.startsWith("maintain") || lower.startsWith("collaborate") ||
                lower.startsWith("implement") || lower.startsWith("architect") || lower.startsWith("ensure") ||
                lower.startsWith("optimize") || lower.startsWith("create") || lower.startsWith("manage");
    }

    private void extractImportantRequirements(String requirements, String fullText, StructuredJobData data) {
        List<String> list = new ArrayList<>();
        if (requirements != null) {
            String[] lines = requirements.split("\\r?\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                    trimmed = trimmed.substring(1).trim();
                }
                if (trimmed.length() > 10 && trimmed.length() < 200) {
                    list.add(trimmed);
                    if (list.size() >= 4) break;
                }
            }
        }

        if (list.isEmpty()) {
            String lower = fullText.toLowerCase();
            if (lower.contains("remote") || lower.contains("hybrid") || lower.contains("on-site")) {
                list.add("Work arrangement: Remote / Hybrid flexible scheduling.");
            }
            list.add("Demonstrated proficiency in core software engineering and system design.");
        }
        data.setImportantRequirements(list);
    }
}
