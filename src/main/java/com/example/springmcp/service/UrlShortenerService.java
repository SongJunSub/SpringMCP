package com.example.springmcp.service;

import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UrlShortenerService {

    @Autowired
    private UrlEntryRepository urlEntryRepository;

    private static final int KEY_LENGTH = 6;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    public String shortenUrl(String longUrl) {
        String shortKey = generateUniqueShortKey();
        UrlEntry urlEntry = new UrlEntry(shortKey, longUrl);
        urlEntryRepository.save(urlEntry);
        return shortKey;
    }

    public String getLongUrl(String shortKey) {
        UrlEntry urlEntry = urlEntryRepository.findByShortUrl(shortKey);
        return (urlEntry != null) ? urlEntry.getLongUrl() : null;
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