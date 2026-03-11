package com.aicodereviewassistant.util;

import com.aicodereviewassistant.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class GitHubUrlParser {

    public RepoCoordinates parse(String repoUrl) {
        String normalized = repoUrl.replace("https://github.com/", "")
                .replace("http://github.com/", "")
                .replace(".git", "");

        String[] parts = normalized.split("/");
        if (parts.length < 2) {
            throw new BadRequestException("Invalid GitHub repository URL");
        }
        return new RepoCoordinates(parts[0], parts[1]);
    }

    public record RepoCoordinates(String owner, String repo) {
    }
}
