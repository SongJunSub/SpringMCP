package com.example.springmcp.controller;

import com.example.springmcp.service.SemanticSearchService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/semantic")
@Tag(name = "Semantic Search API", description = "Advanced semantic search capabilities with AI-powered query expansion and document ranking")
public class SemanticSearchController {

    private final SemanticSearchService semanticSearchService;

    @Autowired
    public SemanticSearchController(SemanticSearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    @Operation(summary = "Perform advanced semantic search",
               description = "Executes semantic search with AI-powered query expansion, document re-ranking, and relevance scoring",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> semanticSearch(
            @Parameter(description = "Search query", example = "Spring AI RAG implementation") 
            @RequestParam String query,
            @Parameter(description = "Maximum number of documents to return", example = "10")
            @RequestParam(defaultValue = "10") int maxDocs,
            @Parameter(description = "Similarity threshold (0.0-1.0)", example = "0.7")
            @RequestParam(defaultValue = "0.7") double threshold,
            @Parameter(description = "Enable AI-powered query expansion", example = "true")
            @RequestParam(defaultValue = "true") boolean useQueryExpansion,
            @Parameter(description = "Filter expression for metadata", example = "category == 'documentation'")
            @RequestParam(required = false) String filter) {
        
        try {
            // 검색 설정 구성
            SemanticSearchService.SearchConfiguration config = new SemanticSearchService.SearchConfiguration(
                maxDocs, threshold, useQueryExpansion);
            config.setFilterExpression(filter);

            // 의미론적 검색 수행
            SemanticSearchService.SemanticSearchResult result = 
                semanticSearchService.performSemanticSearch(query, config);

            // 응답 데이터 구성
            return ResponseEntity.ok(Map.of(
                "originalQuery", result.getOriginalQuery(),
                "expandedQueries", result.getExpandedQueries(),
                "documentsFound", result.getDocuments().size(),
                "searchTimestamp", result.getTimestamp(),
                "queryExpansionScores", result.getQueryScores(),
                "documents", result.getDocuments().stream()
                    .map(scoredDoc -> Map.of(
                        "content", truncateContent(scoredDoc.getDocument().getContent(), 300),
                        "metadata", scoredDoc.getDocument().getMetadata(),
                        "scores", Map.of(
                            "final", Math.round(scoredDoc.getFinalScore() * 1000.0) / 1000.0,
                            "relevance", Math.round(scoredDoc.getRelevanceScore() * 1000.0) / 1000.0,
                            "quality", Math.round(scoredDoc.getQualityScore() * 1000.0) / 1000.0,
                            "recency", Math.round(scoredDoc.getRecencyScore() * 1000.0) / 1000.0
                        )
                    ))
                    .collect(Collectors.toList()),
                "searchConfiguration", Map.of(
                    "maxDocuments", config.getMaxDocuments(),
                    "similarityThreshold", config.getSimilarityThreshold(),
                    "queryExpansionEnabled", config.isUseQueryExpansion(),
                    "filterExpression", config.getFilterExpression()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "의미론적 검색 중 오류가 발생했습니다.",
                "details", e.getMessage(),
                "query", query
            ));
        }
    }

    @Operation(summary = "Perform semantic search with custom configuration",
               description = "Executes semantic search with detailed configuration options",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/search/advanced")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> advancedSemanticSearch(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String query = (String) requestBody.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "검색 쿼리가 필요합니다."
                ));
            }

            // 설정 파라미터 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> configMap = (Map<String, Object>) requestBody.getOrDefault("config", Map.of());
            
            int maxDocs = (Integer) configMap.getOrDefault("maxDocuments", 10);
            double threshold = ((Number) configMap.getOrDefault("similarityThreshold", 0.7)).doubleValue();
            boolean useQueryExpansion = (Boolean) configMap.getOrDefault("useQueryExpansion", true);
            String filter = (String) configMap.get("filterExpression");

            // 검색 설정 구성
            SemanticSearchService.SearchConfiguration config = new SemanticSearchService.SearchConfiguration(
                maxDocs, threshold, useQueryExpansion);
            config.setFilterExpression(filter);

            // 의미론적 검색 수행
            SemanticSearchService.SemanticSearchResult result = 
                semanticSearchService.performSemanticSearch(query, config);

            // 상세한 분석 정보 포함
            return ResponseEntity.ok(Map.of(
                "searchSummary", Map.of(
                    "originalQuery", result.getOriginalQuery(),
                    "totalDocumentsFound", result.getDocuments().size(),
                    "searchDuration", System.currentTimeMillis() - result.getTimestamp(),
                    "averageScore", result.getDocuments().stream()
                        .mapToDouble(SemanticSearchService.ScoredDocument::getFinalScore)
                        .average().orElse(0.0)
                ),
                "queryAnalysis", Map.of(
                    "originalQuery", result.getOriginalQuery(),
                    "expandedQueries", result.getExpandedQueries(),
                    "expansionCount", result.getExpandedQueries().size() - 1,
                    "queryScores", result.getQueryScores()
                ),
                "documents", result.getDocuments().stream()
                    .map(scoredDoc -> Map.of(
                        "id", scoredDoc.getDocument().getId(),
                        "content", Map.of(
                            "preview", truncateContent(scoredDoc.getDocument().getContent(), 200),
                            "fullLength", scoredDoc.getDocument().getContent().length(),
                            "wordCount", scoredDoc.getDocument().getContent().split("\\s+").length
                        ),
                        "metadata", scoredDoc.getDocument().getMetadata(),
                        "scoring", Map.of(
                            "finalScore", Math.round(scoredDoc.getFinalScore() * 1000.0) / 1000.0,
                            "components", Map.of(
                                "relevance", Math.round(scoredDoc.getRelevanceScore() * 1000.0) / 1000.0,
                                "quality", Math.round(scoredDoc.getQualityScore() * 1000.0) / 1000.0,
                                "recency", Math.round(scoredDoc.getRecencyScore() * 1000.0) / 1000.0
                            ),
                            "explanation", generateScoreExplanation(scoredDoc)
                        )
                    ))
                    .collect(Collectors.toList()),
                "searchConfiguration", config
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "고급 의미론적 검색 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Compare multiple search strategies",
               description = "Compares results from different search configurations to help optimize search parameters",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/search/compare")
    @CircuitBreaker(name = "externalService")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> compareSearchStrategies(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String query = (String) requestBody.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "검색 쿼리가 필요합니다."
                ));
            }

            // 다양한 검색 전략 정의
            Map<String, SemanticSearchService.SearchConfiguration> strategies = Map.of(
                "conservative", new SemanticSearchService.SearchConfiguration(5, 0.8, false),
                "balanced", new SemanticSearchService.SearchConfiguration(10, 0.7, true),
                "expansive", new SemanticSearchService.SearchConfiguration(15, 0.6, true),
                "recent_focused", new SemanticSearchService.SearchConfiguration(8, 0.7, true)
            );

            Map<String, Object> comparisonResults = Map.of(
                "query", query,
                "strategies", strategies.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                SemanticSearchService.SemanticSearchResult result = 
                                    semanticSearchService.performSemanticSearch(query, entry.getValue());
                                
                                return Map.of(
                                    "configuration", entry.getValue(),
                                    "resultsCount", result.getDocuments().size(),
                                    "averageScore", result.getDocuments().stream()
                                        .mapToDouble(SemanticSearchService.ScoredDocument::getFinalScore)
                                        .average().orElse(0.0),
                                    "topDocuments", result.getDocuments().stream()
                                        .limit(3)
                                        .map(doc -> Map.of(
                                            "preview", truncateContent(doc.getDocument().getContent(), 100),
                                            "score", Math.round(doc.getFinalScore() * 1000.0) / 1000.0
                                        ))
                                        .collect(Collectors.toList())
                                );
                            } catch (Exception e) {
                                return Map.of("error", e.getMessage());
                            }
                        }
                    )),
                "recommendations", generateSearchRecommendations(query)
            );

            return ResponseEntity.ok(comparisonResults);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "검색 전략 비교 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * 텍스트 내용을 지정된 길이로 자르기
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 점수 설명 생성
     */
    private String generateScoreExplanation(SemanticSearchService.ScoredDocument scoredDoc) {
        StringBuilder explanation = new StringBuilder();
        
        if (scoredDoc.getRelevanceScore() > 0.8) {
            explanation.append("높은 관련성, ");
        } else if (scoredDoc.getRelevanceScore() > 0.5) {
            explanation.append("중간 관련성, ");
        } else {
            explanation.append("낮은 관련성, ");
        }
        
        if (scoredDoc.getQualityScore() > 0.7) {
            explanation.append("좋은 품질, ");
        } else {
            explanation.append("일반 품질, ");
        }
        
        if (scoredDoc.getRecencyScore() > 0.8) {
            explanation.append("최신 문서");
        } else if (scoredDoc.getRecencyScore() > 0.5) {
            explanation.append("중간 정도 최신 문서");
        } else {
            explanation.append("오래된 문서");
        }
        
        return explanation.toString();
    }

    /**
     * 검색 추천사항 생성
     */
    private Map<String, String> generateSearchRecommendations(String query) {
        Map<String, String> recommendations = Map.of(
            "query_optimization", "더 구체적인 키워드를 사용하거나 동의어를 포함해보세요",
            "threshold_tuning", "관련성이 낮다면 임계값을 낮춰보세요 (0.6-0.7)",
            "expansion_usage", "결과가 부족하다면 쿼리 확장을 활성화해보세요",
            "filter_application", "특정 카테고리나 시간대로 필터링을 고려해보세요"
        );
        
        return recommendations;
    }
}