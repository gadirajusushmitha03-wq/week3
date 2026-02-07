package com.agarg.securecollab.chatservice.service;

import com.agarg.securecollab.chatservice.entity.OAuthTokenEntity;
import com.agarg.securecollab.chatservice.repository.OAuthTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth Token Management Service
 * Securely stores and manages OAuth2 tokens for external integrations
 */
@Service
public class OAuthTokenService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthTokenService.class);

    private final OAuthTokenRepository repository;
    private final EncryptionService encryptionService;

    public OAuthTokenService(OAuthTokenRepository repository, EncryptionService encryptionService) {
        this.repository = repository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public OAuthTokenEntity storeToken(String userId, String provider, String accessToken, String refreshToken, int expiresInSeconds) {
        try {
            // Encrypt tokens before storage
            String keyId = "oauth_" + provider;
            String encryptedAccess = encryptionService.encrypt(accessToken, keyId).getCiphertext();
            String encryptedRefresh = refreshToken != null ? encryptionService.encrypt(refreshToken, keyId).getCiphertext() : null;

            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);

            OAuthTokenEntity existing = repository.findByUserIdAndProvider(userId, provider);
            if (existing != null) {
                existing.setAccessToken(encryptedAccess);
                existing.setRefreshToken(encryptedRefresh);
                existing.setExpiresAt(expiresAt);
                existing.setUpdatedAt(LocalDateTime.now());
                repository.save(existing);
                logger.info("Updated OAuth token for user={} provider={}", userId, provider);
                return existing;
            }

            OAuthTokenEntity entity = new OAuthTokenEntity(userId, provider, encryptedAccess, encryptedRefresh, expiresAt);
            repository.save(entity);
            logger.info("Stored OAuth token for user={} provider={}", userId, provider);
            return entity;
        } catch (Exception e) {
            logger.error("Error storing OAuth token", e);
            throw new RuntimeException("Failed to store OAuth token", e);
        }
    }

    @Transactional(readOnly = true)
    public String getAccessToken(String userId, String provider) {
        try {
            OAuthTokenEntity entity = repository.findByUserIdAndProvider(userId, provider);
            if (entity == null) {
                return null;
            }

            // Check if token expired
            if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now())) {
                logger.warn("OAuth token expired for user={} provider={}", userId, provider);
                return null;
            }

            // Decrypt and return
            String keyId = "oauth_" + provider;
            EncryptionService.EncryptedPayload payload = new EncryptionService.EncryptedPayload(
                "", entity.getAccessToken(), keyId
            );
            return encryptionService.decrypt(payload);
        } catch (Exception e) {
            logger.error("Error retrieving OAuth token", e);
            return null;
        }
    }

    @Transactional
    public void revokeToken(String userId, String provider) {
        OAuthTokenEntity entity = repository.findByUserIdAndProvider(userId, provider);
        if (entity != null) {
            repository.delete(entity);
            logger.info("Revoked OAuth token for user={} provider={}", userId, provider);
        }
    }

    public List<OAuthTokenEntity> getUserTokens(String userId) {
        return repository.findByUserId(userId);
    }
}
