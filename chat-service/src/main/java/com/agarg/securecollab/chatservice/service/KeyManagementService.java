package com.agarg.securecollab.chatservice.service;

import com.agarg.securecollab.chatservice.entity.KeyBundleEntity;
import com.agarg.securecollab.chatservice.repository.KeyBundleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KeyManagementService {

    private static final Logger logger = LoggerFactory.getLogger(KeyManagementService.class);
    private final KeyBundleRepository repository;

    public KeyManagementService(KeyBundleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public KeyBundleEntity registerKeyBundle(String userId, String deviceId, String identityKey, String preKey, String signedPreKey) {
        KeyBundleEntity existing = repository.findByUserIdAndDeviceId(userId, deviceId);
        if (existing != null) {
            existing.setIdentityKey(identityKey);
            existing.setPreKey(preKey);
            existing.setSignedPreKey(signedPreKey);
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            repository.save(existing);
            logger.info("Updated key bundle for user={} device={}", userId, deviceId);
            return existing;
        }

        KeyBundleEntity entity = new KeyBundleEntity(userId, deviceId, identityKey, preKey, signedPreKey);
        repository.save(entity);
        logger.info("Registered new key bundle for user={} device={}", userId, deviceId);
        return entity;
    }

    public List<KeyBundleEntity> getKeyBundlesForUser(String userId) {
        return repository.findByUserId(userId);
    }

    public KeyBundleEntity getKeyBundle(String userId, String deviceId) {
        return repository.findByUserIdAndDeviceId(userId, deviceId);
    }

    @Transactional
    public void removeKeyBundle(String userId, String deviceId) {
        KeyBundleEntity e = repository.findByUserIdAndDeviceId(userId, deviceId);
        if (e != null) {
            repository.delete(e);
            logger.info("Removed key bundle for user={} device={}", userId, deviceId);
        }
    }
}
