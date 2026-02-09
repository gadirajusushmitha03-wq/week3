
package com.agarg.securecollab.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * chat-service
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.agarg.securecollab.chatservice.repository")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
