
package com.example.springmcp.service;

import com.example.springmcp.model.UrlEntry;
import com.example.springmcp.repository.UrlEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
                .thenReturn(null) // For initial uniqueness check
                .thenReturn(urlEntry); // For the save operation

        String shortKey = urlShortenerService.shortenUrl(longUrl);

        assertNotNull(shortKey);
        assertEquals(6, shortKey.length());
        assertTrue(Character.isLetter(shortKey.charAt(0)));
    }

    @Test
    public void shortenUrlWithCustomKeyShouldReturnCustomKey() {
        String longUrl = "https://www.example.com";
        String customKey = "mykey";
        UrlEntry urlEntry = new UrlEntry(customKey, longUrl);

        when(urlEntryRepository.findByShortUrl(customKey)).thenReturn(null);
        when(urlEntryRepository.save(any(UrlEntry.class))).thenReturn(urlEntry);

        String shortKey = urlShortenerService.shortenUrl(longUrl, customKey);

        assertEquals(customKey, shortKey);
    }

    @Test
    public void shortenUrlWithExistingCustomKeyShouldThrowException() {
        String longUrl = "https://www.example.com";
        String customKey = "existingkey";
        UrlEntry existingUrlEntry = new UrlEntry(customKey, "https://www.oldurl.com");

        when(urlEntryRepository.findByShortUrl(customKey)).thenReturn(existingUrlEntry);

        assertThrows(IllegalArgumentException.class, () -> {
            urlShortenerService.shortenUrl(longUrl, customKey);
        });
    }

    @Test
    public void getLongUrlShouldReturnLongUrl() {
        String shortKey = "abc123";
        String longUrl = "https://www.google.com";
        UrlEntry urlEntry = new UrlEntry(shortKey, longUrl);

        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(urlEntry);

        String retrievedLongUrl = urlShortenerService.getLongUrl(shortKey);

        assertNotNull(retrievedLongUrl);
        assertEquals(longUrl, retrievedLongUrl);
    }

    @Test
    public void getLongUrlShouldReturnNullForInvalidKey() {
        String shortKey = "invalid";

        when(urlEntryRepository.findByShortUrl(shortKey)).thenReturn(null);

        String retrievedLongUrl = urlShortenerService.getLongUrl(shortKey);

        assertNull(retrievedLongUrl);
    }
}
