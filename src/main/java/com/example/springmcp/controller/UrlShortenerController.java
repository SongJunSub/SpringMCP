package com.example.springmcp.controller;

import com.example.springmcp.dto.UrlShortenerRequest;
import com.example.springmcp.dto.UrlShortenerResponse;
import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.service.UrlShortenerService;
import com.example.springmcp.service.MetricsService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/shorten")
@Tag(name = "URL Shortener API", description = "Endpoints for URL shortening and redirection")
@Validated
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final MetricsService metricsService;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlShortenerController(UrlShortenerService urlShortenerService, MetricsService metricsService) {
        this.urlShortenerService = urlShortenerService;
        this.metricsService = metricsService;
    }

    @Operation(summary = "Shorten a URL", description = "Creates a short URL for a given long URL. Optionally, a custom key can be provided.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "URL shortened successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    @ApiResponse(responseCode = "409", description = "Custom key already in use", content = @Content)
    @PostMapping
    public ResponseEntity<UrlShortenerResponse> shortenUrl(@Valid @RequestBody UrlShortenerRequest urlShortenerRequest) {
        Timer.Sample sample = metricsService.startUrlShortenTimer();
        
        try {
            UrlEntry urlEntry;
            String longUrl = urlShortenerRequest.getLongUrl();
            String customKey = urlShortenerRequest.getCustomKey();

            if (customKey != null && !customKey.isEmpty()) {
                urlEntry = urlShortenerService.shortenUrl(longUrl, customKey);
            } else {
                urlEntry = urlShortenerService.shortenUrl(longUrl);
            }
            
            String shortUrl = baseUrl + "/api/shorten/" + urlEntry.getShortUrl();
            UrlShortenerResponse response = new UrlShortenerResponse(urlEntry.getShortUrl(), urlEntry.getLongUrl(), shortUrl, urlEntry.getCreatedAt());
            
            // 메트릭 기록
            metricsService.recordUrlShortened(null); // 카테고리는 추후 구현 가능
            
            return ResponseEntity.created(URI.create("/api/shorten/" + urlEntry.getShortUrl())).body(response);
        } finally {
            metricsService.recordUrlShortenTime(sample);
        }
    }

    @Operation(summary = "Redirect to the original long URL", description = "Redirects to the original long URL associated with the given short key.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "302", description = "Redirect to original URL")
    @ApiResponse(responseCode = "404", description = "URL not found", content = @Content)
    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable @Parameter(description = "The short key of the URL", example = "abc123") @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{5}$", message = "Short key must be 6 alphanumeric characters and start with a letter") String shortKey) {
        Timer.Sample sample = metricsService.startUrlResolveTimer();
        
        try {
            String longUrl = urlShortenerService.getLongUrl(shortKey);
            if (longUrl != null) {
                // 메트릭 기록
                metricsService.recordUrlAccessed(shortKey);
                return ResponseEntity.status(302).location(URI.create(longUrl)).build();
            } else {
                // 메트릭 기록
                metricsService.recordUrlNotFound();
                return ResponseEntity.notFound().build();
            }
        } finally {
            metricsService.recordUrlResolveTime(sample);
        }
    }
}