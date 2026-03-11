package com.aicodereviewassistant.service.ai;

import com.aicodereviewassistant.dto.CodeExplanationResponse;
import com.aicodereviewassistant.entity.AiSuggestion;
import com.aicodereviewassistant.entity.SuggestionType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "ai.mock.enabled", havingValue = "false", matchIfMissing = true)
public class SpringAiCodeAnalysisService implements AiCodeAnalysisService {

    private final ChatClient chatClient;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;

    public SpringAiCodeAnalysisService(
            ChatClient.Builder chatClientBuilder,
            PromptTemplateService promptTemplateService,
            ObjectMapper objectMapper
    ) {
        this.chatClient = chatClientBuilder.build();
        this.promptTemplateService = promptTemplateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AiSuggestion> analyzeCodePatch(String sourceName, String codePatch, String languageHint) {
        String systemPrompt = promptTemplateService.loadTemplate("prompts/code-review-system-prompt.txt");
        String userPrompt = "SOURCE_NAME: " + sourceName + "\nLANGUAGE_HINT: " + languageHint + "\nCODE_PATCH:\n" + codePatch;

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return mapResponseToSuggestions(response);
    }

    @Override
    public CodeExplanationResponse explainCode(String code, String language, String context) {
        String systemPrompt = promptTemplateService.loadTemplate("prompts/code-explanation-system-prompt.txt");
        String userPrompt = "LANGUAGE: " + language + "\nCONTEXT: " + (context == null ? "N/A" : context) + "\nCODE:\n" + code;

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return parseExplanation(response);
    }

    @Override
    public String summarizePullRequest(String prTitle, String combinedDiff) {
        String systemPrompt = promptTemplateService.loadTemplate("prompts/pr-summary-system-prompt.txt");
        String userPrompt = "PR_TITLE: " + prTitle + "\nPR_DIFF:\n" + combinedDiff;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    private List<AiSuggestion> mapResponseToSuggestions(String response) {
        try {
            String normalized = normalizeJsonPayload(response);
            List<Map<String, Object>> json = objectMapper.readValue(normalized, new TypeReference<>() {});
            List<AiSuggestion> suggestions = new ArrayList<>();
            for (Map<String, Object> item : json) {
                AiSuggestion suggestion = new AiSuggestion();
                suggestion.setSuggestionType(resolveSuggestionType(item.get("suggestionType")));
                suggestion.setSeverity(resolveSeverity(item.get("severity")));
                suggestion.setFilePath((String) item.get("filePath"));
                suggestion.setLineNumber(resolveLineNumber(item.get("lineNumber")));
                suggestion.setTitle((String) item.getOrDefault("title", "Suggestion"));
                suggestion.setDetails((String) item.getOrDefault("details", "No details provided"));
                suggestion.setSuggestedFix((String) item.get("suggestedFix"));
                suggestions.add(suggestion);
            }
            return suggestions;
        } catch (Exception ex) {
            AiSuggestion fallback = new AiSuggestion();
            fallback.setSuggestionType(SuggestionType.READABILITY);
            fallback.setSeverity("LOW");
            fallback.setTitle("Model response parsing fallback");
            fallback.setDetails(response);
            return List.of(fallback);
        }
    }

    private CodeExplanationResponse parseExplanation(String response) {
        try {
            String normalized = normalizeJsonPayload(response);
            Map<String, String> json = objectMapper.readValue(normalized, new TypeReference<>() {});
            return new CodeExplanationResponse(
                    json.getOrDefault("explanation", "Explanation unavailable"),
                    json.getOrDefault("complexityNotes", "No complexity notes"),
                    json.getOrDefault("refactoringHint", "No refactoring hint")
            );
        } catch (Exception ex) {
            return new CodeExplanationResponse(response, "Could not parse structured complexity notes", "Could not parse refactoring hints");
        }
    }

    private String normalizeJsonPayload(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1).trim();
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            int start = trimmed.indexOf('[');
            int end = trimmed.lastIndexOf(']');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }

    private SuggestionType resolveSuggestionType(Object rawType) {
        if (rawType == null) {
            return SuggestionType.READABILITY;
        }
        String candidate = String.valueOf(rawType).trim().toUpperCase();
        if (candidate.contains("|")) {
            candidate = candidate.substring(0, candidate.indexOf('|')).trim();
        }
        try {
            return SuggestionType.valueOf(candidate);
        } catch (Exception ex) {
            return SuggestionType.READABILITY;
        }
    }

    private String resolveSeverity(Object rawSeverity) {
        String fallback = "MEDIUM";
        if (rawSeverity == null) {
            return fallback;
        }
        String candidate = String.valueOf(rawSeverity).trim().toUpperCase();
        if (candidate.contains("|")) {
            candidate = candidate.substring(0, candidate.indexOf('|')).trim();
        }
        Set<String> allowed = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
        return allowed.contains(candidate) ? candidate : fallback;
    }

    private Integer resolveLineNumber(Object rawLine) {
        if (rawLine == null) {
            return null;
        }
        if (rawLine instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(rawLine).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
