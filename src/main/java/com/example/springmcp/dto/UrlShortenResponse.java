package com.example.springmcp.dto;

public class UrlShortenResponse {
    private String originalUrl;
    private String shortUrl;
    private String shortKey;

    public UrlShortenResponse() {
    }

    public UrlShortenResponse(String originalUrl, String shortUrl, String shortKey) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.shortKey = shortKey;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }
}
