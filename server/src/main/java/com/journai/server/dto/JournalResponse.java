package com.journai.server.dto;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.journai.server.model.Journal;
import com.journai.server.model.Mood;

public class JournalResponse {

    private String id;
    private String title;
    private String content;
    private Mood mood;
    private String summary;
    private String reason;
    @JsonProperty("createdAt")
    private String createdAt;
    private int wordCount;

    public JournalResponse() {
    }

    public JournalResponse(String id, String title, String content, Mood mood, String summary, String reason,
            String createdAt, int wordCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.summary = summary;
        this.reason = reason;
        this.createdAt = createdAt;
        this.wordCount = wordCount;
    }

    // Static factory method to create from Journal entity
    public static JournalResponse fromJournal(Journal journal) {

        // Format createdAt to ISO string with Z suffix
        String createdAtString = journal.getCreatedAt() != null
                ? journal.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
                : "";

        // Calculate word count from content
        int wordCount = calculateWordCount(journal.getContent());

        return new JournalResponse(
                journal.getId(),
                journal.getTitle(),
                journal.getContent(),
                journal.getMood(),
                journal.getSummary() != null ? journal.getSummary() : "",
                "", // reason field - empty for now
                createdAtString,
                wordCount);
    }

    private static int calculateWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        // Remove HTML tags and count words
        String textOnly = content.replaceAll("<[^>]*>", " ");
        String[] words = textOnly.trim().split("\\s+");
        return words.length;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }
}
