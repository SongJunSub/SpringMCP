package com.example.springmcp.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "AI Chat API", description = "Endpoints for interacting with the AI chatbot")
public class ChatController {

    private final ChatClient chatClient;

    @Autowired
    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                        .withModel("gpt-4")
                        .withTemperature(0.7)
                        .withMaxTokens(1000)
                        .build())
                .build();
    }

    @Operation(summary = "Get a response from the AI chatbot", 
               description = "Sends a message to the AI and returns its response.", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/chat")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> chat(
            @Parameter(description = "The message to send to the AI", example = "What is the capital of France?") 
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        
        try {
            ChatResponse response = chatClient.prompt()
                    .user(message)
                    .call()
                    .chatResponse();
            
            return ResponseEntity.ok(Map.of(
                "response", response.getResult().getOutput().getContent(),
                "model", response.getMetadata().getModel(),
                "usage", response.getMetadata().getUsage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "AI 서비스 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get AI response with custom template",
               description = "Uses a custom prompt template for AI interaction",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/chat/template")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> chatWithTemplate(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String template = (String) requestBody.get("template");
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) requestBody.get("variables");
            
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Prompt prompt = promptTemplate.create(variables);
            
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            return ResponseEntity.ok(Map.of(
                "response", response.getResult().getOutput().getContent(),
                "template", template,
                "variables", variables,
                "usage", response.getMetadata().getUsage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "템플릿 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Stream AI chat response",
               description = "Returns streaming response from AI for real-time interaction",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/api/chat/stream", produces = "text/plain")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<String> streamChat(
            @Parameter(description = "The message to send to the AI") 
            @RequestParam String message) {
        
        try {
            String response = chatClient.prompt()
                    .user(message)
                    .stream()
                    .content()
                    .collectList()
                    .block()
                    .toString();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("스트림 처리 중 오류: " + e.getMessage());
        }
    }
}