package com.aicodereviewassistant.service.impl;

import com.aicodereviewassistant.dto.CodeReviewResponse;
import com.aicodereviewassistant.dto.GitHubRepositoryConnectRequest;
import com.aicodereviewassistant.dto.GitHubRepositoryConnectResponse;
import com.aicodereviewassistant.dto.PullRequestAnalyzeRequest;
import com.aicodereviewassistant.entity.*;
import com.aicodereviewassistant.exception.BadRequestException;
import com.aicodereviewassistant.exception.NotFoundException;
import com.aicodereviewassistant.integration.github.GitHubApiClient;
import com.aicodereviewassistant.integration.github.GitHubModels.GitHubAuthenticatedUser;
import com.aicodereviewassistant.integration.github.GitHubModels.GitHubPullRequest;
import com.aicodereviewassistant.integration.github.GitHubModels.GitHubPullRequestFile;
import com.aicodereviewassistant.mapper.CodeReviewMapper;
import com.aicodereviewassistant.repository.*;
import com.aicodereviewassistant.service.GitHubReviewService;
import com.aicodereviewassistant.service.ai.AiCodeAnalysisService;
import com.aicodereviewassistant.service.ai.SuggestionPostProcessor;
import com.aicodereviewassistant.util.GitHubUrlParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GitHubReviewServiceImpl implements GitHubReviewService {

    private final UserRepository userRepository;
    private final SourceRepositoryRepository sourceRepositoryRepository;
    private final PullRequestReviewTargetRepository pullRequestRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final AiSuggestionRepository aiSuggestionRepository;
    private final CodeReviewMapper codeReviewMapper;
    private final AiCodeAnalysisService aiCodeAnalysisService;
    private final SuggestionPostProcessor suggestionPostProcessor;
    private final GitHubApiClient gitHubApiClient;
    private final GitHubUrlParser gitHubUrlParser;

    public GitHubReviewServiceImpl(
            UserRepository userRepository,
            SourceRepositoryRepository sourceRepositoryRepository,
            PullRequestReviewTargetRepository pullRequestRepository,
            CodeReviewRepository codeReviewRepository,
            AiSuggestionRepository aiSuggestionRepository,
            CodeReviewMapper codeReviewMapper,
            AiCodeAnalysisService aiCodeAnalysisService,
            SuggestionPostProcessor suggestionPostProcessor,
            GitHubApiClient gitHubApiClient,
            GitHubUrlParser gitHubUrlParser
    ) {
        this.userRepository = userRepository;
        this.sourceRepositoryRepository = sourceRepositoryRepository;
        this.pullRequestRepository = pullRequestRepository;
        this.codeReviewRepository = codeReviewRepository;
        this.aiSuggestionRepository = aiSuggestionRepository;
        this.codeReviewMapper = codeReviewMapper;
        this.aiCodeAnalysisService = aiCodeAnalysisService;
        this.suggestionPostProcessor = suggestionPostProcessor;
        this.gitHubApiClient = gitHubApiClient;
        this.gitHubUrlParser = gitHubUrlParser;
    }

    @Override
    @Transactional
    public GitHubRepositoryConnectResponse connectRepository(GitHubRepositoryConnectRequest request) {
        GitHubAuthenticatedUser authenticatedUser = gitHubApiClient.getAuthenticatedUser(request.githubToken());
        validateGitHubIdentity(request.githubUsername(), authenticatedUser.login());
        User user = resolveOrCreateUser(request, authenticatedUser);

        SourceRepository repository = sourceRepositoryRepository.findByRepoUrl(request.repoUrl())
                .orElseGet(SourceRepository::new);

        repository.setRepoUrl(request.repoUrl());
        repository.setProvider("GITHUB");
        repository.setDefaultBranch(request.defaultBranch());
        repository.setOwner(user);

        GitHubUrlParser.RepoCoordinates coordinates = gitHubUrlParser.parse(request.repoUrl());
        repository.setName(coordinates.repo());

        SourceRepository saved = sourceRepositoryRepository.save(repository);
        return new GitHubRepositoryConnectResponse(saved.getId(), user.getId(), user.getEmail(), user.getGithubUsername());
    }

    @Override
    @Transactional
    public CodeReviewResponse analyzePullRequest(PullRequestAnalyzeRequest request) {
        SourceRepository repository = sourceRepositoryRepository.findById(request.repositoryId())
                .orElseThrow(() -> new NotFoundException("Repository not found: " + request.repositoryId()));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.userId()));

        GitHubUrlParser.RepoCoordinates coordinates = gitHubUrlParser.parse(repository.getRepoUrl());
        String token = user.getGithubAccessToken();

        GitHubPullRequest pr = gitHubApiClient.getPullRequest(coordinates.owner(), coordinates.repo(), request.pullRequestNumber(), token);
        List<GitHubPullRequestFile> files = gitHubApiClient.getPullRequestFiles(coordinates.owner(), coordinates.repo(), request.pullRequestNumber(), token);

        PullRequestReviewTarget pullRequest = pullRequestRepository
                .findByRepositoryIdAndPrNumber(repository.getId(), request.pullRequestNumber())
                .orElseGet(PullRequestReviewTarget::new);

        pullRequest.setRepository(repository);
        pullRequest.setPrNumber(request.pullRequestNumber());
        pullRequest.setTitle(pr.title());
        pullRequest.setAuthor(pr.user() != null ? pr.user().login() : "unknown");
        pullRequest.setState(pr.state());
        pullRequest.setBaseBranch(pr.base() != null ? pr.base().ref() : repository.getDefaultBranch());
        pullRequest.setHeadBranch(pr.head() != null ? pr.head().ref() : "unknown");
        pullRequestRepository.save(pullRequest);

        String combinedPatch = files.stream()
                .map(file -> "FILE: " + file.filename() + "\nPATCH:\n" + (file.patch() == null ? "No patch" : file.patch()))
                .reduce("", (a, b) -> a + "\n\n" + b);

        CodeReview review = new CodeReview();
        review.setUser(user);
        review.setRepository(repository);
        review.setPullRequest(pullRequest);
        review.setSourceName("PR #" + pr.number() + " - " + pr.title());
        review.setSourceType("GITHUB_PULL_REQUEST");
        review.setStatus(ReviewStatus.PROCESSING);
        codeReviewRepository.save(review);

        List<AiSuggestion> suggestions = aiCodeAnalysisService.analyzeCodePatch(review.getSourceName(), combinedPatch, "multi-file");
        suggestions = suggestionPostProcessor.normalizeAndDedupe(suggestions);
        suggestions.forEach(suggestion -> suggestion.setReview(review));
        aiSuggestionRepository.saveAll(suggestions);

        review.setStatus(ReviewStatus.COMPLETED);
        review.setRiskScore(Math.min(100, suggestions.size() * 8));
        review.setOverallSummary(aiCodeAnalysisService.summarizePullRequest(pr.title(), combinedPatch));
        codeReviewRepository.save(review);

        return codeReviewMapper.toResponse(review, suggestions);
    }

    private void validateGitHubIdentity(String requestedUsername, String authenticatedUsername) {
        if (authenticatedUsername == null || authenticatedUsername.isBlank()) {
            throw new BadRequestException("Unable to verify GitHub user from token");
        }
        if (!requestedUsername.equalsIgnoreCase(authenticatedUsername)) {
            throw new BadRequestException("GitHub username does not match token owner");
        }
    }

    private User resolveOrCreateUser(GitHubRepositoryConnectRequest request, GitHubAuthenticatedUser authenticatedUser) {
        String normalizedGithubUsername = authenticatedUser.login();
        String normalizedEmail = resolveUserEmail(request.userEmail(), authenticatedUser);

        Optional<User> existingByGitHub = userRepository.findByGithubUsername(normalizedGithubUsername);
        Optional<User> existingByEmail = userRepository.findByEmail(normalizedEmail);

        User user = existingByGitHub.orElseGet(() -> existingByEmail.orElseGet(User::new));
        if (user.getGithubUsername() != null && !user.getGithubUsername().equalsIgnoreCase(normalizedGithubUsername)) {
            throw new BadRequestException("Email is already linked to a different GitHub account");
        }

        user.setName((authenticatedUser.name() == null || authenticatedUser.name().isBlank())
                ? normalizedGithubUsername
                : authenticatedUser.name());
        user.setEmail(normalizedEmail);
        user.setRole(user.getRole() == null ? UserRole.DEVELOPER : user.getRole());
        user.setGithubUsername(normalizedGithubUsername);
        user.setGithubAccessToken(request.githubToken());
        return userRepository.save(user);
    }

    private String resolveUserEmail(String requestedEmail, GitHubAuthenticatedUser authenticatedUser) {
        if (requestedEmail != null && !requestedEmail.isBlank()) {
            return requestedEmail.trim().toLowerCase();
        }
        if (authenticatedUser.email() != null && !authenticatedUser.email().isBlank()) {
            return authenticatedUser.email().trim().toLowerCase();
        }
        return authenticatedUser.login().toLowerCase() + "@github.local";
    }
}
