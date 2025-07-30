package com.journai.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalyzeJournalRequest {

    @NotBlank(message = "Text is required")
    @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
    private String text;

    public AnalyzeJournalRequest() {
    }

    public AnalyzeJournalRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
