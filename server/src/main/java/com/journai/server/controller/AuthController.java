package com.journai.server.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journai.server.dto.ApiResponse;
import com.journai.server.model.User;
import com.journai.server.security.ClerkJwtService;
import com.journai.server.service.JournalService;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private ClerkJwtService clerkJwtService;

    @Autowired
    private JournalService journalService;

    @GetMapping("/protected")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProtectedUserData(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            logger.warn("Unauthorized access attempt");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User authentication required", "Unauthorized"));
        }

        String userId = (String) authentication.getPrincipal();

        try {
            Map<String, Object> user = clerkJwtService.getClerkUserInfo(userId);
            logger.info("User data retrieved successfully for userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(Map.of("user", user), "User data retrieved successfully"));
        } catch (Exception e) {
            logger.error("Failed to retrieve user data for userId: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve user data", e.getMessage()));
        }
    }

    @GetMapping("/api/user-journals")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserJournals(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            logger.warn("Unauthorized access attempt to get user journals");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User authentication required", "Unauthorized"));
        }

        String userId = (String) authentication.getPrincipal();

        try {
            User user = journalService.getUserWithJournalIds(userId);
            long journalCount = journalService.getUserJournalCount(userId);

            logger.info("User data retrieved successfully for userId: {}, journalCount: {}",
                    userId, journalCount);

            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("user", user, "journalCount", journalCount),
                    "User data retrieved successfully"));
        } catch (Exception e) {
            logger.error("Failed to retrieve user data with journal IDs for userId: {}", userId, e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to retrieve user data", e.getMessage()));
        }
    }
}
