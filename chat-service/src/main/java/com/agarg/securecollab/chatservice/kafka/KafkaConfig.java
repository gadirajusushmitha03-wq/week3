package com.agarg.securecollab.chatservice.kafka;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * Producer Factory - exactly-once semantics
     */
    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaProperties props) {
        var factoryProps = props.buildProducerProperties();
        factoryProps.put(ProducerConfig.ACKS_CONFIG, "all");
        factoryProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        factoryProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        factoryProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        factoryProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "chat-transactional");
        return new DefaultKafkaProducerFactory<>(factoryProps, new StringSerializer(), new StringSerializer());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Consumer Factory - exactly-once semantics
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties props) {
        var factoryProps = props.buildConsumerProperties();
        factoryProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        factoryProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        factoryProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        factoryProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(factoryProps, new StringDeserializer(), new StringDeserializer());
    }

    /**
     * Listener Container Factory with error handling and DLT
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);

        // Dead Letter Topic publisher
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        
        // Default error handler with exponential backoff
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000, 3));
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        
        factory.setCommonErrorHandler(errorHandler);

        // Enable batch processing
        factory.setBatchListener(false);

        return factory;
    }
}
