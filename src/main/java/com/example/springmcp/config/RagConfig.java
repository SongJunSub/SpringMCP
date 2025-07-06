
package com.example.springmcp.config;

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import jakarta.annotation.PostConstruct;
import java.net.MalformedURLException;

@Configuration
public class RagConfig {

    @Autowired
    private VectorStore vectorStore;

    @PostConstruct
    public void init() throws MalformedURLException {
        Resource resource = new UrlResource("https://docs.spring.io/spring-ai/reference/1.0-SNAPSHOT/pdf/spring-ai-reference.pdf");
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(pdfReader.get()));
    }
}
