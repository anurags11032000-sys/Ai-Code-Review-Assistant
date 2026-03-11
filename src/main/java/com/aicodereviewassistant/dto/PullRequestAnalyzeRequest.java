package com.aicodereviewassistant.dto;

import jakarta.validation.constraints.NotNull;

public record PullRequestAnalyzeRequest(
        @NotNull Long repositoryId,
        @NotNull Integer pullRequestNumber,
        @NotNull Long userId
) {
}
