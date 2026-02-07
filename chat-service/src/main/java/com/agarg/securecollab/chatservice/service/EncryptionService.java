package com.agarg.securecollab.chatservice.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * End-to-End Encryption (E2E) Service
 * Provides AES-256-GCM encryption for message content
 * Author: Anurag Garg
 */
@Service
public class EncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    
    // In-memory key store (in production, use proper KMS like AWS KMS, HashiCorp Vault, etc.)
    private final Map<String, SecretKey> keyStore = new HashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate a new encryption key for a user or channel
     * @param keyId unique identifier for the key
     * @return the generated SecretKey
     */
    public SecretKey generateKey(String keyId) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE, secureRandom);
            SecretKey key = keyGen.generateKey();
            keyStore.put(keyId, key);
            logger.info("Generated new encryption key: {}", keyId);
            return key;
        } catch (Exception e) {
            logger.error("Error generating encryption key", e);
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Encrypt a message using AES-256-GCM
     * @param plaintext the message to encrypt
     * @param keyId the key identifier
     * @return encrypted payload with IV and ciphertext
     */
    public EncryptedPayload encrypt(String plaintext, String keyId) {
        try {
            SecretKey key = keyStore.computeIfAbsent(keyId, this::generateKey);
            
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            
            // Encode IV and ciphertext to Base64 for transmission
            String encryptedContent = Base64.getEncoder().encodeToString(ciphertext);
            String ivContent = Base64.getEncoder().encodeToString(iv);
            
            logger.debug("Encrypted message with key: {}", keyId);
            
            return new EncryptedPayload(ivContent, encryptedContent, keyId);
        } catch (Exception e) {
            logger.error("Error encrypting message", e);
            throw new RuntimeException("Failed to encrypt message", e);
        }
    }
    
    /**
     * Decrypt a message using AES-256-GCM
     * @param payload the encrypted payload
     * @return decrypted plaintext
     */
    public String decrypt(EncryptedPayload payload) {
        try {
            SecretKey key = keyStore.get(payload.getKeyId());
            if (key == null) {
                throw new IllegalArgumentException("Key not found: " + payload.getKeyId());
            }
            
            byte[] iv = Base64.getDecoder().decode(payload.getIv());
            byte[] ciphertext = Base64.getDecoder().decode(payload.getCiphertext());
            
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            
            logger.debug("Decrypted message with key: {}", payload.getKeyId());
            
            return new String(plaintext);
        } catch (Exception e) {
            logger.error("Error decrypting message", e);
            throw new RuntimeException("Failed to decrypt message", e);
        }
    }
    
    /**
     * Import a key from Base64 encoded format
     * @param keyId the key identifier
     * @param encodedKey Base64 encoded key
     */
    public void importKey(String keyId, String encodedKey) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            keyStore.put(keyId, key);
            logger.info("Imported key: {}", keyId);
        } catch (Exception e) {
            logger.error("Error importing key", e);
            throw new RuntimeException("Failed to import key", e);
        }
    }
    
    /**
     * Export a key in Base64 encoded format
     * @param keyId the key identifier
     * @return Base64 encoded key
     */
    public String exportKey(String keyId) {
        SecretKey key = keyStore.get(keyId);
        if (key == null) {
            throw new IllegalArgumentException("Key not found: " + keyId);
        }
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * Encrypted payload DTO
     */
    public static class EncryptedPayload {
        private String iv;          // Initialization Vector
        private String ciphertext;  // Encrypted content
        private String keyId;       // Key identifier for decryption
        
        public EncryptedPayload(String iv, String ciphertext, String keyId) {
            this.iv = iv;
            this.ciphertext = ciphertext;
            this.keyId = keyId;
        }
        
        public String getIv() { return iv; }
        public void setIv(String iv) { this.iv = iv; }
        
        public String getCiphertext() { return ciphertext; }
        public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }
        
        public String getKeyId() { return keyId; }
        public void setKeyId(String keyId) { this.keyId = keyId; }
    }
}
