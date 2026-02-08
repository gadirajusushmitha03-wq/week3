package com.agarg.securecollab.chatservice.repository;

import com.agarg.securecollab.chatservice.entity.OAuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthTokenEntity, Long> {
    OAuthTokenEntity findByUserIdAndProvider(String userId, String provider);
    List<OAuthTokenEntity> findByUserId(String userId);
    void deleteAllByUserId(String userId);
}
