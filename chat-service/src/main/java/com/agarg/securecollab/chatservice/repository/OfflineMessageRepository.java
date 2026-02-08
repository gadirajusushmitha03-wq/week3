package com.agarg.securecollab.chatservice.repository;

import com.agarg.securecollab.chatservice.entity.OfflineMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfflineMessageRepository extends JpaRepository<OfflineMessageEntity, Long> {
    List<OfflineMessageEntity> findByRecipientIdOrderByCreatedAtAsc(String recipientId);
    List<OfflineMessageEntity> findByExpiresAtBefore(LocalDateTime time);
    void deleteByRecipientId(String recipientId);
    void deleteAllByUserId(String userId);
}
