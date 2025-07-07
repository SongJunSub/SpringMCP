package com.example.springmcp.controller;

import com.example.springmcp.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/shorten")
@Tag(name = "URL Shortener API", description = "Endpoints for URL shortening and redirection")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Operation(summary = "Shorten a URL", description = "Creates a short URL for a given long URL. Optionally, a custom key can be provided.")
    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody @Parameter(description = "The long URL to shorten", example = "https://www.google.com") String longUrl, @RequestParam(required = false) @Parameter(description = "Optional custom key for the short URL", example = "mycustomurl") String customKey) {
        String shortKey;
        if (customKey != null && !customKey.isEmpty()) {
            shortKey = urlShortenerService.shortenUrl(longUrl, customKey);
        } else {
            shortKey = urlShortenerService.shortenUrl(longUrl);
        }
        return ResponseEntity.created(URI.create("/api/shorten/" + shortKey)).body(shortKey);
    }

    @Operation(summary = "Redirect to the original long URL", description = "Redirects to the original long URL associated with the given short key.")
    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable @Parameter(description = "The short key of the URL", example = "abc123") String shortKey) {
        String longUrl = urlShortenerService.getLongUrl(shortKey);
        if (longUrl != null) {
            return ResponseEntity.status(302).location(URI.create(longUrl)).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}