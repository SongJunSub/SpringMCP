
package com.example.springmcp.config;

import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import jakarta.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class RagConfig {

    @Autowired
    private VectorStore vectorStore;

    @PostConstruct
    public void init() throws MalformedURLException {
        // Load PDF document
        Resource pdfResource = new UrlResource("https://docs.spring.io/spring-ai/reference/1.0-SNAPSHOT/pdf/spring-ai-reference.pdf");
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);

        // Load Text document
        Resource textResource = new ClassPathResource("sample.txt");
        TextReader textReader = new TextReader(textResource);

        TokenTextSplitter textSplitter = new TokenTextSplitter();

        // Combine documents from both readers
        vectorStore.accept(textSplitter.apply(Stream.concat(pdfReader.get().stream(), textReader.get().stream()).collect(Collectors.toList())));
    }
}
