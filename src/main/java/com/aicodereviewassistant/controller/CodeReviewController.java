package com.aicodereviewassistant.controller;

import com.aicodereviewassistant.dto.*;
import com.aicodereviewassistant.service.CodeReviewService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/reviews")
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    public CodeReviewController(CodeReviewService codeReviewService) {
        this.codeReviewService = codeReviewService;
    }

    @PostMapping("/upload")
    public CodeReviewResponse uploadAndAnalyze(@Valid @RequestBody CodeSnippetUploadRequest request) {
        return codeReviewService.analyzeUploadedCode(request);
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CodeReviewResponse uploadFileAndAnalyze(
            @RequestParam MultipartFile file,
            @RequestParam String language,
            @RequestParam String userEmail
    ) throws IOException {
        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
        CodeSnippetUploadRequest request = new CodeSnippetUploadRequest(
                file.getOriginalFilename() == null ? "uploaded-file" : file.getOriginalFilename(),
                language,
                sourceCode,
                userEmail
        );
        return codeReviewService.analyzeUploadedCode(request);
    }

    @GetMapping("/{reviewId}")
    public CodeReviewResponse getById(@PathVariable Long reviewId) {
        return codeReviewService.getReviewById(reviewId);
    }

    @GetMapping("/search-by-file")
    public CodeReviewResponse getLatestByFileName(@RequestParam String fileName) {
        return codeReviewService.getLatestReviewByFileName(fileName);
    }

    @GetMapping
    public PageResponse<CodeReviewResponse> listPreviousReviews(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return codeReviewService.listReviews(userId, page, size);
    }

    @PostMapping("/explain")
    public CodeExplanationResponse explainCode(@Valid @RequestBody CodeExplainRequest request) {
        return codeReviewService.explainCode(request);
    }

    @GetMapping("/{reviewId}/summary")
    public PullRequestSummaryResponse summarize(@PathVariable Long reviewId) {
        return codeReviewService.summarizePullRequest(reviewId);
    }
}
