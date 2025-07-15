
package com.example.springmcp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public class UrlShortenerRequest {

    @NotBlank(message = "Long URL cannot be empty")
    @URL(message = "Invalid URL format")
    private String longUrl;
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{5}$", message = "Custom key must be 6 alphanumeric characters and start with a letter")
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
