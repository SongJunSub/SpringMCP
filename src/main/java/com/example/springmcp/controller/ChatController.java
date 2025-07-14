
package com.example.springmcp.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "AI Chat API", description = "Endpoints for interacting with the AI chatbot")
public class ChatController {

    private final ChatClient chatClient;

    @Autowired
    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Operation(summary = "Get a response from the AI chatbot", description = "Sends a message to the AI and returns its response.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/chat")
    @CircuitBreaker(name = "externalService", fallbackMethod = "chatFallback")
    public String chat(@Parameter(description = "The message to send to the AI", example = "What is the capital of France?") @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return chatClient.prompt().user(message).call().content();
    }

    private String chatFallback(String message, Throwable t) {
        return "I am sorry, I cannot process your request at the moment. Please try again later. (Fallback: " + t.getMessage() + ")";
    }
}
