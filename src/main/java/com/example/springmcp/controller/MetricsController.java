package com.example.springmcp.controller;

import com.example.springmcp.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics API", description = "Application metrics and monitoring endpoints")
public class MetricsController {

    private final MetricsService metricsService;

    @Autowired
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Get application metrics summary",
               description = "Returns a comprehensive summary of all application metrics",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        try {
            Map<String, Object> summary = metricsService.getMetricsSummary();
            
            // 추가 메타데이터 포함
            Map<String, Object> response = new HashMap<>(summary);
            response.put("timestamp", System.currentTimeMillis());
            response.put("healthy", metricsService.isHealthy());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get URL shortener metrics",
               description = "Returns detailed metrics for the URL shortener service",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/urls")
    public ResponseEntity<Map<String, Object>> getUrlMetrics() {
        try {
            Map<String, Object> summary = metricsService.getMetricsSummary();
            @SuppressWarnings("unchecked")
            Map<String, Object> urlMetrics = (Map<String, Object>) summary.get("urls");
            
            Map<String, Object> response = new HashMap<>(urlMetrics);
            response.put("timestamp", System.currentTimeMillis());
            response.put("category", "url_shortener");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "URL 메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get AI service metrics",
               description = "Returns detailed metrics for AI services (Chat, RAG)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/ai")
    public ResponseEntity<Map<String, Object>> getAiMetrics() {
        try {
            Map<String, Object> summary = metricsService.getMetricsSummary();
            @SuppressWarnings("unchecked")
            Map<String, Object> aiMetrics = (Map<String, Object>) summary.get("ai");
            
            Map<String, Object> response = new HashMap<>(aiMetrics);
            response.put("timestamp", System.currentTimeMillis());
            response.put("category", "ai_services");
            
            // AI 서비스 상태 추가 정보
            double totalRequests = ((Number) aiMetrics.get("chatRequests")).doubleValue() + 
                                 ((Number) aiMetrics.get("ragRequests")).doubleValue();
            double errorRate = totalRequests > 0 ? 
                ((Number) aiMetrics.get("errors")).doubleValue() / totalRequests : 0.0;
            
            response.put("totalRequests", totalRequests);
            response.put("errorRate", Math.round(errorRate * 10000.0) / 100.0); // 백분율로 표시
            response.put("status", errorRate < 0.05 ? "healthy" : "degraded");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "AI 메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get document processing metrics",
               description = "Returns metrics for document processing and vector store",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> getDocumentMetrics() {
        try {
            Map<String, Object> summary = metricsService.getMetricsSummary();
            @SuppressWarnings("unchecked")
            Map<String, Object> docMetrics = (Map<String, Object>) summary.get("documents");
            
            Map<String, Object> response = new HashMap<>(docMetrics);
            response.put("timestamp", System.currentTimeMillis());
            response.put("category", "document_processing");
            
            // 추가 계산된 메트릭
            long totalSize = ((Number) docMetrics.get("totalSize")).longValue();
            long documentsCount = ((Number) docMetrics.get("inVectorStore")).longValue();
            
            response.put("averageDocumentSize", documentsCount > 0 ? totalSize / documentsCount : 0);
            response.put("totalSizeMB", Math.round(totalSize / (1024.0 * 1024.0) * 100.0) / 100.0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "문서 메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get search metrics",
               description = "Returns metrics for search operations",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> getSearchMetrics() {
        try {
            Map<String, Object> summary = metricsService.getMetricsSummary();
            @SuppressWarnings("unchecked")
            Map<String, Object> searchMetrics = (Map<String, Object>) summary.get("search");
            
            Map<String, Object> response = new HashMap<>(searchMetrics);
            response.put("timestamp", System.currentTimeMillis());
            response.put("category", "search");
            
            // 검색 품질 평가
            double averageScore = ((Number) searchMetrics.get("averageScore")).doubleValue();
            String qualityRating;
            if (averageScore > 0.8) {
                qualityRating = "excellent";
            } else if (averageScore > 0.6) {
                qualityRating = "good";
            } else if (averageScore > 0.4) {
                qualityRating = "fair";
            } else {
                qualityRating = "poor";
            }
            
            response.put("qualityRating", qualityRating);
            response.put("averageScorePercent", Math.round(averageScore * 100.0));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "검색 메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get user activity metrics",
               description = "Returns user activity and engagement metrics",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserMetrics() {
        try {
            Map<String, Object> summary = metricsService.getMetricsSummary();
            @SuppressWarnings("unchecked")
            Map<String, Object> userMetrics = (Map<String, Object>) summary.get("users");
            
            Map<String, Object> response = new HashMap<>(userMetrics);
            response.put("timestamp", System.currentTimeMillis());
            response.put("category", "user_activity");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "사용자 메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get application health status",
               description = "Returns overall application health based on metrics",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthMetrics() {
        try {
            boolean isHealthy = metricsService.isHealthy();
            Map<String, Object> summary = metricsService.getMetricsSummary();
            
            Map<String, Object> response = Map.of(
                "status", isHealthy ? "healthy" : "unhealthy",
                "timestamp", System.currentTimeMillis(),
                "uptime", getUptime(),
                "components", Map.of(
                    "ai_services", checkAiServiceHealth(summary),
                    "url_shortener", checkUrlShortenerHealth(summary),
                    "document_processing", checkDocumentProcessingHealth(summary),
                    "search", checkSearchHealth(summary)
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "헬스 메트릭 조회 중 오류가 발생했습니다.",
                "details", e.getMessage(),
                "status", "unhealthy"
            ));
        }
    }

    @Operation(summary = "Record custom metric event",
               description = "Allows recording of custom application events for monitoring",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> recordEvent(
            @RequestBody Map<String, Object> eventData) {
        
        try {
            String eventType = (String) eventData.get("type");
            String category = (String) eventData.get("category");
            Object value = eventData.get("value");
            
            if (eventType == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "이벤트 타입이 필요합니다."
                ));
            }
            
            // 사용자 활동 이벤트 처리
            if ("user_activity".equals(eventType)) {
                String userId = (String) eventData.get("userId");
                if (userId != null) {
                    metricsService.recordUserActivity(userId);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "이벤트가 성공적으로 기록되었습니다.",
                "eventType", eventType,
                "category", category,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "이벤트 기록 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    // 헬퍼 메서드들
    private long getUptime() {
        // 실제 구현에서는 애플리케이션 시작 시간을 저장하고 계산
        return System.currentTimeMillis();
    }

    private String checkAiServiceHealth(Map<String, Object> summary) {
        @SuppressWarnings("unchecked")
        Map<String, Object> aiMetrics = (Map<String, Object>) summary.get("ai");
        
        double totalRequests = ((Number) aiMetrics.get("chatRequests")).doubleValue() + 
                             ((Number) aiMetrics.get("ragRequests")).doubleValue();
        double errors = ((Number) aiMetrics.get("errors")).doubleValue();
        double errorRate = totalRequests > 0 ? errors / totalRequests : 0.0;
        
        return errorRate < 0.05 ? "healthy" : "degraded";
    }

    private String checkUrlShortenerHealth(Map<String, Object> summary) {
        @SuppressWarnings("unchecked")
        Map<String, Object> urlMetrics = (Map<String, Object>) summary.get("urls");
        
        long activeUrls = ((Number) urlMetrics.get("active")).longValue();
        return activeUrls >= 0 ? "healthy" : "unknown";
    }

    private String checkDocumentProcessingHealth(Map<String, Object> summary) {
        @SuppressWarnings("unchecked")
        Map<String, Object> docMetrics = (Map<String, Object>) summary.get("documents");
        
        long processed = ((Number) docMetrics.get("processed")).longValue();
        long uploaded = ((Number) docMetrics.get("uploaded")).longValue();
        
        return uploaded == 0 || processed >= uploaded * 0.9 ? "healthy" : "degraded";
    }

    private String checkSearchHealth(Map<String, Object> summary) {
        @SuppressWarnings("unchecked")
        Map<String, Object> searchMetrics = (Map<String, Object>) summary.get("search");
        
        double averageScore = ((Number) searchMetrics.get("averageScore")).doubleValue();
        return averageScore > 0.5 ? "healthy" : "degraded";
    }
}