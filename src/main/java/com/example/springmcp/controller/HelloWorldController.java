
package com.example.springmcp.controller;

import io.swagger.v3.oas.annotations.Operation;import io.swagger.v3.oas.annotations.tags.Tag;import org.springframework.web.bind.annotation.GetMapping;import org.springframework.web.bind.annotation.RestController;@RestController@Tag(name = "Hello World API", description = "Simple Hello World endpoint")public class HelloWorldController {    @Operation(summary = "Returns a greeting message", description = "Returns \"Hello, World!\"")    @GetMapping("/hello")    public String hello() {        return "Hello, World!";    }}
