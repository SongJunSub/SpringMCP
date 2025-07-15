package com.example.springmcp.service;

import com.example.springmcp.exception.DuplicateKeyException;
import com.example.springmcp.exception.UrlNotFoundException;
import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class UrlShortenerService {

    private final UrlEntryRepository urlEntryRepository;

    public UrlShortenerService(UrlEntryRepository urlEntryRepository) {
        this.urlEntryRepository = urlEntryRepository;
    }

    @Value("${app.shortener.key-length}")
    private int keyLength;

    @Value("${app.shortener.alphanumeric-characters}")
    private String alphanumeric;

    private static SecureRandom random = new SecureRandom();

    @Transactional
    @CachePut(value = "urls", key = "#result")
    public String shortenUrl(String longUrl) {
        return shortenUrl(longUrl, null);
    }

    @Transactional
    @CachePut(value = "urls", key = "#result")
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

    @Cacheable(value = "urls", key = "#shortKey")
    public UrlEntry getUrlEntry(String shortKey) {
        UrlEntry urlEntry = urlEntryRepository.findByShortUrl(shortKey);
        if (urlEntry == null) {
            throw new UrlNotFoundException("URL not found for key: " + shortKey);
        }
        return urlEntry;
    }

    public String getLongUrl(String shortKey) {
        return getUrlEntry(shortKey).getLongUrl();
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            StringBuilder sb = new StringBuilder(keyLength);
            for (int i = 0; i < keyLength; i++) {
                sb.append(alphanumeric.charAt(random.nextInt(alphanumeric.length())));
            }
            shortKey = sb.toString();
        } while (!Character.isLetter(shortKey.charAt(0)) || urlEntryRepository.findByShortUrl(shortKey) != null);
        
        return shortKey;
    }
}