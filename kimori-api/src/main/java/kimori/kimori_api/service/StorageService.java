package kimori.kimori_api.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import kimori.kimori_api.exception.ApiException;

/**
 * Validates and stores person pictures/certificates in Firebase Storage (FR-006, FR-024).
 * The Firebase Storage object path is used as the stored reference; callers resolve it to a
 * download URL via the Firebase client SDK, which already has the caller's authenticated context.
 */
@Service
public class StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    public String uploadPicture(String uid, String treeId, String personId, MultipartFile file) {
        validateSize(file);
        validateContentType(file, "image/jpeg", "image/png", "image/webp");
        return upload(uid, treeId, personId, "picture", file);
    }

    public String uploadCertificate(String uid, String treeId, String personId, String kind, MultipartFile file) {
        validateSize(file);
        validateContentType(file, "application/pdf");
        return upload(uid, treeId, personId, kind + "-certificate", file);
    }

    /** Best-effort cleanup; a missing/already-deleted object path is not an error for the caller. */
    public void deleteFile(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            return;
        }
        Blob blob = bucket().get(objectPath);
        if (blob != null) {
            blob.delete();
        }
    }

    private void validateSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.validation("A file is required", "file");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw ApiException.validation("File exceeds the 10MB limit", "file");
        }
    }

    private void validateContentType(MultipartFile file, String... allowed) {
        String contentType = file.getContentType();
        for (String type : allowed) {
            if (type.equalsIgnoreCase(contentType)) {
                return;
            }
        }
        throw ApiException.validation(
                "Unsupported file type: " + contentType + " (expected one of " + String.join(", ", allowed) + ")",
                "file");
    }

    private String upload(String uid, String treeId, String personId, String label, MultipartFile file) {
        String objectPath = "users/%s/trees/%s/people/%s/%s-%s".formatted(
                uid, treeId, personId, label, UUID.randomUUID());
        try {
            bucket().create(objectPath, file.getBytes(), file.getContentType());
            return objectPath;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", "Failed to store file");
        }
    }

    private Bucket bucket() {
        return StorageClient.getInstance().bucket();
    }
}
