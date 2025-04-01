package com.example.springmcp.model;

import java.time.LocalDateTime;

public class UrlMapping {
    private String shortKey;
    private String originalUrl;
    private LocalDateTime createdAt;

    public UrlMapping() {
    }

    public UrlMapping(String shortKey, String originalUrl) {
        this.shortKey = shortKey;
        this.originalUrl = originalUrl;
        this.createdAt = LocalDateTime.now();
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
