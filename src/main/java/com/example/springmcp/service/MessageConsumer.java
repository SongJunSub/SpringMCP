
package com.example.springmcp.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @RabbitListener(queues = "test-queue")
    public void receiveMessage(String message) {
        System.out.println(" [x] Received '" + message + "'");
        // Simulate message processing failure for DLQ testing
        if (message.contains("fail")) {
            throw new RuntimeException("Simulated processing failure for: " + message);
        }
    }
}
