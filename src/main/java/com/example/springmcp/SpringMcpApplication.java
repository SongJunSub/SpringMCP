package com.example.springmcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMcpApplication.class, args);
    }

}
