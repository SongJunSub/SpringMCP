package com.example.springmcp.dto;

public class UrlShortenRequest {
    private String url;

    public UrlShortenRequest() {
    }

    public UrlShortenRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
