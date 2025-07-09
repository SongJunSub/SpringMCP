package com.example.springmcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = SpringMcpApplication.class)
class SpringMcpApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }

}
