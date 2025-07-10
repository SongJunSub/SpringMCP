
package com.example.springmcp.controller;

import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Tag(name = "RAG API", description = "Endpoints for Retrieval Augmented Generation")
public class RagController {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private UrlEntryRepository urlEntryRepository;

    @Operation(summary = "Get an answer based on retrieved documents", description = "Retrieves relevant documents and uses them to answer the user's question.", security = @SecurityRequirement(name = "basicAuth"))
    @GetMapping("/api/rag")
    public String rag(@Parameter(description = "The question to answer", example = "What is Spring AI?") @RequestParam(value = "message", defaultValue = "What is Spring AI?") String message) {
        List<Document> vectorDocuments = vectorStore.similaritySearch(message);

        // Simple keyword search on longUrl
        List<Document> keywordDocuments = urlEntryRepository.findAll().stream()
                .filter(entry -> entry.getLongUrl().contains(message) || entry.getShortUrl().contains(message))
                .map(entry -> new Document("URL Entry: " + entry.getShortUrl() + " -> " + entry.getLongUrl()))
                .collect(Collectors.toList());

        String documents = Stream.concat(vectorDocuments.stream(), keywordDocuments.stream())
                .map(doc -> doc.getFormattedContent())
                .distinct() // Remove duplicates based on formatted content
                .collect(Collectors.joining(System.lineSeparator()));

        PromptTemplate promptTemplate = new PromptTemplate("""
                Based on the following documents, please answer the user's question.
                Documents:
                {documents}

                Question:
                {query}
                """);
        promptTemplate.add("documents", documents);
        promptTemplate.add("query", message);

        return chatClientBuilder.build().prompt(promptTemplate.create()).call().content();
    }
}
