package com.example.springmcp.dto;

import java.time.LocalDateTime;

public class UrlShortenerResponse {
    private String shortKey;
    private String longUrl;
    private String shortUrl;
    private LocalDateTime createdAt;

    public UrlShortenerResponse(String shortKey, String longUrl, String shortUrl, LocalDateTime createdAt) {
        this.shortKey = shortKey;
        this.longUrl = longUrl;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}