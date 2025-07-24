package com.example.springmcp.controller;

import java.util.*;
import java.util.stream.Collectors;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.springmcp.repository.UrlEntryRepository;

@RestController
@Tag(name = "RAG API", description = "Endpoints for Retrieval Augmented Generation")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final UrlEntryRepository urlEntryRepository;

    @Autowired
    public RagController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, 
                        UrlEntryRepository urlEntryRepository) {
        this.chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                        .withModel("gpt-4")
                        .withTemperature(0.3)
                        .withMaxTokens(1500)
                        .build())
                .build();
        this.vectorStore = vectorStore;
        this.urlEntryRepository = urlEntryRepository;
    }

    @Value("classpath:/prompts/rag-prompt.st")
    private Resource ragPromptResource;

    private Set<String> expandQuery(String query) {
        Set<String> expandedQueries = new HashSet<>();
        expandedQueries.add(query);

        // Enhanced query expansion with more comprehensive mappings
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("spring ai") || lowerQuery.contains("springai")) {
            expandedQueries.addAll(Arrays.asList(
                "spring artificial intelligence",
                "spring machine learning",
                "AI framework",
                "chatbot development"
            ));
        }
        
        if (lowerQuery.contains("url") || lowerQuery.contains("link")) {
            expandedQueries.addAll(Arrays.asList(
                "shorten link", "web address", "hyperlink", 
                "redirect", "short URL", "link shortening"
            ));
        }
        
        if (lowerQuery.contains("database") || lowerQuery.contains("db")) {
            expandedQueries.addAll(Arrays.asList(
                "data storage", "MySQL", "JPA", "repository", "entity"
            ));
        }
        
        if (lowerQuery.contains("api") || lowerQuery.contains("rest")) {
            expandedQueries.addAll(Arrays.asList(
                "REST API", "endpoint", "controller", "HTTP", "web service"
            ));
        }
        
        return expandedQueries;
    }

    @Operation(summary = "Get an answer based on retrieved documents", 
               description = "Retrieves relevant documents using hybrid search and uses them to answer the user's question.", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/rag")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> rag(
            @Parameter(description = "The question to answer", example = "What is Spring AI?") 
            @RequestParam(value = "message", defaultValue = "What is Spring AI?") String message,
            @Parameter(description = "Maximum number of documents to retrieve", example = "5")
            @RequestParam(value = "maxDocs", defaultValue = "5") int maxDocs) {
        
        try {
            Set<String> expandedQueries = expandQuery(message);
            List<Document> allDocuments = new ArrayList<>();

            // Vector similarity search with enhanced parameters
            for (String query : expandedQueries) {
                SearchRequest searchRequest = SearchRequest.query(query)
                        .withTopK(Math.min(maxDocs, 10))
                        .withSimilarityThreshold(0.7);
                        
                allDocuments.addAll(vectorStore.similaritySearch(searchRequest));

                // Enhanced keyword search on URL entries
                allDocuments.addAll(urlEntryRepository.findAll().stream()
                        .filter(entry -> 
                            entry.getLongUrl().toLowerCase().contains(query.toLowerCase()) || 
                            entry.getShortUrl().toLowerCase().contains(query.toLowerCase()))
                        .map(entry -> new Document(
                            String.format("URL Mapping: %s -> %s (Created: %s)", 
                                entry.getShortUrl(), entry.getLongUrl(), entry.getCreatedAt()),
                            Map.of("type", "url_mapping", "shortUrl", entry.getShortUrl(), 
                                   "longUrl", entry.getLongUrl())))
                        .collect(Collectors.toList()));
            }

            // Remove duplicates and rank documents
            List<Document> uniqueDocuments = allDocuments.stream()
                    .collect(Collectors.toMap(
                        doc -> doc.getContent(),
                        doc -> doc,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new))
                    .values()
                    .stream()
                    .limit(maxDocs)
                    .collect(Collectors.toList());

            String documents = uniqueDocuments.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n"));

            if (documents.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "response", "관련 문서를 찾을 수 없습니다. 다른 질문을 시도해보세요.",
                    "documentsFound", 0,
                    "expandedQueries", expandedQueries
                ));
            }

            PromptTemplate promptTemplate = new PromptTemplate("""
                당신은 전문적인 AI 어시스턴트입니다. 다음 문서들을 바탕으로 사용자의 질문에 정확하고 유용한 답변을 제공하세요.
                
                검색된 문서들:
                {documents}

                사용자 질문: {query}
                
                답변 지침:
                1. 제공된 문서 내용을 기반으로 답변하세요
                2. 문서에 없는 내용은 추측하지 마세요
                3. 답변은 명확하고 구체적으로 작성하세요
                4. 필요시 예시나 코드를 포함하세요
                5. 한국어로 답변하세요
                """);

            String response = chatClient.prompt()
                    .user(promptTemplate.createMessage(Map.of(
                        "documents", documents,
                        "query", message
                    )))
                    .call()
                    .content();

            return ResponseEntity.ok(Map.of(
                "response", response,
                "documentsFound", uniqueDocuments.size(),
                "expandedQueries", expandedQueries,
                "relevantDocuments", uniqueDocuments.stream()
                    .map(doc -> Map.of(
                        "content", doc.getContent().substring(0, Math.min(200, doc.getContent().length())) + "...",
                        "metadata", doc.getMetadata()
                    ))
                    .collect(Collectors.toList())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "RAG 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Add documents to vector store",
               description = "Adds custom documents to the vector database for future RAG queries",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/rag/documents")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> addDocuments(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documentsData = (List<Map<String, Object>>) requestBody.get("documents");
            
            List<Document> documents = documentsData.stream()
                    .map(docData -> {
                        String content = (String) docData.get("content");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> metadata = (Map<String, Object>) docData.getOrDefault("metadata", new HashMap<>());
                        metadata.put("timestamp", System.currentTimeMillis());
                        return new Document(content, metadata);
                    })
                    .collect(Collectors.toList());

            vectorStore.add(documents);
            
            return ResponseEntity.ok(Map.of(
                "message", "문서가 성공적으로 추가되었습니다.",
                "documentsAdded", documents.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "문서 추가 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Search documents",
               description = "Search for similar documents in the vector store",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/rag/search")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "10") int maxResults,
            @Parameter(description = "Similarity threshold") @RequestParam(defaultValue = "0.7") double threshold) {
        
        try {
            SearchRequest searchRequest = SearchRequest.query(query)
                    .withTopK(maxResults)
                    .withSimilarityThreshold(threshold);
                    
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            
            return ResponseEntity.ok(Map.of(
                "query", query,
                "documentsFound", documents.size(),
                "documents", documents.stream()
                    .map(doc -> Map.of(
                        "content", doc.getContent(),
                        "metadata", doc.getMetadata()
                    ))
                    .collect(Collectors.toList())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "문서 검색 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }
}