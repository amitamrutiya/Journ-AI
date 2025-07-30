package com.journai.server.controller;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journai.server.dto.ApiResponse;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "JournAI API");
        data.put("status", "running");

        return ResponseEntity.ok(ApiResponse.success(data, "Welcome to the JournAI server!"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "healthy");
        data.put("timestamp", LocalDateTime.now());
        data.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        data.put("environment", System.getProperty("spring.profiles.active", "development"));

        return ResponseEntity.ok(ApiResponse.success(data, "Service is healthy"));
    }
}
