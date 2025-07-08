
package com.example.springmcp.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class UrlShortenerRequest {

    @NotBlank(message = "Long URL cannot be empty")
    @URL(message = "Invalid URL format")
    private String longUrl;
    private String customKey;

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }
}
