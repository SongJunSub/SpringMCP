
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@Tag(name = "RAG API", description = "Endpoints for Retrieval Augmented Generation")
public class RagController {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private UrlEntryRepository urlEntryRepository;

    private Set<String> expandQuery(String query) {
        Set<String> expandedQueries = new HashSet<>();
        expandedQueries.add(query);

        // Simple example of query expansion
        if (query.toLowerCase().contains("spring ai")) {
            expandedQueries.add("spring artificial intelligence");
            expandedQueries.add("spring machine learning");
        } else if (query.toLowerCase().contains("url")) {
            expandedQueries.add("shorten link");
            expandedQueries.add("web address");
        }
        return expandedQueries;
    }

    @Operation(summary = "Get an answer based on retrieved documents", description = "Retrieves relevant documents and uses them to answer the user's question.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/rag")
    @RateLimiter(name = "default")
    public String rag(@Parameter(description = "The question to answer", example = "What is Spring AI?") @RequestParam(value = "message", defaultValue = "What is Spring AI?") String message) {
        Set<String> expandedQueries = expandQuery(message);
        List<Document> allDocuments = new java.util.ArrayList<>();

        for (String query : expandedQueries) {
            allDocuments.addAll(vectorStore.similaritySearch(query));

            // Simple keyword search on longUrl
            allDocuments.addAll(urlEntryRepository.findAll().stream()
                    .filter(entry -> entry.getLongUrl().contains(query) || entry.getShortUrl().contains(query))
                    .map(entry -> new Document("URL Entry: " + entry.getShortUrl() + " -> " + entry.getLongUrl()))
                    .collect(Collectors.toList()));
        }

        String documents = allDocuments.stream()
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
