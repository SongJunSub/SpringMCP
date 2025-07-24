package com.example.springmcp.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticSearchService.class);
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;

    public SemanticSearchService(VectorStore vectorStore, 
                               EmbeddingModel embeddingModel,
                               ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                        .withModel("gpt-4")
                        .withTemperature(0.2)
                        .withMaxTokens(2000)
                        .build())
                .build();
    }

    /**
     * 의미론적 검색 수행
     */
    public SemanticSearchResult performSemanticSearch(String query, SearchConfiguration config) {
        try {
            logger.info("Performing semantic search for query: {}", query);
            
            // 쿼리 확장
            Set<String> expandedQueries = expandQueryWithAI(query, config.isUseQueryExpansion());
            
            List<Document> allDocuments = new ArrayList<>();
            Map<String, Double> queryScores = new HashMap<>();
            
            // 각 확장된 쿼리에 대해 검색 수행
            for (String expandedQuery : expandedQueries) {
                SearchRequest searchRequest = SearchRequest.query(expandedQuery)
                        .withTopK(config.getMaxDocuments())
                        .withSimilarityThreshold(config.getSimilarityThreshold())
                        .withFilterExpression(config.getFilterExpression());
                
                List<Document> docs = vectorStore.similaritySearch(searchRequest);
                allDocuments.addAll(docs);
                
                // 각 쿼리의 점수 저장
                queryScores.put(expandedQuery, calculateQueryRelevance(query, expandedQuery));
            }
            
            // 문서 중복 제거 및 재랭킹
            List<ScoredDocument> rankedDocuments = rerankDocuments(allDocuments, query, config);
            
            // 결과 생성
            return new SemanticSearchResult(
                query,
                expandedQueries,
                rankedDocuments,
                queryScores,
                System.currentTimeMillis()
            );
            
        } catch (Exception e) {
            logger.error("Error performing semantic search: {}", e.getMessage(), e);
            throw new RuntimeException("의미론적 검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * AI를 사용한 쿼리 확장
     */
    private Set<String> expandQueryWithAI(String originalQuery, boolean useAIExpansion) {
        Set<String> expandedQueries = new HashSet<>();
        expandedQueries.add(originalQuery);
        
        if (!useAIExpansion) {
            return expandedQueries;
        }
        
        try {
            PromptTemplate expansionTemplate = new PromptTemplate("""
                원본 쿼리: "{query}"
                
                위 쿼리와 의미상 유사하거나 관련된 검색어들을 5개 생성해주세요.
                각 검색어는 다른 관점이나 동의어를 포함해야 합니다.
                
                응답 형식:
                1. [검색어1]
                2. [검색어2]
                3. [검색어3]
                4. [검색어4]
                5. [검색어5]
                """);

            String response = chatClient.prompt()
                    .user(expansionTemplate.createMessage(Map.of("query", originalQuery)))
                    .call()
                    .content();

            // 응답에서 검색어 추출
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.matches("\\d+\\.\\s*\\[.*\\]")) {
                    String expandedQuery = line.replaceAll("\\d+\\.\\s*\\[(.*)\\]", "$1").trim();
                    if (!expandedQuery.isEmpty()) {
                        expandedQueries.add(expandedQuery);
                    }
                }
            }
            
            logger.info("Expanded query '{}' to {} terms", originalQuery, expandedQueries.size());
            
        } catch (Exception e) {
            logger.warn("Failed to expand query with AI: {}", e.getMessage());
        }
        
        return expandedQueries;
    }

    /**
     * 문서 재랭킹
     */
    private List<ScoredDocument> rerankDocuments(List<Document> documents, String originalQuery, SearchConfiguration config) {
        // 중복 제거
        Map<String, Document> uniqueDocuments = documents.stream()
                .collect(Collectors.toMap(
                    doc -> doc.getContent().substring(0, Math.min(100, doc.getContent().length())),
                    doc -> doc,
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));

        List<ScoredDocument> scoredDocuments = new ArrayList<>();
        
        for (Document doc : uniqueDocuments.values()) {
            double relevanceScore = calculateRelevanceScore(doc, originalQuery);
            double qualityScore = calculateQualityScore(doc);
            double recencyScore = calculateRecencyScore(doc);
            
            double finalScore = (relevanceScore * 0.5) + (qualityScore * 0.3) + (recencyScore * 0.2);
            
            scoredDocuments.add(new ScoredDocument(doc, finalScore, relevanceScore, qualityScore, recencyScore));
        }

        // 점수에 따라 정렬
        scoredDocuments.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));
        
        // 최대 문서 수 제한
        return scoredDocuments.stream()
                .limit(config.getMaxDocuments())
                .collect(Collectors.toList());
    }

    /**
     * 문서 관련성 점수 계산
     */
    private double calculateRelevanceScore(Document document, String query) {
        String content = document.getContent().toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        // 단순한 키워드 매칭 기반 점수
        String[] queryTerms = lowerQuery.split("\\s+");
        int matches = 0;
        
        for (String term : queryTerms) {
            if (content.contains(term)) {
                matches++;
            }
        }
        
        return (double) matches / queryTerms.length;
    }

    /**
     * 문서 품질 점수 계산
     */
    private double calculateQualityScore(Document document) {
        String content = document.getContent();
        
        // 기본적인 품질 지표들
        double lengthScore = Math.min(1.0, content.length() / 1000.0); // 긴 문서일수록 높은 점수
        double structureScore = content.matches(".*[.!?].*") ? 1.0 : 0.5; // 문장 구조가 있는지
        
        return (lengthScore + structureScore) / 2.0;
    }

    /**
     * 문서 최신성 점수 계산
     */
    private double calculateRecencyScore(Document document) {
        Object timestamp = document.getMetadata().get("processed_at");
        if (timestamp == null) {
            return 0.5; // 기본값
        }
        
        try {
            long processedTime = Long.parseLong(timestamp.toString());
            long currentTime = System.currentTimeMillis();
            long daysSinceProcessed = (currentTime - processedTime) / (1000 * 60 * 60 * 24);
            
            // 30일 이내는 1.0, 그 이후는 점차 감소
            return Math.max(0.1, 1.0 - (daysSinceProcessed / 30.0));
            
        } catch (Exception e) {
            return 0.5;
        }
    }

    /**
     * 쿼리 관련성 계산
     */
    private double calculateQueryRelevance(String originalQuery, String expandedQuery) {
        if (originalQuery.equals(expandedQuery)) {
            return 1.0;
        }
        
        // 단순한 단어 겹침 기반 계산
        String[] original = originalQuery.toLowerCase().split("\\s+");
        String[] expanded = expandedQuery.toLowerCase().split("\\s+");
        
        Set<String> originalSet = Set.of(original);
        Set<String> expandedSet = Set.of(expanded);
        
        Set<String> intersection = new HashSet<>(originalSet);
        intersection.retainAll(expandedSet);
        
        Set<String> union = new HashSet<>(originalSet);
        union.addAll(expandedSet);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    // 내부 클래스들
    public static class SearchConfiguration {
        private int maxDocuments = 10;
        private double similarityThreshold = 0.7;
        private boolean useQueryExpansion = true;
        private String filterExpression = null;

        // Constructors, getters, setters
        public SearchConfiguration() {}

        public SearchConfiguration(int maxDocuments, double similarityThreshold, boolean useQueryExpansion) {
            this.maxDocuments = maxDocuments;
            this.similarityThreshold = similarityThreshold;
            this.useQueryExpansion = useQueryExpansion;
        }

        public int getMaxDocuments() { return maxDocuments; }
        public void setMaxDocuments(int maxDocuments) { this.maxDocuments = maxDocuments; }
        
        public double getSimilarityThreshold() { return similarityThreshold; }
        public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
        
        public boolean isUseQueryExpansion() { return useQueryExpansion; }
        public void setUseQueryExpansion(boolean useQueryExpansion) { this.useQueryExpansion = useQueryExpansion; }
        
        public String getFilterExpression() { return filterExpression; }
        public void setFilterExpression(String filterExpression) { this.filterExpression = filterExpression; }
    }

    public static class ScoredDocument {
        private final Document document;
        private final double finalScore;
        private final double relevanceScore;
        private final double qualityScore;
        private final double recencyScore;

        public ScoredDocument(Document document, double finalScore, double relevanceScore, double qualityScore, double recencyScore) {
            this.document = document;
            this.finalScore = finalScore;
            this.relevanceScore = relevanceScore;
            this.qualityScore = qualityScore;
            this.recencyScore = recencyScore;
        }

        public Document getDocument() { return document; }
        public double getFinalScore() { return finalScore; }
        public double getRelevanceScore() { return relevanceScore; }
        public double getQualityScore() { return qualityScore; }
        public double getRecencyScore() { return recencyScore; }
    }

    public static class SemanticSearchResult {
        private final String originalQuery;
        private final Set<String> expandedQueries;
        private final List<ScoredDocument> documents;
        private final Map<String, Double> queryScores;
        private final long timestamp;

        public SemanticSearchResult(String originalQuery, Set<String> expandedQueries, 
                                  List<ScoredDocument> documents, Map<String, Double> queryScores, long timestamp) {
            this.originalQuery = originalQuery;
            this.expandedQueries = expandedQueries;
            this.documents = documents;
            this.queryScores = queryScores;
            this.timestamp = timestamp;
        }

        public String getOriginalQuery() { return originalQuery; }
        public Set<String> getExpandedQueries() { return expandedQueries; }
        public List<ScoredDocument> getDocuments() { return documents; }
        public Map<String, Double> getQueryScores() { return queryScores; }
        public long getTimestamp() { return timestamp; }
    }
}