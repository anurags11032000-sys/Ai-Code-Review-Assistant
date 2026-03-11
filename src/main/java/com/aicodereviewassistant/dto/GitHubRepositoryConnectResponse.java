package com.aicodereviewassistant.dto;

public record GitHubRepositoryConnectResponse(
        Long repositoryId,
        Long userId,
        String userEmail,
        String githubUsername
) {
}
