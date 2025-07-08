package com.example.springmcp.controller;

import com.example.springmcp.dto.UrlShortenerRequest;
import com.example.springmcp.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "Shorten a URL", description = "Creates a short URL for a given long URL. Optionally, a custom key can be provided.", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "201", description = "URL shortened successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    @ApiResponse(responseCode = "409", description = "Custom key already in use", content = @Content)
    @PostMapping
    public ResponseEntity<String> shortenUrl(@Valid @RequestBody UrlShortenerRequest urlShortenerRequest) {
        String shortKey;
        String longUrl = urlShortenerRequest.getLongUrl();
        String customKey = urlShortenerRequest.getCustomKey();

        if (customKey != null && !customKey.isEmpty()) {
            shortKey = urlShortenerService.shortenUrl(longUrl, customKey);
        } else {
            shortKey = urlShortenerService.shortenUrl(longUrl);
        }
        return ResponseEntity.created(URI.create("/api/shorten/" + shortKey)).body(shortKey);
    }

    @Operation(summary = "Redirect to the original long URL", description = "Redirects to the original long URL associated with the given short key.", security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "302", description = "Redirect to original URL")
    @ApiResponse(responseCode = "404", description = "URL not found", content = @Content)
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