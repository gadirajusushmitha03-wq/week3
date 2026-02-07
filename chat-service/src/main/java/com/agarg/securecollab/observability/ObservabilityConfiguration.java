package com.agarg.securecollab.observability;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import io.micrometer.core.instrument.*;

/**
 * Observability Configuration - Prometheus metrics, distributed tracing
 */
@Configuration
@EnableAspectJAutoProxy
public class ObservabilityConfiguration {

  /**
   * Register custom Prometheus metrics
   */
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config().commonTags(
      "application", "secure-collab",
      "environment", System.getenv().getOrDefault("ENV", "development")
    );
  }

  /**
   * Enable method-level timing via @Timed annotation
   */
  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  /**
   * Custom metrics bean for chat service KPIs
   */
  @Bean
  public ChatMetricsCollector chatMetricsCollector(MeterRegistry meterRegistry) {
    return new ChatMetricsCollector(meterRegistry);
  }

  /**
   * Custom metrics for WebRTC/voice calls
   */
  @Bean
  public VoiceMetricsCollector voiceMetricsCollector(MeterRegistry meterRegistry) {
    return new VoiceMetricsCollector(meterRegistry);
  }
}

/**
 * Chat service KPI collectors
 */
class ChatMetricsCollector {
  private final MeterRegistry meterRegistry;
  private final Counter messagesSent;
  private final Counter messagesReceived;
  private final Counter toxicMessagesDetected;
  private final Counter offlineMessagesQueued;
  private final Timer messageLatency;
  private final AtomicInteger activeChannels;

  public ChatMetricsCollector(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    this.messagesSent = Counter.builder("chat.messages.sent")
      .description("Total messages sent")
      .register(meterRegistry);

    this.messagesReceived = Counter.builder("chat.messages.received")
      .description("Total messages received")
      .register(meterRegistry);

    this.toxicMessagesDetected = Counter.builder("chat.toxicity.detected")
      .description("Total toxic messages detected")
      .register(meterRegistry);

    this.offlineMessagesQueued = Counter.builder("chat.offline.queued")
      .description("Total messages queued for offline delivery")
      .register(meterRegistry);

    this.messageLatency = Timer.builder("chat.message.latency")
      .description("Message send-to-delivery latency (ms)")
      .publishPercentiles(0.5, 0.95, 0.99)
      .register(meterRegistry);

    this.activeChannels = new AtomicInteger(0);
    Gauge.builder("chat.channels.active", activeChannels::get)
      .description("Currently active chat channels")
      .register(meterRegistry);
  }

  public void recordMessageSent() { messagesSent.increment(); }
  public void recordMessageReceived() { messagesReceived.increment(); }
  public void recordToxicMessageDetected() { toxicMessagesDetected.increment(); }
  public void recordOfflineMessageQueued() { offlineMessagesQueued.increment(); }
  public void recordMessageLatency(long latencyMs) { messageLatency.record(latencyMs, java.util.concurrent.TimeUnit.MILLISECONDS); }
  public void setActiveChannels(int count) { activeChannels.set(count); }
}

/**
 * Voice/WebRTC call metrics
 */
class VoiceMetricsCollector {
  private final MeterRegistry meterRegistry;
  private final Counter callsInitiated;
  private final Counter callsConnected;
  private final Counter callsFailed;
  private final Timer callDuration;
  private final AtomicInteger activeCalls;
  private final Gauge jitterBuffer;

  public VoiceMetricsCollector(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    this.callsInitiated = Counter.builder("voice.calls.initiated")
      .description("Total voice calls initiated")
      .register(meterRegistry);

    this.callsConnected = Counter.builder("voice.calls.connected")
      .description("Total voice calls successfully connected")
      .register(meterRegistry);

    this.callsFailed = Counter.builder("voice.calls.failed")
      .description("Total voice calls failed")
      .register(meterRegistry);

    this.callDuration = Timer.builder("voice.call.duration")
      .description("Voice call duration (seconds)")
      .publishPercentiles(0.5, 0.95, 0.99)
      .register(meterRegistry);

    this.activeCalls = new AtomicInteger(0);
    Gauge.builder("voice.calls.active", activeCalls::get)
      .description("Currently active voice calls")
      .register(meterRegistry);

    this.jitterBuffer = Gauge.builder("voice.rtp.jitter", () -> 0.0)
      .description("RTP jitter buffer (ms)")
      .register(meterRegistry);
  }

  public void recordCallInitiated() { callsInitiated.increment(); }
  public void recordCallConnected() { callsConnected.increment(); }
  public void recordCallFailed() { callsFailed.increment(); }
  public void recordCallDuration(long durationSeconds) { callDuration.record(durationSeconds, java.util.concurrent.TimeUnit.SECONDS); }
  public void setActiveCalls(int count) { activeCalls.set(count); }
}
