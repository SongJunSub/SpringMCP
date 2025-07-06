package com.example.springmcp.controller;

import com.example.springmcp.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/shorten")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody String longUrl) {
        String shortKey = urlShortenerService.shortenUrl(longUrl);
        return ResponseEntity.created(URI.create("/api/shorten/" + shortKey)).body(shortKey);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortKey) {
        String longUrl = urlShortenerService.getLongUrl(shortKey);
        if (longUrl != null) {
            return ResponseEntity.status(302).location(URI.create(longUrl)).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}