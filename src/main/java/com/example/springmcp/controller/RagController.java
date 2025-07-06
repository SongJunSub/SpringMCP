
package com.example.springmcp.controller;

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

@RestController
public class RagController {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private VectorStore vectorStore;

    @GetMapping("/api/rag")
    public String rag(@RequestParam(value = "message", defaultValue = "What is Spring AI?") String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(message);
        String documents = similarDocuments.stream()
                .map(doc -> doc.getFormattedContent())
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
