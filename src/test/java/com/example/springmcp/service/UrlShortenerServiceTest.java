package com.example.springmcp.service;

import com.example.springmcp.exception.DuplicateKeyException;
import com.example.springmcp.exception.UrlNotFoundException;
import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlEntryRepository urlEntryRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        // Set @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(urlShortenerService, "keyLength", 6);
        ReflectionTestUtils.setField(urlShortenerService, "alphanumeric", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    @Test
    void shortenUrl_generatesUniqueKeyAndSaves() {
        String longUrl = "https://www.example.com";
        UrlEntry expectedUrlEntry = new UrlEntry("short1", longUrl);

        when(urlEntryRepository.findByShortUrl(anyString())).thenReturn(null); // Ensure key is unique
        when(urlEntryRepository.save(any(UrlEntry.class))).thenReturn(expectedUrlEntry);

        UrlEntry result = urlShortenerService.shortenUrl(longUrl);

        assertNotNull(result);
        assertEquals(longUrl, result.getLongUrl());
        assertNotNull(result.getShortUrl());
        verify(urlEntryRepository, times(1)).save(any(UrlEntry.class));
    }

    @Test
    void shortenUrl_withCustomKey_savesCustomKey() {
        String longUrl = "https://www.example.com";
        String customKey = "mycustom";
        UrlEntry expectedUrlEntry = new UrlEntry(customKey, longUrl);

        when(urlEntryRepository.findByShortUrl(customKey)).thenReturn(null);
        when(urlEntryRepository.save(any(UrlEntry.class))).thenReturn(expectedUrlEntry);

        UrlEntry result = urlShortenerService.shortenUrl(longUrl, customKey);

        assertNotNull(result);
        assertEquals(longUrl, result.getLongUrl());
        assertEquals(customKey, result.getShortUrl());
        verify(urlEntryRepository, times(1)).save(any(UrlEntry.class));
    }

    @Test
    void shortenUrl_withCustomKey_throwsDuplicateKeyException() {
        String longUrl = "https://www.example.com";
        String customKey = "existing";
        when(urlEntryRepository.findByShortUrl(customKey)).thenReturn(new UrlEntry(customKey, longUrl));

        assertThrows(DuplicateKeyException.class, () -> urlShortenerService.shortenUrl(longUrl, customKey));
        verify(urlEntryRepository, never()).save(any(UrlEntry.class));
    }

    @Test
    void getUrlEntry_returnsExistingEntry() {
        String shortKey = "short1";
        String longUrl = "https://www.example.com";
        UrlEntry existingEntry = new UrlEntry(shortKey, longUrl);

        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(existingEntry);

        UrlEntry result = urlShortenerService.getUrlEntry(shortKey);

        assertNotNull(result);
        assertEquals(shortKey, result.getShortUrl());
        assertEquals(longUrl, result.getLongUrl());
    }

    @Test
    void getUrlEntry_throwsUrlNotFoundException() {
        String shortKey = "nonexistent";
        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(null);

        assertThrows(UrlNotFoundException.class, () -> urlShortenerService.getUrlEntry(shortKey));
    }

    @Test
    void getLongUrl_returnsLongUrl() {
        String shortKey = "short1";
        String longUrl = "https://www.example.com";
        UrlEntry existingEntry = new UrlEntry(shortKey, longUrl);

        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(existingEntry);

        String result = urlShortenerService.getLongUrl(shortKey);

        assertEquals(longUrl, result);
    }

    @Test
    void getLongUrl_throwsUrlNotFoundException() {
        String shortKey = "nonexistent";
        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(null);

        assertThrows(UrlNotFoundException.class, () -> urlShortenerService.getLongUrl(shortKey));
    }
}