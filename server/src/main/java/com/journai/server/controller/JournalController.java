package com.journai.server.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.journai.server.dto.AnalyzeJournalRequest;
import com.journai.server.dto.ApiResponse;
import com.journai.server.dto.GeminiAnalysisResult;
import com.journai.server.dto.JournalResponse;
import com.journai.server.dto.SaveJournalRequest;
import com.journai.server.model.Journal;
import com.journai.server.model.Mood;
import com.journai.server.service.GeminiService;
import com.journai.server.service.JournalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class JournalController {

        private static final Logger logger = LoggerFactory.getLogger(JournalController.class);

        @Autowired
        private GeminiService geminiService;

        @Autowired
        private JournalService journalService;

        @PostMapping("/analyze-journal")
        public ResponseEntity<ApiResponse<GeminiAnalysisResult>> analyzeJournal(
                        @Valid @RequestBody AnalyzeJournalRequest request,
                        Authentication authentication) {

                String userId = authentication != null && authentication.getPrincipal() != null
                                ? (String) authentication.getPrincipal()
                                : "anonymous";

                try {
                        String trimmedText = request.getText().trim();

                        logger.info("Journal analysis request received, userId: {}, textLength: {}",
                                        userId, trimmedText.length());

                        GeminiAnalysisResult analysis = geminiService.analyzeJournal(trimmedText);

                        logger.info("Gemini analysis completed successfully, userId: {}, mood: {}, textLength: {}",
                                        userId, analysis.getMood(), trimmedText.length());

                        return ResponseEntity
                                        .ok(ApiResponse.success(analysis, "Journal analysis completed successfully"));

                } catch (Exception e) {
                        logger.error("Error processing journal analysis for userId: {}", userId, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to analyze journal", e.getMessage()));
                }
        }

        @PostMapping("/save-journal")
        public ResponseEntity<ApiResponse<JournalResponse>> saveJournal(
                        @Valid @RequestBody SaveJournalRequest request,
                        Authentication authentication) {

                if (authentication == null || authentication.getPrincipal() == null) {
                        logger.warn("Unauthorized access attempt to save journal");
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("User authentication required", "Unauthorized"));
                }

                String userId = (String) authentication.getPrincipal();

                try {
                        Mood moodValue = request.getMood() != null ? request.getMood() : Mood.NEUTRAL;
                        String summaryValue = request.getSummary() != null ? request.getSummary() : "";
                        String reasonValue = request.getReason() != null ? request.getReason() : "";

                        logger.info("Journal save request received, userId: {}, textLength: {}, mood: {}, hasAnalysis: {}",
                                        userId, request.getText().trim().length(), moodValue,
                                        !moodValue.equals(Mood.NEUTRAL) && !summaryValue.isEmpty()
                                                        && !reasonValue.isEmpty());

                        JournalService.JournalData journalData = new JournalService.JournalData(
                                        userId, request.getText().trim(), moodValue, summaryValue, reasonValue);

                        Journal savedJournal = journalService.saveJournal(journalData);

                        JournalResponse response = JournalResponse.fromJournal(savedJournal);

                        logger.info("Journal saved successfully, userId: {}, journalId: {}, mood: {}",
                                        userId, savedJournal.getId(), savedJournal.getMood());

                        return ResponseEntity.ok(ApiResponse.success(response, "Journal saved successfully"));

                } catch (Exception e) {
                        logger.error("Error saving journal for userId: {}", userId, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to save journal", e.getMessage()));
                }
        }

        @PutMapping("/update-journal/{id}")
        public ResponseEntity<ApiResponse<JournalResponse>> updateJournal(
                        @PathVariable String id,
                        @Valid @RequestBody SaveJournalRequest request,
                        Authentication authentication) {

                if (authentication == null || authentication.getPrincipal() == null) {
                        logger.warn("Unauthorized access attempt to update journal");
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("User authentication required", "Unauthorized"));
                }

                String userId = (String) authentication.getPrincipal();

                try {
                        Mood moodValue = request.getMood() != null ? request.getMood() : Mood.NEUTRAL;
                        String summaryValue = request.getSummary() != null ? request.getSummary() : "";
                        String reasonValue = request.getReason() != null ? request.getReason() : "";

                        logger.info("Journal update request received, userId: {}, journalId: {}, textLength: {}, mood: {}",
                                        userId, id, request.getText().trim().length(), moodValue);

                        JournalService.JournalData journalData = new JournalService.JournalData(
                                        userId, request.getText().trim(), moodValue, summaryValue, reasonValue);

                        Journal updatedJournal = journalService.updateJournal(id, userId, journalData);

                        if (updatedJournal == null) {
                                logger.warn("Journal not found or access denied for update, userId: {}, journalId: {}",
                                                userId, id);
                                return ResponseEntity.status(404)
                                                .body(ApiResponse.error("Journal not found", "Not found"));
                        }

                        JournalResponse response = JournalResponse.fromJournal(updatedJournal);

                        logger.info("Journal updated successfully, userId: {}, journalId: {}, mood: {}",
                                        userId, id, updatedJournal.getMood());

                        return ResponseEntity.ok(ApiResponse.success(response, "Journal updated successfully"));

                } catch (Exception e) {
                        logger.error("Error updating journal, userId: {}, journalId: {}", userId, id, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to update journal", e.getMessage()));
                }
        }

        @GetMapping("/get-user-journal")
        public ResponseEntity<ApiResponse<List<JournalResponse>>> getUserJournals(
                        @RequestParam(required = false) String month,
                        Authentication authentication) {

                if (authentication == null || authentication.getPrincipal() == null) {
                        logger.warn("Unauthorized access attempt to get user journal");
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("User authentication required", "Unauthorized"));
                }

                String userId = (String) authentication.getPrincipal();

                try {
                        logger.info("Fetching user journal entries, userId: {}, selectedMonth: {}",
                                        userId, month != null ? month : "all");

                        List<Journal> journals = journalService.getUserJournals(userId, 31, 0, month);

                        // Convert to DTOs to avoid lazy loading issues
                        List<JournalResponse> journalResponses = journals.stream()
                                        .map(JournalResponse::fromJournal)
                                        .toList();

                        logger.info("Retrieved {} journal entries for user: {}{}",
                                        journalResponses.size(), userId, month != null ? " for month: " + month : "");

                        return ResponseEntity.ok(ApiResponse.success(journalResponses,
                                        "User journal entries retrieved successfully"));

                } catch (Exception e) {
                        logger.error("Error fetching user journal for userId: {}", userId, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to fetch user journal", e.getMessage()));
                }
        }

        @GetMapping("/journal/{id}")
        public ResponseEntity<ApiResponse<Journal>> getJournalById(
                        @PathVariable String id,
                        Authentication authentication) {

                if (authentication == null || authentication.getPrincipal() == null) {
                        logger.warn("Unauthorized access attempt to get journal by ID");
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("User authentication required", "Unauthorized"));
                }

                String userId = (String) authentication.getPrincipal();

                try {
                        logger.info("Fetching journal by ID, userId: {}, journalId: {}", userId, id);

                        Journal journal = journalService.getJournalById(id, userId);

                        if (journal == null) {
                                logger.warn("Journal not found or access denied, userId: {}, journalId: {}", userId,
                                                id);
                                return ResponseEntity.status(404)
                                                .body(ApiResponse.error("Journal not found", "Not found"));
                        }

                        logger.info("Journal retrieved successfully, userId: {}, journalId: {}", userId, id);
                        return ResponseEntity.ok(ApiResponse.success(journal, "Journal retrieved successfully"));

                } catch (Exception e) {
                        logger.error("Error fetching journal by ID, userId: {}, journalId: {}", userId, id, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to fetch journal", e.getMessage()));
                }
        }

        @DeleteMapping("/delete-journal/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteJournal(
                        @PathVariable String id,
                        Authentication authentication) {

                if (authentication == null || authentication.getPrincipal() == null) {
                        logger.warn("Unauthorized access attempt to delete journal");
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("User authentication required", "Unauthorized"));
                }

                String userId = (String) authentication.getPrincipal();

                try {
                        logger.info("Delete journal request received, userId: {}, journalId: {}", userId, id);

                        journalService.deleteJournal(id, userId);

                        logger.info("Journal deleted successfully, userId: {}, journalId: {}", userId, id);
                        return ResponseEntity.ok(ApiResponse.success(null, "Journal deleted successfully"));

                } catch (Exception e) {
                        logger.error("Error deleting journal, userId: {}, journalId: {}", userId, id, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to delete journal", e.getMessage()));
                }
        }

        @GetMapping("/journals/insights")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getJournalInsights(
                        @RequestParam(defaultValue = "month") String range,
                        @RequestParam(required = false) String mood,
                        Authentication authentication) {

                if (authentication == null || authentication.getPrincipal() == null) {
                        logger.warn("Unauthorized access attempt to get journal insights");
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("User authentication required", "Unauthorized"));
                }

                String userId = (String) authentication.getPrincipal();
                Mood newMood = null;
                if (mood != null) {
                        newMood = journalService.mapMoodToEnum(mood);
                }

                try {
                        logger.info("Fetching journal insights, userId: {}, timeRange: {}, moodFilter: {}",
                                        userId, range, newMood != null ? newMood.name() : "none");

                        Map<String, Object> insights = journalService.getJournalInsights(userId, range, newMood);

                        logger.info("Journal insights retrieved successfully, userId: {}, totalEntries: {}, timeRange: {}",
                                        userId, insights.get("totalEntries"), range);

                        return ResponseEntity
                                        .ok(ApiResponse.success(insights, "Journal insights retrieved successfully"));

                } catch (Exception e) {
                        logger.error("Error fetching journal insights for userId: {}", userId, e);
                        return ResponseEntity.status(500)
                                        .body(ApiResponse.error("Failed to fetch journal insights", e.getMessage()));
                }
        }
}
