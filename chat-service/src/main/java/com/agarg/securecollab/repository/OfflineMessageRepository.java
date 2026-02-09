package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.OfflineMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface OfflineMessageRepository extends JpaRepository<OfflineMessageEntity, Long> {
    void deleteByRecipientId(String recipientId);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}