package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    // Added methods for GDPR operations
    List<MessageEntity> findBySenderId(String senderId);
    void deleteAllBySenderId(String senderId);
    long countBySenderId(String senderId);
}