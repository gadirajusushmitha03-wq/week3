package com.agarg.securecollab.chatservice.service;

import com.agarg.securecollab.chatservice.entity.OfflineMessageEntity;
import com.agarg.securecollab.chatservice.model.Message;
import com.agarg.securecollab.chatservice.repository.OfflineMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Offline Message Queue Service (persistent)
 * Stores encrypted messages in the database when recipients are offline
 * and delivers on reconnect. Uses JPA-backed repository.
 */
@Service
public class OfflineMessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineMessageQueueService.class);

    private final OfflineMessageRepository repository;

    // In-memory presence tracking (hook into Redis in next iteration)
    private final Set<String> offlineUsers = Collections.synchronizedSet(new HashSet<>());

    // TTL days for offline messages (configurable)
    private final long messageTtlDays;

    public OfflineMessageQueueService(OfflineMessageRepository repository,
                                      @Value("${securecollab.offline.message-ttl-days:7}") long ttlDays) {
        this.repository = repository;
        this.messageTtlDays = ttlDays;
    }

    /**
     * Persist a message for offline delivery
     */
    @Transactional
    public void queueMessageForOfflineUser(String userId, Message message) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(messageTtlDays);

        OfflineMessageEntity entity = new OfflineMessageEntity(
            message.getId(), userId, message.getChannelId(), message.getSenderId(),
            message.getEncryptedContent(), now, expiresAt, "PENDING"
        );

        repository.save(entity);
        logger.info("Persisted offline message {} for user {}", message.getId(), userId);
    }

    /**
     * Mark a user offline
     */
    public void markUserOffline(String userId) {
        offlineUsers.add(userId);
        logger.info("User marked offline: {}", userId);
    }

    /**
     * Mark user online and return pending messages; deletes delivered messages
     */
    @Transactional
    public List<Message> markUserOnlineAndRetrieveMessages(String userId) {
        offlineUsers.remove(userId);
        List<OfflineMessageEntity> rows = repository.findByRecipientIdOrderByCreatedAtAsc(userId);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<Message> out = new ArrayList<>();
        for (OfflineMessageEntity r : rows) {
            if (r.getExpiresAt() != null && r.getExpiresAt().isBefore(LocalDateTime.now())) {
                logger.warn("Dropping expired offline message: {}", r.getMessageId());
                repository.delete(r);
                continue;
            }

            Message m = new Message();
            m.setId(r.getMessageId());
            m.setChannelId(r.getChannelId());
            m.setSenderId(r.getSenderId());
            m.setEncryptedContent(r.getEncryptedPayload());
            m.setTimestamp(r.getCreatedAt());
            m.setStatus(Message.MessageStatus.DELIVERED);
            out.add(m);

            repository.delete(r);
        }

        logger.info("Delivered {} pending messages for user {}", out.size(), userId);
        return out;
    }

    public boolean isUserOffline(String userId) {
        return offlineUsers.contains(userId);
    }

    public int getPendingMessageCount(String userId) {
        return repository.findByRecipientIdOrderByCreatedAtAsc(userId).size();
    }

    @Transactional
    public void clearPendingMessages(String userId) {
        repository.deleteByRecipientId(userId);
        logger.info("Cleared pending messages for user: {}", userId);
    }

    public Map<String, Object> getQueueStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("offlineUsersCount", offlineUsers.size());
        stats.put("totalQueuedMessages", repository.count());
        return stats;
    }

    /**
     * Scheduled pruning of expired messages (runs hourly)
     */
    @Scheduled(fixedDelayString = "${securecollab.offline.prune-interval-ms:3600000}")
    @Transactional
    public void pruneExpiredMessages() {
        List<OfflineMessageEntity> expired = repository.findByExpiresAtBefore(LocalDateTime.now());
        int removed = 0;
        for (OfflineMessageEntity e : expired) {
            repository.delete(e);
            removed++;
        }
        logger.info("Pruned {} expired offline messages", removed);
    }
}
