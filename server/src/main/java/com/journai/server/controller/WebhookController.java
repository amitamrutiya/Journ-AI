package com.journai.server.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.journai.server.dto.ApiResponse;
import com.journai.server.service.UserService;

@RestController
@RequestMapping("/api")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/webhooks")
    public ResponseEntity<ApiResponse<String>> handleClerkWebhook(@RequestBody Map<String, Object> payload) {
        try {
            logger.info("Received Clerk webhook: {}", payload.get("type"));

            // Handle different webhook events
            String eventType = (String) payload.get("type");

            switch (eventType) {
                case "user.created":
                    handleUserCreated(payload);
                    break;
                case "user.updated":
                    handleUserUpdated(payload);
                    break;
                case "user.deleted":
                    handleUserDeleted(payload);
                    break;
                default:
                    logger.info("Unhandled webhook event type: {}", eventType);
                    break;
            }

            return ResponseEntity.ok(ApiResponse.success("success", "Webhook processed successfully"));

        } catch (Exception e) {
            logger.error("Error processing Clerk webhook", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to process webhook", e.getMessage()));
        }
    }

    private void handleUserCreated(Map<String, Object> payload) {
        logger.info("Processing user.created webhook");

        try {
            // Extract user data from Clerk webhook payload
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) payload.get("data");

            String id = (String) userData.get("id");

            // Extract email from email_addresses array
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> emailAddresses = (java.util.List<Map<String, Object>>) userData
                    .get("email_addresses");
            String email = null;
            if (emailAddresses != null && !emailAddresses.isEmpty()) {
                email = (String) emailAddresses.get(0).get("email_address");
            }

            String firstName = (String) userData.get("first_name");
            String lastName = (String) userData.get("last_name");
            String name = buildFullName(firstName, lastName);
            String imageUrl = (String) userData.get("image_url");

            if (id != null && email != null) {
                userService.createUser(id, email, name, imageUrl);
                logger.info("Successfully created user: {} with email: {}", id, email);
            } else {
                logger.error("Missing required user data: id={}, email={}", id, email);
            }

        } catch (Exception e) {
            logger.error("Error creating user from webhook", e);
            throw e;
        }
    }

    private void handleUserUpdated(Map<String, Object> payload) {
        logger.info("Processing user.updated webhook");

        try {
            // Extract user data from Clerk webhook payload
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) payload.get("data");

            String id = (String) userData.get("id");

            // Extract email from email_addresses array
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> emailAddresses = (java.util.List<Map<String, Object>>) userData
                    .get("email_addresses");
            String email = null;
            if (emailAddresses != null && !emailAddresses.isEmpty()) {
                email = (String) emailAddresses.get(0).get("email_address");
            }

            String firstName = (String) userData.get("first_name");
            String lastName = (String) userData.get("last_name");
            String name = buildFullName(firstName, lastName);
            String imageUrl = (String) userData.get("image_url");

            if (id != null) {
                userService.updateUser(id, email, name, imageUrl);
                logger.info("Successfully updated user: {} with email: {}", id, email);
            } else {
                logger.error("Missing user id in update webhook");
            }

        } catch (Exception e) {
            logger.error("Error updating user from webhook", e);
            throw e;
        }
    }

    private void handleUserDeleted(Map<String, Object> payload) {
        logger.info("Processing user.deleted webhook");

        try {
            // Extract user data from Clerk webhook payload
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) payload.get("data");

            String id = (String) userData.get("id");

            if (id != null) {
                userService.deleteUser(id);
                logger.info("Successfully deleted user: {}", id);
            } else {
                logger.error("Missing user id in delete webhook");
            }

        } catch (Exception e) {
            logger.error("Error deleting user from webhook", e);
            throw e;
        }
    }

    private String buildFullName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return null;
    }
}
