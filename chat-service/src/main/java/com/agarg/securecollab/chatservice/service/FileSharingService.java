package com.agarg.securecollab.chatservice.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Sharing Service
 * Handles encrypted file uploads, downloads, and sharing
 * Author: Anurag Garg
 */
@Service
public class FileSharingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSharingService.class);
    
    private final Map<String, SharedFile> files = new HashMap<>();
    private final EncryptionService encryptionService;
    
    // Configuration
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "pdf", "doc", "docx", "xls", "xlsx", "txt", 
        "jpg", "jpeg", "png", "gif", "zip", "rar"
    );
    
    public FileSharingService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
    
    /**
     * Upload and share a file
     */
    public SharedFile uploadFile(String uploaderId, String channelId, String filename,
                                 byte[] fileContent, String mimeType) {
        
        // Validate file
        validateFile(filename, fileContent);
        
        // Encrypt file content
        String fileKeyId = "file_" + UUID.randomUUID().toString();
        String plaintext = Base64.getEncoder().encodeToString(fileContent);
        EncryptionService.EncryptedPayload encrypted = encryptionService.encrypt(plaintext, fileKeyId);
        
        // Create file record
        SharedFile file = new SharedFile(uploaderId, channelId, filename, 
                                        encrypted.getCiphertext(), 
                                        encrypted.getIv(), fileKeyId,
                                        mimeType, fileContent.length);
        
        files.put(file.getFileId(), file);
        logger.info("File uploaded: {} by user: {}", filename, uploaderId);
        
        return file;
    }
    
    /**
     * Download a file
     */
    public byte[] downloadFile(String fileId, String userId) {
        SharedFile file = files.get(fileId);
        
        if (file == null) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }
        
        // Check access
        if (!file.canAccess(userId)) {
            throw new SecurityException("User not authorized to access file: " + fileId);
        }
        
        try {
            // Decrypt file content
            EncryptionService.EncryptedPayload payload = new EncryptionService.EncryptedPayload(
                file.getIv(), file.getEncryptedContent(), file.getKeyId()
            );
            String decrypted = encryptionService.decrypt(payload);
            
            file.addAccessLog(userId);
            logger.info("File downloaded: {} by user: {}", file.getFilename(), userId);
            
            return Base64.getDecoder().decode(decrypted);
        } catch (Exception e) {
            logger.error("Error downloading file: {}", fileId, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }
    
    /**
     * Share a file with additional users
     */
    public void shareFile(String fileId, List<String> userIds, String shareBy) {
        SharedFile file = files.get(fileId);
        
        if (file == null) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }
        
        if (!file.getUploaderId().equals(shareBy)) {
            throw new SecurityException("Only file owner can share the file");
        }
        
        for (String userId : userIds) {
            file.addSharedWith(userId);
        }
        
        logger.info("File shared: {} with {} users", fileId, userIds.size());
    }
    
    /**
     * Get file info
     */
    public SharedFile getFileInfo(String fileId, String userId) {
        SharedFile file = files.get(fileId);
        
        if (file == null || !file.canAccess(userId)) {
            return null;
        }
        
        return file;
    }
    
    /**
     * Delete a file
     */
    public void deleteFile(String fileId, String userId) {
        SharedFile file = files.get(fileId);
        
        if (file == null) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }
        
        if (!file.getUploaderId().equals(userId)) {
            throw new SecurityException("Only file owner can delete the file");
        }
        
        files.remove(fileId);
        logger.info("File deleted: {} by user: {}", file.getFilename(), userId);
    }
    
    /**
     * Get files in a channel
     */
    public List<SharedFile> getChannelFiles(String channelId, String userId) {
        return files.values().stream()
            .filter(f -> f.getChannelId().equals(channelId))
            .filter(f -> f.canAccess(userId))
            .toList();
    }
    
    private void validateFile(String filename, byte[] fileContent) {
        // Check file size
        if (fileContent.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + MAX_FILE_SIZE);
        }
        
        // Check file type
        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_TYPES.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
    
    public static class SharedFile {
        private String fileId;
        private String uploaderId;
        private String channelId;
        private String filename;
        private String encryptedContent;
        private String iv;
        private String keyId;
        private String mimeType;
        private long fileSize;
        private Set<String> sharedWith;
        private List<AccessLog> accessLogs;
        private LocalDateTime uploadedAt;
        private boolean deleted;
        
        public static class AccessLog {
            public String userId;
            public LocalDateTime accessTime;
            
            public AccessLog(String userId) {
                this.userId = userId;
                this.accessTime = LocalDateTime.now();
            }
        }
        
        public SharedFile(String uploaderId, String channelId, String filename,
                         String encryptedContent, String iv, String keyId,
                         String mimeType, long fileSize) {
            this.fileId = UUID.randomUUID().toString();
            this.uploaderId = uploaderId;
            this.channelId = channelId;
            this.filename = filename;
            this.encryptedContent = encryptedContent;
            this.iv = iv;
            this.keyId = keyId;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
            this.sharedWith = new HashSet<>();
            this.sharedWith.add(uploaderId); // Owner can always access
            this.accessLogs = new ArrayList<>();
            this.uploadedAt = LocalDateTime.now();
            this.deleted = false;
        }
        
        public boolean canAccess(String userId) {
            return sharedWith.contains(userId) && !deleted;
        }
        
        public void addSharedWith(String userId) {
            sharedWith.add(userId);
        }
        
        public void addAccessLog(String userId) {
            accessLogs.add(new AccessLog(userId));
        }
        
        // Getters
        public String getFileId() { return fileId; }
        public String getUploaderId() { return uploaderId; }
        public String getChannelId() { return channelId; }
        public String getFilename() { return filename; }
        public String getEncryptedContent() { return encryptedContent; }
        public String getIv() { return iv; }
        public String getKeyId() { return keyId; }
        public String getMimeType() { return mimeType; }
        public long getFileSize() { return fileSize; }
        public Set<String> getSharedWith() { return new HashSet<>(sharedWith); }
        public List<AccessLog> getAccessLogs() { return new ArrayList<>(accessLogs); }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }
}
