package com.aicodereviewassistant.service.ai;

import com.aicodereviewassistant.entity.AiSuggestion;
import com.aicodereviewassistant.entity.SuggestionType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class SuggestionPostProcessor {

    private static final Set<String> ALLOWED_SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    public List<AiSuggestion> normalizeAndDedupe(List<AiSuggestion> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return List.of();
        }

        Map<String, AiSuggestion> unique = new LinkedHashMap<>();
        for (AiSuggestion suggestion : suggestions) {
            if (suggestion == null) {
                continue;
            }

            suggestion.setSuggestionType(normalizeType(suggestion.getSuggestionType()));
            suggestion.setSeverity(normalizeSeverity(suggestion.getSeverity()));
            suggestion.setTitle(normalizeText(suggestion.getTitle(), "Suggestion"));
            suggestion.setDetails(normalizeText(suggestion.getDetails(), "No details provided"));
            suggestion.setFilePath(trimToNull(suggestion.getFilePath()));
            suggestion.setSuggestedFix(trimToNull(suggestion.getSuggestedFix()));

            String key = dedupeKey(suggestion);
            unique.putIfAbsent(key, suggestion);
        }

        return new ArrayList<>(unique.values());
    }

    private SuggestionType normalizeType(SuggestionType type) {
        return type == null ? SuggestionType.READABILITY : type;
    }

    private String normalizeSeverity(String severity) {
        if (!StringUtils.hasText(severity)) {
            return "MEDIUM";
        }
        String normalized = severity.trim().toUpperCase(Locale.ROOT);
        return ALLOWED_SEVERITIES.contains(normalized) ? normalized : "MEDIUM";
    }

    private String normalizeText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String dedupeKey(AiSuggestion suggestion) {
        String file = suggestion.getFilePath() == null ? "" : suggestion.getFilePath().toLowerCase(Locale.ROOT);
        String line = suggestion.getLineNumber() == null ? "" : String.valueOf(suggestion.getLineNumber());
        String title = suggestion.getTitle().toLowerCase(Locale.ROOT);
        return suggestion.getSuggestionType().name() + "|" + suggestion.getSeverity() + "|" + file + "|" + line + "|" + title;
    }
}

