package com.aicodereviewassistant.integration.github;

public class GitHubModels {

    public record GitHubPullRequest(
            Integer number,
            String title,
            String state,
            GitHubUser user,
            GitHubBranchRef head,
            GitHubBranchRef base
    ) {}

    public record GitHubUser(String login) {}

    public record GitHubAuthenticatedUser(
            String login,
            String name,
            String email
    ) {}

    public record GitHubBranchRef(String ref) {}

    public record GitHubPullRequestFile(
            String filename,
            String status,
            Integer additions,
            Integer deletions,
            Integer changes,
            String patch,
            String raw_url
    ) {}
}
