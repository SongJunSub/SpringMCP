package com.example.springmcp.controller;

import com.example.springmcp.dto.UrlShortenRequest;
import com.example.springmcp.dto.UrlShortenResponse;
import com.example.springmcp.service.UrlShortenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class UrlShortenerController {
    private static final Logger log = LoggerFactory.getLogger(UrlShortenerController.class);
    private final UrlShortenerService urlShortenerService;

    @Autowired
    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@RequestBody UrlShortenRequest request) {
        log.info("Received request to shorten URL: {}", request.getUrl());
        
        try {
            UrlShortenResponse response = urlShortenerService.shortenUrl(request);
            log.info("Successfully shortened URL. Key: {}", response.getShortKey());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.info("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error while shortening URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/shorten/{shortKey}")
    public ResponseEntity<String> redirectToOriginalUrl(@PathVariable String shortKey) {
        log.info("Received request to get original URL for key: {}", shortKey);
        
        try {
            String originalUrl = urlShortenerService.getOriginalUrl(shortKey);
            log.info("Found original URL: {}", originalUrl);
            return ResponseEntity.ok(originalUrl);
        } catch (NoSuchElementException e) {
            log.info("Short key not found: {}", shortKey);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving original URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
