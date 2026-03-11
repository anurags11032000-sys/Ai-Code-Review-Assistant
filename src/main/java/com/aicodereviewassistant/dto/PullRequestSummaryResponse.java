package com.aicodereviewassistant.dto;

public record PullRequestSummaryResponse(
        Long reviewId,
        String pullRequestTitle,
        String summary,
        String riskAssessment,
        String mergeRecommendation
) {
}
