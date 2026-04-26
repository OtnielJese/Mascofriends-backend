package com.vetivet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Endpoint
 * 
 * Used for external monitoring services like Better Stack, UptimeRobot, etc.
 * This endpoint:
 * - Requires NO authentication
 * - Returns HTTP 200 with {"status":"UP"} on success
 * - Has minimal latency and overhead
 * - Does NOT validate database connection by default (fast)
 * 
 * URL: /api/health
 * Method: GET
 * Auth: None (permitAll)
 */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    /**
     * Basic health check - responds immediately
     * Perfect for load balancers and monitoring services
     * 
     * Response: {"status":"UP"} with HTTP 200
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check (optional)
     * Can include timestamp and version info
     * Remove this method if not needed
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> healthDetailed() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "vetivet-backend");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
