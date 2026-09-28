package com.recruitment.platform.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class DenseEmbeddingService implements EmbeddingService {

    public static final int VECTOR_DIMENSION = 128;
    private final ObjectMapper objectMapper;

    // Semantic domain anchors
    private static final Map<String, Integer> DOMAIN_CENTERS = new HashMap<>();
    private static final Map<String, Integer> DOMAIN_SLOTS = new HashMap<>();

    static {
        // Backend & Java (0-15)
        registerKeywords(0, 15, "java", "spring", "springboot", "hibernate", "jpa", "jvm", "kotlin", "maven", "gradle",
                "quarkus", "backend");
        // Cloud & Containers (16-31)
        registerKeywords(16, 31, "aws", "gcp", "azure", "docker", "kubernetes", "k8s", "terraform", "helm", "cloud",
                "serverless", "platforms");
        // Database & Storage (32-47)
        registerKeywords(32, 47, "postgresql", "postgres", "mysql", "mongodb", "redis", "cassandra", "dynamodb", "sql",
                "nosql", "elasticsearch");
        // Distributed Systems & Messaging (48-63)
        registerKeywords(48, 63, "kafka", "rabbitmq", "microservices", "grpc", "rest", "api", "distributed", "event",
                "messaging", "queues", "systems");
        // Frontend & Web (64-79)
        registerKeywords(64, 79, "react", "typescript", "javascript", "angular", "vue", "html", "css", "tailwind",
                "vite", "nextjs", "node");
        // AI / ML / Data (80-95)
        registerKeywords(80, 95, "python", "pytorch", "tensorflow", "machine", "learning", "nlp", "llm", "ai", "pandas",
                "spark", "hadoop");
        // DevOps / CI/CD / Linux (96-111)
        registerKeywords(96, 111, "linux", "ci", "cd", "jenkins", "git", "github", "gitlab", "bash", "prometheus",
                "grafana", "devops");
        // System Design / Security / Architecture (112-127)
        registerKeywords(112, 127, "architecture", "scalability", "resilience", "security", "oauth", "jwt",
                "concurrency", "performance", "lead", "architect");
    }

    private static void registerKeywords(int startIdx, int endIdx, String... words) {
        for (int i = 0; i < words.length; i++) {
            int slot = startIdx + (i % (endIdx - startIdx + 1));
            DOMAIN_CENTERS.put(words[i].toLowerCase(), startIdx);
            DOMAIN_SLOTS.put(words[i].toLowerCase(), slot);
        }
    }

    public DenseEmbeddingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public float[] generateEmbedding(String text) {
        float[] vector = new float[VECTOR_DIMENSION];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String lower = text.toLowerCase();
        String[] tokens = lower.split("[^a-zA-Z0-9+#]+");

        Map<Integer, Float> activations = new HashMap<>();

        for (String token : tokens) {
            if (token.length() < 2)
                continue;

            // Direct domain anchor hit
            if (DOMAIN_CENTERS.containsKey(token)) {
                int center = DOMAIN_CENTERS.get(token);
                int slot = DOMAIN_SLOTS.get(token);
                activations.put(center, activations.getOrDefault(center, 0.0f) + 2.0f);
                activations.put(slot, activations.getOrDefault(slot, 0.0f) + 1.0f);
            }

            // Word hash projection for vocabulary generalization
            int hash = Math.abs(token.hashCode());
            int primarySlot = hash % VECTOR_DIMENSION;
            int secondarySlot = (hash / VECTOR_DIMENSION) % VECTOR_DIMENSION;

            activations.put(primarySlot, activations.getOrDefault(primarySlot, 0.0f) + 0.25f);
            activations.put(secondarySlot, activations.getOrDefault(secondarySlot, 0.0f) + 0.10f);
        }

        // Apply non-linear activation (tanh) and populate vector
        for (Map.Entry<Integer, Float> entry : activations.entrySet()) {
            int idx = entry.getKey();
            float val = entry.getValue();
            vector[idx] = (float) Math.tanh(val);
        }

        // Normalize to unit length (L2 norm = 1.0)
        normalizeVector(vector);
        return vector;
    }

    @Override
    public double computeCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA <= 0.0 || normB <= 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        // Bound within [0.0, 1.0] for matching engine
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    @Override
    public String serializeVector(float[] vector) {
        if (vector == null)
            return "[]";
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            return Arrays.toString(vector);
        }
    }

    @Override
    public float[] deserializeVector(String vectorJson) {
        if (vectorJson == null || vectorJson.isBlank()) {
            return new float[VECTOR_DIMENSION];
        }
        try {
            List<Double> list = objectMapper.readValue(vectorJson, new TypeReference<List<Double>>() {
            });
            float[] result = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i).floatValue();
            }
            return result;
        } catch (IOException e) {
            // Fallback for comma separated brackets
            String clean = vectorJson.replace("[", "").replace("]", "").trim();
            if (clean.isEmpty())
                return new float[VECTOR_DIMENSION];
            String[] parts = clean.split(",");
            float[] result = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i].trim());
            }
            return result;
        }
    }

    @Override
    public String computeSha256(String text) {
        if (text == null)
            text = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(text.hashCode());
        }
    }

    private void normalizeVector(float[] vector) {
        double sumSq = 0.0;
        for (float v : vector) {
            sumSq += v * v;
        }
        if (sumSq > 0.0) {
            float norm = (float) Math.sqrt(sumSq);
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
    }
}
