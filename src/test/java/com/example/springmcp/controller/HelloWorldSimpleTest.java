package com.example.springmcp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(HelloWorldController.class)
public class HelloWorldSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void helloShouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/hello").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }
}
