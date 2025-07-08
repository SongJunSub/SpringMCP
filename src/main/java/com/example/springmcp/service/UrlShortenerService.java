package com.example.springmcp.service;

import com.example.springmcp.exception.DuplicateKeyException;
import com.example.springmcp.exception.UrlNotFoundException;
import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UrlShortenerService {

    @Autowired
    private UrlEntryRepository urlEntryRepository;

    private static final int KEY_LENGTH = 6;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    @CacheEvict(value = "urls", allEntries = true)
    public String shortenUrl(String longUrl) {
        return shortenUrl(longUrl, null);
    }

    @CacheEvict(value = "urls", allEntries = true)
    public String shortenUrl(String longUrl, String customKey) {
        String shortKey;
        if (customKey != null && !customKey.isEmpty()) {
            if (urlEntryRepository.findByShortUrl(customKey) != null) {
                throw new DuplicateKeyException("Custom key '" + customKey + "' already in use.");
            }
            shortKey = customKey;
        } else {
            shortKey = generateUniqueShortKey();
        }
        UrlEntry urlEntry = new UrlEntry(shortKey, longUrl);
        urlEntryRepository.save(urlEntry);
        return shortKey;
    }

    @Cacheable("urls")
    public String getLongUrl(String shortKey) {
        UrlEntry urlEntry = urlEntryRepository.findByShortUrl(shortKey);
        if (urlEntry == null) {
            throw new UrlNotFoundException("URL not found for key: " + shortKey);
        }
        return urlEntry.getLongUrl();
    }

    private String generateUniqueShortKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        String shortKey = sb.toString();
        // Ensure the key starts with a letter
        if (!Character.isLetter(shortKey.charAt(0))) {
            return generateUniqueShortKey(); // Regenerate if it doesn't start with a letter
        }
        // Check for uniqueness in the database
        if (urlEntryRepository.findByShortUrl(shortKey) != null) {
            return generateUniqueShortKey(); // Regenerate if not unique
        }
        return shortKey;
    }
}