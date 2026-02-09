package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.OfflineMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfflineMessageRepository extends JpaRepository<OfflineMessageEntity, Long> {
}