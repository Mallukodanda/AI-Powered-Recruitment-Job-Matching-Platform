package com.recruitment.platform.service.storage;

public record StoredFileInfo(
    String storageKey,
    String originalFilename,
    long fileSize,
    String contentType,
    String sha256Hash
) {}
