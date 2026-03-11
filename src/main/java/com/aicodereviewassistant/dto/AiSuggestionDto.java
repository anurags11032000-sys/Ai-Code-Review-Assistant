package com.aicodereviewassistant.dto;

import com.aicodereviewassistant.entity.SuggestionType;

public record AiSuggestionDto(
        Long id,
        SuggestionType suggestionType,
        String severity,
        String filePath,
        Integer lineNumber,
        String title,
        String details,
        String suggestedFix
) {
}
