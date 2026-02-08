package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.KeyBundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyBundleRepository extends JpaRepository<KeyBundleEntity, Long> {
    // Added methods for GDPR operations
    void deleteAllByUserId(String userId);
    List<KeyBundleEntity> findByUserId(String userId);
    long countByUserId(String userId);
}