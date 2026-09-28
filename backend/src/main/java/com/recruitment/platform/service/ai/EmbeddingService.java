package com.recruitment.platform.service.ai;

public interface EmbeddingService {
    float[] generateEmbedding(String text);
    double computeCosineSimilarity(float[] vectorA, float[] vectorB);
    String serializeVector(float[] vector);
    float[] deserializeVector(String vectorJson);
    String computeSha256(String text);
}
