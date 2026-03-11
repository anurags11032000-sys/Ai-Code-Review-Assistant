package com.aicodereviewassistant.service;

import com.aicodereviewassistant.dto.CodeReviewResponse;
import com.aicodereviewassistant.dto.GitHubRepositoryConnectRequest;
import com.aicodereviewassistant.dto.GitHubRepositoryConnectResponse;
import com.aicodereviewassistant.dto.PullRequestAnalyzeRequest;

public interface GitHubReviewService {
    GitHubRepositoryConnectResponse connectRepository(GitHubRepositoryConnectRequest request);

    CodeReviewResponse analyzePullRequest(PullRequestAnalyzeRequest request);
}
