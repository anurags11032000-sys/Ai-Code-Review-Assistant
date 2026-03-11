package com.aicodereviewassistant.service.impl;

import com.aicodereviewassistant.dto.*;
import com.aicodereviewassistant.entity.AiSuggestion;
import com.aicodereviewassistant.entity.CodeReview;
import com.aicodereviewassistant.entity.ReviewStatus;
import com.aicodereviewassistant.entity.User;
import com.aicodereviewassistant.entity.UserRole;
import com.aicodereviewassistant.exception.BadRequestException;
import com.aicodereviewassistant.exception.NotFoundException;
import com.aicodereviewassistant.mapper.CodeReviewMapper;
import com.aicodereviewassistant.repository.AiSuggestionRepository;
import com.aicodereviewassistant.repository.CodeReviewRepository;
import com.aicodereviewassistant.repository.UserRepository;
import com.aicodereviewassistant.service.CodeReviewService;
import com.aicodereviewassistant.service.ai.AiCodeAnalysisService;
import com.aicodereviewassistant.service.ai.SuggestionPostProcessor;
import com.aicodereviewassistant.service.cache.ReviewCacheService;
import com.aicodereviewassistant.util.LanguageFileExtensionValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CodeReviewServiceImpl implements CodeReviewService {

    private final UserRepository userRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final AiSuggestionRepository aiSuggestionRepository;
    private final AiCodeAnalysisService aiCodeAnalysisService;
    private final SuggestionPostProcessor suggestionPostProcessor;
    private final CodeReviewMapper codeReviewMapper;
    private final ReviewCacheService reviewCacheService;

    public CodeReviewServiceImpl(
            UserRepository userRepository,
            CodeReviewRepository codeReviewRepository,
            AiSuggestionRepository aiSuggestionRepository,
            AiCodeAnalysisService aiCodeAnalysisService,
            SuggestionPostProcessor suggestionPostProcessor,
            CodeReviewMapper codeReviewMapper,
            ReviewCacheService reviewCacheService
    ) {
        this.userRepository = userRepository;
        this.codeReviewRepository = codeReviewRepository;
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.aiCodeAnalysisService = aiCodeAnalysisService;
        this.suggestionPostProcessor = suggestionPostProcessor;
        this.codeReviewMapper = codeReviewMapper;
        this.reviewCacheService = reviewCacheService;
    }

    @Override
    @Transactional
    public CodeReviewResponse analyzeUploadedCode(CodeSnippetUploadRequest request) {
        LanguageFileExtensionValidator.validateOrThrow(request.fileName(), request.language(), request.sourceCode());
        if (codeReviewRepository.existsBySourceNameIgnoreCaseAndSourceType(request.fileName(), "FILE_UPLOAD")) {
            throw new BadRequestException("File name already exists: " + request.fileName());
        }

        String normalizedEmail = request.userEmail() == null ? "" : request.userEmail().trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            throw new BadRequestException("Authenticated email is required");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User created = new User();
                    String localPart = normalizedEmail.split("@")[0];
                    created.setName(localPart.isBlank() ? "Developer" : localPart);
                    created.setEmail(normalizedEmail);
                    created.setRole(UserRole.DEVELOPER);
                    return userRepository.save(created);
                });

        CodeReview review = new CodeReview();
        review.setUser(user);
        review.setSourceName(request.fileName());
        review.setSourceType("FILE_UPLOAD");
        review.setStatus(ReviewStatus.PROCESSING);
        codeReviewRepository.save(review);

        List<AiSuggestion> suggestions = aiCodeAnalysisService.analyzeCodePatch(request.fileName(), request.sourceCode(), request.language());
        suggestions = suggestionPostProcessor.normalizeAndDedupe(suggestions);
        suggestions.forEach(suggestion -> suggestion.setReview(review));
        aiSuggestionRepository.saveAll(suggestions);

        review.setStatus(ReviewStatus.COMPLETED);
        review.setRiskScore(calculateRiskScore(suggestions));
        review.setOverallSummary(buildSummary(suggestions));
        codeReviewRepository.save(review);

        CodeReviewResponse response = codeReviewMapper.toResponse(review, suggestions);
        reviewCacheService.cacheReview(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CodeReviewResponse getReviewById(Long reviewId) {
        CodeReviewResponse cached = reviewCacheService.getCachedReview(reviewId);
        if (cached != null) {
            return cached;
        }

        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));
        List<AiSuggestion> suggestions = aiSuggestionRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);

        CodeReviewResponse response = codeReviewMapper.toResponse(review, suggestions);
        reviewCacheService.cacheReview(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CodeReviewResponse getLatestReviewByFileName(String fileName) {
        CodeReview review = codeReviewRepository.findTopBySourceNameIgnoreCaseOrderByCreatedAtDesc(fileName)
                .orElseThrow(() -> new NotFoundException("Review not found for file: " + fileName));
        return getReviewById(review.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CodeReviewResponse> listReviews(Long userId, int page, int size) {
        var result = codeReviewRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));

        List<CodeReviewResponse> content = result.getContent().stream()
                .map(review -> codeReviewMapper.toResponse(review, aiSuggestionRepository.findByReviewIdOrderByCreatedAtAsc(review.getId())))
                .toList();

        return new PageResponse<>(content, page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public CodeExplanationResponse explainCode(CodeExplainRequest request) {
        LanguageFileExtensionValidator.validateLanguageAndCodeOrThrow(request.language(), request.code());
        return aiCodeAnalysisService.explainCode(request.code(), request.language(), request.context());
    }

    @Override
    @Transactional(readOnly = true)
    public PullRequestSummaryResponse summarizePullRequest(Long reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found: " + reviewId));

        String summary = aiCodeAnalysisService.summarizePullRequest(
                review.getSourceName(),
                review.getOverallSummary() == null ? "No prior summary." : review.getOverallSummary()
        );

        return new PullRequestSummaryResponse(
                reviewId,
                review.getSourceName(),
                summary,
                "Risk score: " + review.getRiskScore(),
                review.getRiskScore() != null && review.getRiskScore() >= 70 ? "Do not merge until fixes" : "Can merge after standard checks"
        );
    }

    private int calculateRiskScore(List<AiSuggestion> suggestions) {
        int score = 0;
        for (AiSuggestion suggestion : suggestions) {
            String severity = suggestion.getSeverity() == null ? "LOW" : suggestion.getSeverity().toUpperCase();
            switch (severity) {
                case "CRITICAL" -> score += 30;
                case "HIGH" -> score += 20;
                case "MEDIUM" -> score += 10;
                default -> score += 5;
            }
        }
        return Math.min(score, 100);
    }

    private String buildSummary(List<AiSuggestion> suggestions) {
        long securityCount = suggestions.stream().filter(s -> s.getSuggestionType() != null && s.getSuggestionType().name().contains("SECURITY")).count();
        long bugCount = suggestions.stream().filter(s -> s.getSuggestionType() != null && s.getSuggestionType().name().contains("BUG")).count();
        return "Found " + suggestions.size() + " suggestions (bugs=" + bugCount + ", security=" + securityCount + ").";
    }
}
