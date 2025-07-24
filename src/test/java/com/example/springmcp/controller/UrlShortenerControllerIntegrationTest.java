
package com.example.springmcp.controller;

import com.example.springmcp.AbstractIntegrationTest;
import com.example.springmcp.SpringMcpApplication;
import com.example.springmcp.dto.UrlShortenerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = SpringMcpApplication.class)
public class UrlShortenerControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shortenAndRedirectUrl() throws Exception {
        String longUrl = "https://www.example.com";
        UrlShortenerRequest request = new UrlShortenerRequest();
        request.setLongUrl(longUrl);

        // Shorten URL
        String responseContent = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String shortKey = objectMapper.readTree(responseContent).get("shortKey").asText();

        // Redirect to original URL
        mockMvc.perform(get("/api/shorten/" + shortKey).with(jwt()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(longUrl));
    }

    @Test
    public void shortenUrlWithCustomKey() throws Exception {
        String longUrl = "https://www.custom.com";
        String customKey = "mycustomkey";
        UrlShortenerRequest request = new UrlShortenerRequest();
        request.setLongUrl(longUrl);
        request.setCustomKey(customKey);

        // Shorten URL with custom key
        String responseContent = mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String returnedCustomKey = objectMapper.readTree(responseContent).get("shortKey").asText();
        assertEquals(customKey, returnedCustomKey);

        // Redirect to original URL
        mockMvc.perform(get("/api/shorten/" + customKey).with(jwt()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(longUrl));
    }

    @Test
    public void shortenUrlWithDuplicateCustomKeyShouldReturnConflict() throws Exception {
        String longUrl = "https://www.duplicate.com";
        String customKey = "duplicatekey";
        UrlShortenerRequest request = new UrlShortenerRequest();
        request.setLongUrl(longUrl);
        request.setCustomKey(customKey);

        // First attempt to shorten
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isCreated());

        // Second attempt with same custom key
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isConflict());
    }

    @Test
    public void redirectToNonExistentUrlShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/shorten/nonexistent").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shortenUrlWithInvalidLongUrlShouldReturnBadRequest() throws Exception {
        String invalidUrl = "invalid-url";
        UrlShortenerRequest request = new UrlShortenerRequest();
        request.setLongUrl(invalidUrl);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shortenUrlWithInvalidCustomKeyShouldReturnBadRequest() throws Exception {
        String longUrl = "https://www.example.com";
        String invalidCustomKey = "123456"; // Starts with number, not letter
        UrlShortenerRequest request = new UrlShortenerRequest();
        request.setLongUrl(longUrl);
        request.setCustomKey(invalidCustomKey);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.customKey").exists()); // Assuming validation error message is in customKey field
    }

    @Test
    public void shortenUrlResponseShouldContainCorrectData() throws Exception {
        String longUrl = "https://www.responsecheck.com";
        UrlShortenerRequest request = new UrlShortenerRequest();
        request.setLongUrl(longUrl);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortKey").exists())
                .andExpect(jsonPath("$.longUrl").value(longUrl))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }
}
