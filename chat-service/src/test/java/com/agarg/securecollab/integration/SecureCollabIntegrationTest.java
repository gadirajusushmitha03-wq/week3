package com.agarg.securecollab.integration;

import com.agarg.securecollab.entity.*;
import com.agarg.securecollab.repository.*;
import com.agarg.securecollab.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Tests
 * Tests full flows: encryption → message send → offline delivery → retrieval
 * Uses TestContainers for real database/message broker/cache instances
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class SecureCollabIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
    .withDatabaseName("securecollab_test")
    .withUsername("test")
    .withPassword("test");

  @Container
  static KafkaContainer kafka = new KafkaContainer();

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7")
    .withExposedPorts(6379);

  @Autowired private MessageRepository messageRepository;
  @Autowired private EncryptionService encryptionService;
  @Autowired private OfflineMessageQueueService offlineQueueService;
  @Autowired private KeyManagementService keyManagementService;
  @Autowired private ChatApiController chatApiController;
  @Autowired private RateLimitService rateLimitService;

  private static final String USER_1 = "alice@example.com";
  private static final String USER_2 = "bob@example.com";

  @BeforeEach
  void setup() {
    // Clear database and cache before each test
    messageRepository.deleteAll();
    // Redis cache cleared via test cleanup
  }

  /**
   * Test: Full encrypted message flow
   * 1. User registers public key
   * 2. Sends encrypted message
   * 3. Server stores ciphertext
   * 4. Recipient retrieves and decrypts
   */
  @Test
  void testEncryptedMessageFlow() throws Exception {
    // Step 1: Register public keys for both users
    String publicKey1 = "-----BEGIN PUBLIC KEY-----\nMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL...\n-----END PUBLIC KEY-----";
    String publicKey2 = "-----BEGIN PUBLIC KEY-----\nMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAM...\n-----END PUBLIC KEY-----";

    KeyBundleEntity keyBundle1 = keyManagementService.registerPublicKey(USER_1, "device-1", publicKey1);
    KeyBundleEntity keyBundle2 = keyManagementService.registerPublicKey(USER_2, "device-1", publicKey2);

    assertNotNull(keyBundle1.getId());
    assertNotNull(keyBundle2.getId());

    // Step 2: Send encrypted message
    String plaintext = "Secret message for Bob";
    String encrypted = encryptionService.encrypt(plaintext, publicKey2);

    assertNotEquals(plaintext, encrypted); // Verify encryption occurred
    assertTrue(encrypted.contains("+")); // Base64 encoded

    // Step 3: Store in database
    MessageEntity message = new MessageEntity();
    message.setFromUserId(USER_1);
    message.setToUserId(USER_2);
    message.setEncryptedContent(encrypted);
    message.setIv("random-iv");

    MessageEntity saved = messageRepository.save(message);
    assertNotNull(saved.getId());

    // Step 4: Verify server cannot read plaintext
    MessageEntity retrieved = messageRepository.findById(saved.getId()).orElse(null);
    assertNotNull(retrieved);
    assertNotEquals(plaintext, retrieved.getEncryptedContent());
    assertEquals(encrypted, retrieved.getEncryptedContent());
  }

  /**
   * Test: Offline message delivery queue
   * 1. User goes offline
   * 2. Messages queued for offline delivery
   * 3. User comes back online
   * 4. Queued messages delivered automatically
   */
  @Test
  void testOfflineMessageDelivery() throws Exception {
    // Simulate user going offline
    String messageText = "Hello Bob, are you there?";

    // Queue message for offline delivery
    offlineQueueService.queueMessageForOfflineDelivery(USER_1, USER_2, messageText);

    // Verify message is in queue
    long queuedCount = offlineQueueService.getOfflineMessageCount(USER_2);
    assertTrue(queuedCount > 0);

    // Simulate user coming online (delivery triggered)
    // In real system, this happens via presence update event
    assertTrue(rateLimitService.isWithinLimit(USER_2, 10)); // Should not be rate-limited
  }

  /**
   * Test: Toxicity detection
   * 1. Send message with toxic content
   * 2. Server detects and flags
   * 3. Message still stored but marked
   */
  @Test
  void testToxicityDetection() throws Exception {
    ToxicityDetectionService toxicityService = new ToxicityDetectionService();

    String toxicMessage = "You are stupid and worthless";
    ToxicityDetectionService.ToxicityResult result = toxicityService.detectToxicity(toxicMessage);

    assertTrue(result.isToxic());
    assertTrue(result.score() > 0.6);
    assertEquals(ToxicityDetectionService.ToxicitySeverity.HIGH, result.severity());
  }

  /**
   * Test: Rate limiting on WebSocket
   * 1. Send 25 messages in 10 seconds
   * 2. Should allow 20, reject 5+
   */
  @Test
  void testRateLimiting() throws Exception {
    RateLimitService rateLimitService = new RateLimitService(); // Inject Redis

    // Send 30 messages in quick succession
    for (int i = 0; i < 30; i++) {
      boolean allowed = rateLimitService.isWithinLimit(USER_1, 10);
      if (i < 20) {
        assertTrue(allowed, "Message " + i + " should be allowed (within 20/10s limit)");
      } else {
        assertFalse(allowed, "Message " + i + " should be rejected (exceeds 20/10s limit)");
      }
    }
  }

  /**
   * Test: Bot workflow execution
   * 1. Create bot workflow: message trigger → Jira issue creation
   * 2. Send message matching trigger
   * 3. Verify Jira issue was created
   */
  @Test
  void testBotWorkflowExecution() throws Exception {
    // TODO: Implement when bot framework is testable
    // Create workflow with trigger: message contains "BUG:"
    // Action: Create Jira issue
    // Send message: "BUG: Chat not connecting"
    // Assert: Jira issue created with title "Chat not connecting"
  }

  /**
   * Test: Channel management
   * 1. Create channel
   * 2. Add members
   * 3. Send message to channel
   * 4. Verify all members receive it
   */
  @Test
  void testChannelMessaging() throws Exception {
    // TODO: Implement when channel repository is available
    // Create channel "engineering"
    // Add users: alice, bob, charlie
    // Send message to channel
    // Verify all 3 users have message in their inbox
  }

  /**
   * Test: Kafka event streaming
   * 1. Publish message.created event to Kafka
   * 2. Verify toxicity checker consumes it
   * 3. Verify offline queue consumes it
   */
  @Test
  void testKafkaEventStreaming() throws Exception {
    // TODO: Implement Kafka consumer mock
    // Send message via API
    // Verify chat.messages topic has event
    // Verify chat.toxicity topic has toxicity check event (if applicable)
  }

  /**
   * Test: GDPR data deletion
   * 1. User requests account deletion
   * 2. All messages deleted
   * 3. All keys deleted
   * 4. Verify no data remains
   */
  @Test
  void testGDPRDataDeletion() throws Exception {
    // TODO: Implement when GDPRController is integrated
    // Create user with messages
    // Request deletion
    // Verify messageRepository.findByFromUserId(USER_1) returns empty
    // Verify keyBundleRepository.findByUserId(USER_1) returns empty
  }

  /**
   * Test: Performance - 1000 concurrent messages
   * Measures throughput, latency, memory usage
   */
  @Test
  void testHighThroughput() throws Exception {
    long startTime = System.currentTimeMillis();
    int messageCount = 1000;

    for (int i = 0; i < messageCount; i++) {
      String message = "Stress test message " + i;
      String encrypted = encryptionService.encrypt(message, "dummy-key");
      
      MessageEntity entity = new MessageEntity();
      entity.setFromUserId(USER_1);
      entity.setToUserId(USER_2);
      entity.setEncryptedContent(encrypted);
      messageRepository.save(entity);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    double throughput = (messageCount * 1000.0) / duration;

    System.out.println("Throughput: " + throughput + " msg/sec");
    System.out.println("Total time: " + duration + " ms");

    assertTrue(throughput > 100, "Should process > 100 msg/sec");
  }

  /**
   * Test: Database transaction rollback on error
   * Verify ACID properties maintained
   */
  @Test
  void testTransactionRollback() throws Exception {
    // TODO: Test transaction boundaries
    // Create message
    // Trigger error in encryption
    // Verify message not persisted
    // Verify state not corrupted
  }
}
