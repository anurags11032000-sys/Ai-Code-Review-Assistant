package com.aicodereviewassistant.dto;

import com.aicodereviewassistant.entity.ReviewStatus;

import java.time.Instant;
import java.util.List;

public record CodeReviewResponse(
        Long reviewId,
        String sourceName,
        String sourceType,
        ReviewStatus status,
        Integer riskScore,
        String overallSummary,
        Instant createdAt,
        List<AiSuggestionDto> suggestions
) {
}
