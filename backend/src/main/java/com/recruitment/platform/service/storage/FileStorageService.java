package com.recruitment.platform.service.storage;

import com.recruitment.platform.exception.FileValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx");
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "jar", "jsp", "php", "py", "js", "vbs", "dll", "bin", "com"
    );

    // Magic byte signatures
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_MAGIC_BYTES = new byte[]{0x50, 0x4B, 0x03, 0x04}; // PK\x03\x04 for DOCX

    private final Path rootStoragePath;

    public FileStorageService(@Value("${recruitment.storage.resume-dir:uploads/resumes}") String storageDir) {
        this.rootStoragePath = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + rootStoragePath, e);
        }
    }

    public StoredFileInfo storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileValidationException("File size exceeds maximum allowed threshold of 10MB");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.isBlank()) {
            throw new FileValidationException("Filename must not be empty");
        }

        // 1. Sanitize filename & prevent path traversal
        String cleanedFilename = StringUtils.cleanPath(rawFilename);
        if (cleanedFilename.contains("..") || cleanedFilename.contains("/") || cleanedFilename.contains("\\")) {
            throw new FileValidationException("Filename contains invalid path traversal sequences");
        }

        // Extract extension
        int extIndex = cleanedFilename.lastIndexOf('.');
        if (extIndex == -1 || extIndex == cleanedFilename.length() - 1) {
            throw new FileValidationException("File must have a valid extension (.pdf or .docx)");
        }
        String extension = cleanedFilename.substring(extIndex + 1).toLowerCase();

        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new FileValidationException("Executable or script files are strictly prohibited");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileValidationException("Unsupported file type ." + extension + ". Only PDF and DOCX documents are accepted.");
        }

        // 2. Deep inspection: Validate Magic Bytes (Do not trust client-reported MIME type)
        validateMagicBytes(file, extension);

        // 3. Compute SHA-256 for integrity verification
        String sha256Hash = computeSha256(file);

        // 4. Save to isolated storage location with secure randomized UUID filename
        String storageKey = UUID.randomUUID() + "." + extension;
        Path targetLocation = this.rootStoragePath.resolve(storageKey).normalize();

        if (!targetLocation.startsWith(this.rootStoragePath)) {
            throw new FileValidationException("Security violation: Target file path is outside designated storage directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file securely: " + e.getMessage(), e);
        }

        String contentType = "pdf".equals(extension) ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return new StoredFileInfo(storageKey, cleanedFilename, file.getSize(), contentType, sha256Hash);
    }

    public Path loadFilePath(String storageKey) {
        Path filePath = this.rootStoragePath.resolve(storageKey).normalize();
        if (!filePath.startsWith(this.rootStoragePath) || !Files.exists(filePath)) {
            throw new FileValidationException("File not found in storage: " + storageKey);
        }
        return filePath;
    }

    public InputStream loadFileStream(String storageKey) throws IOException {
        Path filePath = loadFilePath(storageKey);
        return Files.newInputStream(filePath);
    }

    public void deleteFile(String storageKey) {
        try {
            Path filePath = this.rootStoragePath.resolve(storageKey).normalize();
            if (filePath.startsWith(this.rootStoragePath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {}
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new FileValidationException("Corrupted or incomplete file stream");
            }

            if ("pdf".equals(extension)) {
                if (!matchesSignature(header, PDF_MAGIC_BYTES)) {
                    throw new FileValidationException("File signature mismatch: File claims to be PDF but content does not match PDF format");
                }
            } else if ("docx".equals(extension)) {
                if (!matchesSignature(header, ZIP_MAGIC_BYTES)) {
                    throw new FileValidationException("File signature mismatch: File claims to be DOCX but content does not match DOCX/ZIP format");
                }
            }
        } catch (IOException e) {
            throw new FileValidationException("Failed to read file header for verification: " + e.getMessage());
        }
    }

    private boolean matchesSignature(byte[] data, byte[] signature) {
        if (data.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }
}
