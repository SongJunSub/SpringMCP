package com.example.springmcp.controller;

import com.example.springmcp.service.DocumentProcessingService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Processing API", description = "Endpoints for processing and managing documents in the vector store")
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @Autowired
    public DocumentController(DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
    }

    @Operation(summary = "Upload and process a PDF document",
               description = "Uploads a PDF file, processes it, and adds it to the vector store for RAG queries",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload/pdf")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @Parameter(description = "PDF file to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Document category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Document tags") @RequestParam(value = "tags", required = false) String tags) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "파일이 비어있습니다."
                ));
            }

            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "PDF 파일만 업로드 가능합니다."
                ));
            }

            // 메타데이터 설정
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getOriginalFilename());
            metadata.put("content_type", file.getContentType());
            metadata.put("file_size", file.getSize());
            metadata.put("upload_time", System.currentTimeMillis());
            
            if (title != null && !title.trim().isEmpty()) {
                metadata.put("title", title);
            }
            if (category != null && !category.trim().isEmpty()) {
                metadata.put("category", category);
            }
            if (tags != null && !tags.trim().isEmpty()) {
                metadata.put("tags", tags);
            }

            // PDF 파일을 Resource로 변환
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // 문서 처리
            documentProcessingService.processPdfDocument(resource, metadata);

            return ResponseEntity.ok(Map.of(
                "message", "PDF 문서가 성공적으로 처리되어 벡터 스토어에 추가되었습니다.",
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "metadata", metadata
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "PDF 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Upload and process any document",
               description = "Uploads any document file, processes it with Tika, and adds it to the vector store",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/upload/document")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @Parameter(description = "Document file to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document title") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "Document category") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "Document tags") @RequestParam(value = "tags", required = false) String tags) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "파일이 비어있습니다."
                ));
            }

            // 메타데이터 설정
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("filename", file.getOriginalFilename());
            metadata.put("content_type", file.getContentType());
            metadata.put("file_size", file.getSize());
            metadata.put("upload_time", System.currentTimeMillis());
            
            if (title != null && !title.trim().isEmpty()) {
                metadata.put("title", title);
            }
            if (category != null && !category.trim().isEmpty()) {
                metadata.put("category", category);
            }
            if (tags != null && !tags.trim().isEmpty()) {
                metadata.put("tags", tags);
            }

            // 파일을 Resource로 변환
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // 문서 처리
            documentProcessingService.processDocument(resource, metadata);

            return ResponseEntity.ok(Map.of(
                "message", "문서가 성공적으로 처리되어 벡터 스토어에 추가되었습니다.",
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "contentType", file.getContentType(),
                "metadata", metadata
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "문서 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Process text content",
               description = "Processes plain text content and adds it to the vector store",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/process/text")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> processText(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String content = (String) requestBody.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "텍스트 내용이 필요합니다."
                ));
            }

            // 메타데이터 설정
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("content_type", "text/plain");
            metadata.put("content_length", content.length());
            metadata.put("upload_time", System.currentTimeMillis());
            
            // 추가 메타데이터가 있다면 포함
            if (requestBody.containsKey("metadata")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> additionalMetadata = (Map<String, Object>) requestBody.get("metadata");
                metadata.putAll(additionalMetadata);
            }

            // 텍스트 처리
            documentProcessingService.processTextDocument(content, metadata);

            return ResponseEntity.ok(Map.of(
                "message", "텍스트가 성공적으로 처리되어 벡터 스토어에 추가되었습니다.",
                "contentLength", content.length(),
                "metadata", metadata
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "텍스트 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Process URL content",
               description = "Fetches content from a URL and processes it for the vector store",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/process/url")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> processUrl(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String url = (String) requestBody.get("url");
            String content = (String) requestBody.get("content");
            
            if (url == null || url.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "URL이 필요합니다."
                ));
            }
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "컨텐츠가 필요합니다."
                ));
            }

            // 메타데이터 설정
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source_type", "url");
            metadata.put("url", url);
            metadata.put("content_length", content.length());
            metadata.put("processed_time", System.currentTimeMillis());
            
            // 추가 메타데이터가 있다면 포함
            if (requestBody.containsKey("metadata")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> additionalMetadata = (Map<String, Object>) requestBody.get("metadata");
                metadata.putAll(additionalMetadata);
            }

            // URL 컨텐츠 처리
            documentProcessingService.processUrlContent(url, content, metadata);

            return ResponseEntity.ok(Map.of(
                "message", "URL 컨텐츠가 성공적으로 처리되어 벡터 스토어에 추가되었습니다.",
                "url", url,
                "contentLength", content.length(),
                "metadata", metadata
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "URL 컨텐츠 처리 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Delete documents by metadata",
               description = "Deletes documents from vector store based on metadata criteria",
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/delete")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> deleteDocuments(
            @Parameter(description = "Metadata key to filter by") @RequestParam String metadataKey,
            @Parameter(description = "Metadata value to match") @RequestParam String metadataValue) {
        
        try {
            documentProcessingService.deleteDocumentsByMetadata(metadataKey, metadataValue);
            
            return ResponseEntity.ok(Map.of(
                "message", "문서가 성공적으로 삭제되었습니다.",
                "criteria", Map.of(metadataKey, metadataValue)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "문서 삭제 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get document statistics",
               description = "Returns statistics about documents in the vector store",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/stats")
    @RateLimiter(name = "default")
    public ResponseEntity<Map<String, Object>> getDocumentStats() {
        
        try {
            long documentCount = documentProcessingService.getDocumentCount();
            
            return ResponseEntity.ok(Map.of(
                "totalDocuments", documentCount,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "통계 조회 중 오류가 발생했습니다.",
                "details", e.getMessage()
            ));
        }
    }
}