package com.example.springmcp.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DocumentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentProcessingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * PDF 문서를 처리하여 벡터 스토어에 저장
     */
    public void processPdfDocument(Resource pdfResource, Map<String, Object> metadata) {
        try {
            logger.info("Processing PDF document: {}", pdfResource.getFilename());
            
            DocumentReader pdfReader = new PagePdfDocumentReader(pdfResource,
                    PagePdfDocumentReader.builder()
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                    .withNumberOfTopTextLinesToDelete(0)
                                    .withNumberOfBottomTextLinesToDelete(0)
                                    .build())
                            .build());

            List<Document> documents = pdfReader.get();
            enhanceDocumentsWithMetadata(documents, metadata);
            
            // 문서를 청크로 분할
            List<Document> chunks = textSplitter.apply(documents);
            logger.info("Split PDF into {} chunks", chunks.size());
            
            // 벡터 스토어에 저장
            vectorStore.add(chunks);
            logger.info("Successfully processed PDF document with {} chunks", chunks.size());
            
        } catch (Exception e) {
            logger.error("Error processing PDF document: {}", e.getMessage(), e);
            throw new RuntimeException("PDF 문서 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 다양한 형식의 문서를 Tika를 사용하여 처리
     */
    public void processDocument(Resource resource, Map<String, Object> metadata) {
        try {
            logger.info("Processing document with Tika: {}", resource.getFilename());
            
            TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
            List<Document> documents = tikaReader.get();
            enhanceDocumentsWithMetadata(documents, metadata);
            
            // 문서를 청크로 분할
            List<Document> chunks = textSplitter.apply(documents);
            logger.info("Split document into {} chunks", chunks.size());
            
            // 벡터 스토어에 저장
            vectorStore.add(chunks);
            logger.info("Successfully processed document with {} chunks", chunks.size());
            
        } catch (Exception e) {
            logger.error("Error processing document: {}", e.getMessage(), e);
            throw new RuntimeException("문서 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 텍스트 문서를 직접 처리
     */
    public void processTextDocument(String content, Map<String, Object> metadata) {
        try {
            logger.info("Processing text document");
            
            Document document = new Document(content, metadata);
            List<Document> chunks = textSplitter.apply(List.of(document));
            logger.info("Split text into {} chunks", chunks.size());
            
            // 벡터 스토어에 저장
            vectorStore.add(chunks);
            logger.info("Successfully processed text document with {} chunks", chunks.size());
            
        } catch (Exception e) {
            logger.error("Error processing text document: {}", e.getMessage(), e);
            throw new RuntimeException("텍스트 문서 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * URL에서 문서를 가져와서 처리
     */
    public void processUrlContent(String url, String content, Map<String, Object> metadata) {
        try {
            logger.info("Processing URL content: {}", url);
            
            Map<String, Object> enrichedMetadata = new HashMap<>(metadata);
            enrichedMetadata.put("source_url", url);
            enrichedMetadata.put("content_type", "web_page");
            enrichedMetadata.put("processed_at", System.currentTimeMillis());
            
            Document document = new Document(content, enrichedMetadata);
            List<Document> chunks = textSplitter.apply(List.of(document));
            logger.info("Split URL content into {} chunks", chunks.size());
            
            // 벡터 스토어에 저장
            vectorStore.add(chunks);
            logger.info("Successfully processed URL content with {} chunks", chunks.size());
            
        } catch (Exception e) {
            logger.error("Error processing URL content: {}", e.getMessage(), e);
            throw new RuntimeException("URL 컨텐츠 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 벡터 스토어에서 특정 메타데이터 기준으로 문서 삭제
     */
    public void deleteDocumentsByMetadata(String metadataKey, String metadataValue) {
        try {
            logger.info("Deleting documents with {}={}", metadataKey, metadataValue);
            
            // Note: VectorStore 인터페이스에 따라 삭제 방법이 다를 수 있음
            // 여기서는 기본적인 구현을 제공
            vectorStore.delete(List.of(metadataValue));
            
            logger.info("Successfully deleted documents");
            
        } catch (Exception e) {
            logger.error("Error deleting documents: {}", e.getMessage(), e);
            throw new RuntimeException("문서 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 문서에 메타데이터를 추가
     */
    private void enhanceDocumentsWithMetadata(List<Document> documents, Map<String, Object> metadata) {
        for (Document doc : documents) {
            Map<String, Object> docMetadata = new HashMap<>(doc.getMetadata());
            docMetadata.putAll(metadata);
            docMetadata.put("processed_at", System.currentTimeMillis());
            docMetadata.put("chunk_index", documents.indexOf(doc));
            
            // 문서 내용 요약 정보 추가
            String content = doc.getContent();
            docMetadata.put("content_length", content.length());
            docMetadata.put("word_count", content.split("\\s+").length);
            
            doc.getMetadata().clear();
            doc.getMetadata().putAll(docMetadata);
        }
    }

    /**
     * 벡터 스토어의 문서 수 조회
     */
    public long getDocumentCount() {
        try {
            // VectorStore 구현에 따라 다를 수 있음
            // 임시로 검색을 통해 추정
            return vectorStore.similaritySearch("").size();
        } catch (Exception e) {
            logger.warn("Could not get document count: {}", e.getMessage());
            return -1;
        }
    }
}