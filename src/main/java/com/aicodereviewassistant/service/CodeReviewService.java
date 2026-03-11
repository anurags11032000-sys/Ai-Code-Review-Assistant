package com.aicodereviewassistant.service;

import com.aicodereviewassistant.dto.*;

public interface CodeReviewService {
    CodeReviewResponse analyzeUploadedCode(CodeSnippetUploadRequest request);

    CodeReviewResponse getReviewById(Long reviewId);
    CodeReviewResponse getLatestReviewByFileName(String fileName);

    PageResponse<CodeReviewResponse> listReviews(Long userId, int page, int size);

    CodeExplanationResponse explainCode(CodeExplainRequest request);

    PullRequestSummaryResponse summarizePullRequest(Long reviewId);
}
