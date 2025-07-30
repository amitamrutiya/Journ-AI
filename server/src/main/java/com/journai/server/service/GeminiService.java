package com.journai.server.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.journai.server.config.AppProperties;
import com.journai.server.dto.GeminiAnalysisResult;
import com.journai.server.model.Mood;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiAnalysisResult analyzeJournal(String journalText) {
        try {
            String apiKey = appProperties.getGemini().getApiKey();
            String model = appProperties.getGemini().getModel();

            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("Gemini API key not configured");
            }

            logger.info("Sending journal analysis request to Gemini, text length: {}, model: {}",
                    journalText.length(), model);

            String prompt = createAnalysisPrompt(journalText);
            Map<String, Object> requestBody = createRequestBody(prompt);

            String response = webClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key="
                            + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("Received response from Gemini, response length: {}",
                    response != null ? response.length() : 0);

            return parseGeminiResponse(response);

        } catch (Exception e) {
            logger.error("Gemini analysis failed", e);

            // Return default analysis
            return new GeminiAnalysisResult(
                    Mood.NEUTRAL,
                    "Please share your journal thoughts and experiences for analysis",
                    "Unable to analyze the provided content. Please write about your day, feelings, or experiences.");
        }
    }

    private String createAnalysisPrompt(String journalText) {
        return """
                You are an expert emotional intelligence AI assistant. Analyze the following journal entry and return insights in a strictly formatted JSON.

                JOURNAL ENTRY:
                %s

                TASK:
                1. Determine the primary mood or emotion expressed in the text.
                2. Generate a one-line summary of the user's day or emotional state.
                3. Provide a brief reason explaining why this mood was identified.

                RESPONSE FORMAT:
                Respond ONLY with a valid JSON object in this exact format:
                {
                  "mood": "[one of the following: happy, sad, anxious, excited, angry, peaceful, grateful, frustrated, worried, content, neutral, tired]",
                  "summary": "[one-line summary of the day/experience in 15–30 words]",
                  "reason": "[brief explanation citing specific phrases or emotional indicators from the journal entry]"
                }

                IMPORTANT INSTRUCTIONS:
                - The mood **must be one of these EXACT values**: happy, sad, anxious, excited, angry, peaceful, grateful, frustrated, worried, content, neutral, tired.
                - Do not invent or choose any mood word outside of this list.
                - Only include the JSON object in your response.
                - The summary should be concise and reflect the emotional tone of the entry.
                - The reason must clearly reference emotional signals or language from the journal.
                - Use "neutral" for entries that don't express strong emotions or are matter-of-fact.
                - Use "tired" for entries expressing physical or mental exhaustion, fatigue, or feeling drained.

                Respond with **only the JSON**, and nothing else.
                                """
                .formatted(journalText);
    }

    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> content = new HashMap<>();
        Map<String, String> text = new HashMap<>();
        text.put("text", prompt);
        content.put("parts", new Object[] { text });

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Object[] { content });

        return requestBody;
    }

    private GeminiAnalysisResult parseGeminiResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidatesNode = rootNode.path("candidates");

            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");

                if (partsNode.isArray() && partsNode.size() > 0) {
                    String textContent = partsNode.get(0).path("text").asText();

                    // Try to parse as JSON
                    try {
                        JsonNode analysisNode = objectMapper.readTree(textContent);
                        return new GeminiAnalysisResult(
                                Mood.valueOf(analysisNode.path("mood").asText("NEUTRAL").toUpperCase()),
                                analysisNode.path("summary").asText(""),
                                analysisNode.path("reason").asText(""));
                    } catch (Exception e) {
                        // If JSON parsing fails, extract values manually
                        return extractAnalysisFromText(textContent);
                    }
                }
            }

            // Fallback
            return createDefaultAnalysis();

        } catch (Exception e) {
            logger.error("Failed to parse Gemini response", e);
            return createDefaultAnalysis();
        }
    }

    private GeminiAnalysisResult extractAnalysisFromText(String text) {
        Mood mood = Mood.NEUTRAL;
        String summary = "";
        String reason = "";

        // Simple extraction logic
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.toLowerCase().contains("mood")) {
                mood = Mood.valueOf(extractValue(line).toUpperCase());
            } else if (line.toLowerCase().contains("summary")) {
                summary = extractValue(line);
            } else if (line.toLowerCase().contains("reason")) {
                reason = extractValue(line);
            }
        }

        return new GeminiAnalysisResult(mood, summary, reason);
    }

    private String extractValue(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex != -1) {
            String value = line.substring(colonIndex + 1).trim();
            // Remove quotes, commas, and other unwanted characters
            value = value.replaceAll("^[\"']*", "").replaceAll("[\"',]*$", "");
            return value.trim();
        }
        return "";
    }

    private GeminiAnalysisResult createDefaultAnalysis() {
        return new GeminiAnalysisResult(
                Mood.NEUTRAL,
                "Please share your journal thoughts and experiences for analysis",
                "Unable to analyze the provided content. Please write about your day, feelings, or experiences.");
    }
}
