package com.example.springmcp.service;

import com.example.springmcp.exception.DuplicateKeyException;
import com.example.springmcp.exception.UrlNotFoundException;
import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {

    @Mock
    private UrlEntryRepository urlEntryRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shortenUrlShouldReturnShortKey() {
        String longUrl = "https://www.google.com";
        UrlEntry urlEntry = new UrlEntry("abc123", longUrl);

        when(urlEntryRepository.save(any(UrlEntry.class))).thenReturn(urlEntry);
        when(urlEntryRepository.findByShortUrl(any(String.class)))
                .thenReturn(null); // For initial uniqueness check

        UrlEntry returnedUrlEntry = urlShortenerService.shortenUrl(longUrl);

        assertNotNull(returnedUrlEntry);
        assertNotNull(returnedUrlEntry.getShortUrl());
        assertEquals(6, returnedUrlEntry.getShortUrl().length());
        assertTrue(Character.isLetter(returnedUrlEntry.getShortUrl().charAt(0)));
    }

    @Test
    public void shortenUrlWithCustomKeyShouldReturnCustomKey() {
        String longUrl = "https://www.example.com";
        String customKey = "mykey";
        UrlEntry urlEntry = new UrlEntry(customKey, longUrl);

        when(urlEntryRepository.findByShortUrl(customKey)).thenReturn(null);
        when(urlEntryRepository.save(any(UrlEntry.class))).thenReturn(urlEntry);

        UrlEntry returnedUrlEntry = urlShortenerService.shortenUrl(longUrl, customKey);

        assertEquals(customKey, returnedUrlEntry.getShortUrl());
    }

    @Test
    public void shortenUrlWithExistingCustomKeyShouldThrowDuplicateKeyException() {
        String longUrl = "https://www.example.com";
        String customKey = "existingkey";
        UrlEntry existingUrlEntry = new UrlEntry(customKey, "https://www.oldurl.com");

        when(urlEntryRepository.findByShortUrl(customKey)).thenReturn(existingUrlEntry);

        assertThrows(DuplicateKeyException.class, () -> {
            urlShortenerService.shortenUrl(longUrl, customKey);
        });
    }

    @Test
    public void getLongUrlShouldReturnLongUrl() {
        String shortKey = "abc123";
        String longUrl = "https://www.google.com";
        UrlEntry urlEntry = new UrlEntry(shortKey, longUrl);

        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(urlEntry);

        UrlEntry retrievedUrlEntry = urlShortenerService.getUrlEntry(shortKey);

        assertNotNull(retrievedUrlEntry);
        assertEquals(longUrl, retrievedUrlEntry.getLongUrl());
    }

    @Test
    public void getLongUrlShouldThrowUrlNotFoundExceptionForInvalidKey() {
        String shortKey = "invalid";

        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(null);

        assertThrows(UrlNotFoundException.class, () -> {
            urlShortenerService.getUrlEntry(shortKey);
        });
    }
}