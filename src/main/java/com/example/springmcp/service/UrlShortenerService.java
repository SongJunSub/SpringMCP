package com.example.springmcp.service;

import com.example.springmcp.dto.UrlShortenRequest;
import com.example.springmcp.dto.UrlShortenResponse;
import com.example.springmcp.model.UrlMapping;
import com.example.springmcp.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.NoSuchElementException;

@Service
public class UrlShortenerService {
    private static final Logger log = LoggerFactory.getLogger(UrlShortenerService.class);
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository urlRepository;
    private final String baseUrl;

    @Autowired
    public UrlShortenerService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
        // In a real application, this might come from configuration
        this.baseUrl = "http://localhost:8080/api/shorten/";
    }

    public UrlShortenResponse shortenUrl(UrlShortenRequest request) {
        log.info("Shortening URL: {}", request.getUrl());
        
        if (request.getUrl() == null || request.getUrl().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        String shortKey = generateUniqueKey();
        UrlMapping urlMapping = new UrlMapping(shortKey, request.getUrl());
        urlRepository.save(urlMapping);
        
        String shortUrl = baseUrl + shortKey;
        log.info("URL shortened. Original: {}, Short key: {}", request.getUrl(), shortKey);
        
        return new UrlShortenResponse(request.getUrl(), shortUrl, shortKey);
    }

    public String getOriginalUrl(String shortKey) {
        log.info("Retrieving original URL for key: {}", shortKey);
        
        return urlRepository.findByShortKey(shortKey)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> {
                    log.info("Short key not found: {}", shortKey);
                    return new NoSuchElementException("No URL found for key: " + shortKey);
                });
    }

    private String generateUniqueKey() {
        while (true) {
            String key = generateRandomKey();
            if (!urlRepository.existsByShortKey(key)) {
                return key;
            }
        }
    }

    private String generateRandomKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        
        // Ensure first character is a letter
        sb.append(CHARACTERS.charAt(RANDOM.nextInt(52))); // First 52 chars are letters
        
        // Generate the rest of the key
        for (int i = 1; i < KEY_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        
        return sb.toString();
    }
}
