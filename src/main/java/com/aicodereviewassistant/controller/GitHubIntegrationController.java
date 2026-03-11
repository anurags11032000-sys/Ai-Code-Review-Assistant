package com.aicodereviewassistant.controller;

import com.aicodereviewassistant.dto.CodeReviewResponse;
import com.aicodereviewassistant.dto.GitHubRepositoryConnectRequest;
import com.aicodereviewassistant.dto.GitHubRepositoryConnectResponse;
import com.aicodereviewassistant.dto.PullRequestAnalyzeRequest;
import com.aicodereviewassistant.service.GitHubReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/github")
public class GitHubIntegrationController {

    private final GitHubReviewService gitHubReviewService;

    public GitHubIntegrationController(GitHubReviewService gitHubReviewService) {
        this.gitHubReviewService = gitHubReviewService;
    }

    @PostMapping("/connect-repository")
    public GitHubRepositoryConnectResponse connectRepository(@Valid @RequestBody GitHubRepositoryConnectRequest request) {
        return gitHubReviewService.connectRepository(request);
    }

    @PostMapping("/analyze-pr")
    public CodeReviewResponse analyzePullRequest(@Valid @RequestBody PullRequestAnalyzeRequest request) {
        return gitHubReviewService.analyzePullRequest(request);
    }
}
