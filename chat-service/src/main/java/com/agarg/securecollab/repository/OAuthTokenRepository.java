package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.OAuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenRepository extends JpaRepository<OAuthTokenEntity, Long> {
    // Added methods for GDPR operations
    void deleteAllByUserId(String userId);
    long countByUserId(String userId);
}