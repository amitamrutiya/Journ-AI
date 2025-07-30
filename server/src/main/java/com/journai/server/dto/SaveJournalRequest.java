package com.journai.server.dto;

import com.journai.server.model.Mood;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SaveJournalRequest {

    @NotBlank(message = "Text is required")
    @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
    private String text;

    private Mood mood;
    private String summary;
    private String reason;

    public SaveJournalRequest() {
    }

    public SaveJournalRequest(String text, Mood mood, String summary, String reason) {
        this.text = text;
        this.mood = mood;
        this.summary = summary;
        this.reason = reason;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
}
