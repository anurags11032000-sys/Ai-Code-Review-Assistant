package com.aicodereviewassistant.integration.github;

import com.aicodereviewassistant.exception.ExternalServiceException;
import com.aicodereviewassistant.integration.github.GitHubModels.GitHubAuthenticatedUser;
import com.aicodereviewassistant.integration.github.GitHubModels.GitHubPullRequest;
import com.aicodereviewassistant.integration.github.GitHubModels.GitHubPullRequestFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GitHubApiClient {

    private final RestClient restClient;

    public GitHubApiClient(@Value("${integration.github.api-base-url:https://api.github.com}") String githubApiBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(githubApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public GitHubPullRequest getPullRequest(String owner, String repo, int prNumber, String token) {
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{prNumber}", owner, repo, prNumber)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(GitHubPullRequest.class);
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to fetch pull request from GitHub", ex);
        }
    }

    public GitHubAuthenticatedUser getAuthenticatedUser(String token) {
        try {
            return restClient.get()
                    .uri("/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(GitHubAuthenticatedUser.class);
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to validate GitHub credentials", ex);
        }
    }

    public List<GitHubPullRequestFile> getPullRequestFiles(String owner, String repo, int prNumber, String token) {
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{prNumber}/files", owner, repo, prNumber)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception ex) {
            throw new ExternalServiceException("Failed to fetch pull request files from GitHub", ex);
        }
    }
}
