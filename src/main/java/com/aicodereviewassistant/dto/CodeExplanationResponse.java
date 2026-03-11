package com.aicodereviewassistant.dto;

public record CodeExplanationResponse(
        String explanation,
        String complexityNotes,
        String refactoringHint
) {
}
