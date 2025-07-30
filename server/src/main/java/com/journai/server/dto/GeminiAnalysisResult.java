package com.journai.server.dto;

import com.journai.server.model.Mood;

public class GeminiAnalysisResult {

    private Mood mood;
    private String summary;
    private String reason;

    public GeminiAnalysisResult() {
    }

    public GeminiAnalysisResult(Mood mood, String summary, String reason) {
        this.mood = mood;
        this.summary = summary;
        this.reason = reason;
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
