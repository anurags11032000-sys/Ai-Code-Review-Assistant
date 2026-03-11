package com.aicodereviewassistant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GitHubRepositoryConnectRequest(
        @NotBlank String repoUrl,
        @NotBlank String defaultBranch,
        @NotBlank String githubUsername,
        @NotBlank String githubToken,
        @Email String userEmail
) {
}
