package com.agarg.securecollab.chatservice.repository;

import com.agarg.securecollab.chatservice.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    List<MessageEntity> findByChannelIdOrderByCreatedAtAsc(String channelId);
    void deleteAllBySenderId(String senderId);
    List<MessageEntity> findBySenderId(String senderId);
    long countBySenderId(String senderId);
}
