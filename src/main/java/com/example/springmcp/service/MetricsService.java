package com.example.springmcp.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicDouble;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    private final MeterRegistry meterRegistry;
    
    // URL Shortener 메트릭
    private final Counter urlShortenedCounter;
    private final Counter urlAccessedCounter;
    private final Counter urlNotFoundCounter;
    private final Timer urlShortenTimer;
    private final Timer urlResolveTimer;
    private final AtomicLong totalUrlsCreated = new AtomicLong(0);
    private final AtomicLong activeUrls = new AtomicLong(0);
    
    // AI 관련 메트릭
    private final Counter aiChatRequestCounter;
    private final Counter aiRagRequestCounter;
    private final Counter aiErrorCounter;
    private final Timer aiChatResponseTimer;
    private final Timer aiRagResponseTimer;
    private final AtomicDouble averageAiResponseTime = new AtomicDouble(0.0);
    private final AtomicLong aiTokensUsed = new AtomicLong(0);
    
    // 문서 처리 메트릭
    private final Counter documentsProcessedCounter;
    private final Counter documentsUploadedCounter;
    private final Timer documentProcessingTimer;
    private final AtomicLong totalDocumentsInVectorStore = new AtomicLong(0);
    private final AtomicLong totalDocumentSize = new AtomicLong(0);
    
    // 검색 메트릭
    private final Counter searchRequestCounter;
    private final Counter semanticSearchCounter;
    private final Timer searchResponseTimer;
    private final AtomicDouble averageSearchScore = new AtomicDouble(0.0);
    
    // 비즈니스 메트릭
    private final Map<String, AtomicLong> categoryCounters = new ConcurrentHashMap<>();
    private final AtomicLong dailyActiveUsers = new AtomicLong(0);
    private final AtomicLong peakConcurrentUsers = new AtomicLong(0);

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // URL Shortener 메트릭 초기화
        this.urlShortenedCounter = Counter.builder("url_shortened_total")
                .description("Total number of URLs shortened")
                .register(meterRegistry);
                
        this.urlAccessedCounter = Counter.builder("url_accessed_total")
                .description("Total number of URL accesses")
                .register(meterRegistry);
                
        this.urlNotFoundCounter = Counter.builder("url_not_found_total")
                .description("Total number of URL not found errors")
                .register(meterRegistry);
                
        this.urlShortenTimer = Timer.builder("url_shorten_duration")
                .description("Time taken to shorten URLs")
                .register(meterRegistry);
                
        this.urlResolveTimer = Timer.builder("url_resolve_duration")
                .description("Time taken to resolve URLs")
                .register(meterRegistry);
        
        // AI 관련 메트릭 초기화
        this.aiChatRequestCounter = Counter.builder("ai_chat_requests_total")
                .description("Total number of AI chat requests")
                .register(meterRegistry);
                
        this.aiRagRequestCounter = Counter.builder("ai_rag_requests_total")
                .description("Total number of AI RAG requests")
                .register(meterRegistry);
                
        this.aiErrorCounter = Counter.builder("ai_errors_total")
                .description("Total number of AI service errors")
                .register(meterRegistry);
                
        this.aiChatResponseTimer = Timer.builder("ai_chat_response_duration")
                .description("Time taken for AI chat responses")
                .register(meterRegistry);
                
        this.aiRagResponseTimer = Timer.builder("ai_rag_response_duration")
                .description("Time taken for AI RAG responses")
                .register(meterRegistry);
        
        // 문서 처리 메트릭 초기화
        this.documentsProcessedCounter = Counter.builder("documents_processed_total")
                .description("Total number of documents processed")
                .register(meterRegistry);
                
        this.documentsUploadedCounter = Counter.builder("documents_uploaded_total")
                .description("Total number of documents uploaded")
                .register(meterRegistry);
                
        this.documentProcessingTimer = Timer.builder("document_processing_duration")
                .description("Time taken to process documents")
                .register(meterRegistry);
        
        // 검색 메트릭 초기화
        this.searchRequestCounter = Counter.builder("search_requests_total")
                .description("Total number of search requests")
                .register(meterRegistry);
                
        this.semanticSearchCounter = Counter.builder("semantic_search_requests_total")
                .description("Total number of semantic search requests")
                .register(meterRegistry);
                
        this.searchResponseTimer = Timer.builder("search_response_duration")
                .description("Time taken for search responses")
                .register(meterRegistry);
        
        // Gauge 메트릭 등록
        Gauge.builder("urls_active_total")
                .description("Total number of active URLs")
                .register(meterRegistry, activeUrls, AtomicLong::get);
                
        Gauge.builder("ai_average_response_time_seconds")
                .description("Average AI response time")
                .register(meterRegistry, averageAiResponseTime, AtomicDouble::get);
                
        Gauge.builder("ai_tokens_used_total")
                .description("Total AI tokens used")
                .register(meterRegistry, aiTokensUsed, AtomicLong::get);
                
        Gauge.builder("documents_in_vector_store_total")
                .description("Total documents in vector store")
                .register(meterRegistry, totalDocumentsInVectorStore, AtomicLong::get);
                
        Gauge.builder("documents_total_size_bytes")
                .description("Total size of all documents")
                .register(meterRegistry, totalDocumentSize, AtomicLong::get);
                
        Gauge.builder("search_average_score")
                .description("Average search relevance score")
                .register(meterRegistry, averageSearchScore, AtomicDouble::get);
                
        Gauge.builder("users_daily_active")
                .description("Daily active users")
                .register(meterRegistry, dailyActiveUsers, AtomicLong::get);
                
        Gauge.builder("users_peak_concurrent")
                .description("Peak concurrent users")
                .register(meterRegistry, peakConcurrentUsers, AtomicLong::get);
        
        logger.info("MetricsService initialized with {} meters", meterRegistry.getMeters().size());
    }

    // URL Shortener 메트릭 메서드들
    public void recordUrlShortened(String category) {
        urlShortenedCounter.increment();
        totalUrlsCreated.incrementAndGet();
        activeUrls.incrementAndGet();
        
        if (category != null) {
            categoryCounters.computeIfAbsent(category, k -> 
                Gauge.builder("urls_by_category")
                    .description("URLs by category")
                    .tag("category", k)
                    .register(meterRegistry, new AtomicLong(0), AtomicLong::get))
                .incrementAndGet();
        }
        
        logger.debug("URL shortened, total active URLs: {}", activeUrls.get());
    }

    public void recordUrlAccessed(String shortUrl) {
        urlAccessedCounter.increment();
        logger.debug("URL accessed: {}", shortUrl);
    }

    public void recordUrlNotFound() {
        urlNotFoundCounter.increment();
        logger.debug("URL not found error recorded");
    }

    public Timer.Sample startUrlShortenTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordUrlShortenTime(Timer.Sample sample) {
        sample.stop(urlShortenTimer);
    }

    public Timer.Sample startUrlResolveTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordUrlResolveTime(Timer.Sample sample) {
        sample.stop(urlResolveTimer);
    }

    public void recordUrlDeleted() {
        activeUrls.decrementAndGet();
        logger.debug("URL deleted, active URLs: {}", activeUrls.get());
    }

    // AI 메트릭 메서드들
    public void recordAiChatRequest() {
        aiChatRequestCounter.increment();
        logger.debug("AI chat request recorded");
    }

    public void recordAiRagRequest() {
        aiRagRequestCounter.increment();
        logger.debug("AI RAG request recorded");
    }

    public void recordAiError(String errorType) {
        aiErrorCounter.increment("error_type", errorType);
        logger.debug("AI error recorded: {}", errorType);
    }

    public Timer.Sample startAiChatTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAiChatTime(Timer.Sample sample) {
        sample.stop(aiChatResponseTimer);
        updateAverageAiResponseTime();
    }

    public Timer.Sample startAiRagTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAiRagTime(Timer.Sample sample) {
        sample.stop(aiRagResponseTimer);
        updateAverageAiResponseTime();
    }

    public void recordAiTokensUsed(long tokens) {
        aiTokensUsed.addAndGet(tokens);
        logger.debug("AI tokens used: {}, total: {}", tokens, aiTokensUsed.get());
    }

    private void updateAverageAiResponseTime() {
        double chatAvg = aiChatResponseTimer.mean();
        double ragAvg = aiRagResponseTimer.mean();
        averageAiResponseTime.set((chatAvg + ragAvg) / 2.0);
    }

    // 문서 처리 메트릭 메서드들
    public void recordDocumentProcessed(String type, long sizeBytes) {
        documentsProcessedCounter.increment("type", type);
        totalDocumentsInVectorStore.incrementAndGet();
        totalDocumentSize.addAndGet(sizeBytes);
        logger.debug("Document processed: type={}, size={} bytes", type, sizeBytes);
    }

    public void recordDocumentUploaded(String contentType, long sizeBytes) {
        documentsUploadedCounter.increment("content_type", contentType);
        logger.debug("Document uploaded: type={}, size={} bytes", contentType, sizeBytes);
    }

    public Timer.Sample startDocumentProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDocumentProcessingTime(Timer.Sample sample) {
        sample.stop(documentProcessingTimer);
    }

    // 검색 메트릭 메서드들
    public void recordSearchRequest(String searchType) {
        searchRequestCounter.increment("type", searchType);
        logger.debug("Search request recorded: {}", searchType);
    }

    public void recordSemanticSearch(int documentsFound, double averageScore) {
        semanticSearchCounter.increment();
        averageSearchScore.set(averageScore);
        logger.debug("Semantic search recorded: {} docs, avg score: {}", documentsFound, averageScore);
    }

    public Timer.Sample startSearchTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordSearchTime(Timer.Sample sample) {
        sample.stop(searchResponseTimer);
    }

    // 사용자 메트릭 메서드들
    public void recordUserActivity(String userId) {
        // 실제 구현에서는 Redis나 다른 캐시를 사용하여 일일 활성 사용자 추적
        dailyActiveUsers.incrementAndGet();
        logger.debug("User activity recorded for: {}", userId);
    }

    public void updatePeakConcurrentUsers(long currentUsers) {
        if (currentUsers > peakConcurrentUsers.get()) {
            peakConcurrentUsers.set(currentUsers);
            logger.info("New peak concurrent users: {}", currentUsers);
        }
    }

    // 비즈니스 메트릭 조회 메서드들
    public Map<String, Object> getMetricsSummary() {
        return Map.of(
            "urls", Map.of(
                "totalCreated", totalUrlsCreated.get(),
                "active", activeUrls.get(),
                "accessCount", urlAccessedCounter.count(),
                "notFoundCount", urlNotFoundCounter.count()
            ),
            "ai", Map.of(
                "chatRequests", aiChatRequestCounter.count(),
                "ragRequests", aiRagRequestCounter.count(),
                "errors", aiErrorCounter.count(),
                "tokensUsed", aiTokensUsed.get(),
                "averageResponseTime", averageAiResponseTime.get()
            ),
            "documents", Map.of(
                "processed", documentsProcessedCounter.count(),
                "uploaded", documentsUploadedCounter.count(),
                "inVectorStore", totalDocumentsInVectorStore.get(),
                "totalSize", totalDocumentSize.get()
            ),
            "search", Map.of(
                "requests", searchRequestCounter.count(),
                "semanticRequests", semanticSearchCounter.count(),
                "averageScore", averageSearchScore.get()
            ),
            "users", Map.of(
                "dailyActive", dailyActiveUsers.get(),
                "peakConcurrent", peakConcurrentUsers.get()
            )
        );
    }

    // 헬스 체크용 메트릭
    public boolean isHealthy() {
        // 기본적인 헬스 체크 로직
        double errorRate = getErrorRate();
        double averageResponseTime = getAverageResponseTime();
        
        return errorRate < 0.05 && averageResponseTime < 5.0; // 5% 미만 에러율, 5초 미만 응답시간
    }

    private double getErrorRate() {
        double totalRequests = aiChatRequestCounter.count() + aiRagRequestCounter.count() + searchRequestCounter.count();
        return totalRequests > 0 ? aiErrorCounter.count() / totalRequests : 0.0;
    }

    private double getAverageResponseTime() {
        return averageAiResponseTime.get();
    }
}