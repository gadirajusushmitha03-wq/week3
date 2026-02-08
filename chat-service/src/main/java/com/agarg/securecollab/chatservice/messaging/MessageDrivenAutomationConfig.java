package com.agarg.securecollab.chatservice.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Message-Driven Automation Configuration using RabbitMQ
 * Processes events and triggers automated workflows
 */
@Configuration
public class MessageDrivenAutomationConfig {
    
    // Exchanges
    public static final String CHAT_EVENTS_EXCHANGE = "chat.events";
    public static final String BOT_COMMANDS_EXCHANGE = "bot.commands";
    public static final String INTEGRATION_EXCHANGE = "integrations";
    
    // Queues
    public static final String MESSAGE_CREATED_QUEUE = "queue.message.created";
    public static final String BOT_TRIGGER_QUEUE = "queue.bot.trigger";
    public static final String TOXICITY_CHECK_QUEUE = "queue.toxicity.check";
    public static final String OFFLINE_DELIVERY_QUEUE = "queue.offline.delivery";
    public static final String WEBHOOK_QUEUE = "queue.webhooks";
    
    // Routing keys
    public static final String ROUTING_MESSAGE_CREATED = "message.created";
    public static final String ROUTING_BOT_TRIGGER = "bot.trigger";
    public static final String ROUTING_TOXICITY = "toxicity.check";
    public static final String ROUTING_OFFLINE = "offline.delivery";
    
    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(CHAT_EVENTS_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange botCommandsExchange() {
        return new TopicExchange(BOT_COMMANDS_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange integrationExchange() {
        return new TopicExchange(INTEGRATION_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue messageCreatedQueue() {
        return new Queue(MESSAGE_CREATED_QUEUE, true);
    }
    
    @Bean
    public Queue botTriggerQueue() {
        return new Queue(BOT_TRIGGER_QUEUE, true);
    }
    
    @Bean
    public Queue toxicityCheckQueue() {
        return new Queue(TOXICITY_CHECK_QUEUE, true);
    }
    
    @Bean
    public Queue offlineDeliveryQueue() {
        return new Queue(OFFLINE_DELIVERY_QUEUE, true);
    }
    
    @Bean
    public Queue webhookQueue() {
        return new Queue(WEBHOOK_QUEUE, true);
    }
    
    @Bean
    public Binding messageCreatedBinding(Queue messageCreatedQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(messageCreatedQueue).to(chatEventsExchange).with(ROUTING_MESSAGE_CREATED);
    }
    
    @Bean
    public Binding botTriggerBinding(Queue botTriggerQueue, TopicExchange botCommandsExchange) {
        return BindingBuilder.bind(botTriggerQueue).to(botCommandsExchange).with(ROUTING_BOT_TRIGGER);
    }
}
