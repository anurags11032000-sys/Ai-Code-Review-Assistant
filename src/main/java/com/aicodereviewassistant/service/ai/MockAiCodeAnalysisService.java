package com.aicodereviewassistant.service.ai;

import com.aicodereviewassistant.dto.CodeExplanationResponse;
import com.aicodereviewassistant.entity.AiSuggestion;
import com.aicodereviewassistant.entity.SuggestionType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "ai.mock.enabled", havingValue = "true")
public class MockAiCodeAnalysisService implements AiCodeAnalysisService {

    @Override
    public List<AiSuggestion> analyzeCodePatch(String sourceName, String codePatch, String languageHint) {
        List<AiSuggestion> suggestions = new ArrayList<>();

        if (codePatch != null && codePatch.toLowerCase().contains("password")) {
            suggestions.add(build(
                    SuggestionType.SECURITY,
                    "HIGH",
                    sourceName,
                    "Hardcoded credential detected",
                    "Avoid hardcoded secrets. Use environment variables or secret manager.",
                    "Move secret to env var and inject through configuration."
            ));
        }

        if (codePatch != null && codePatch.contains("for(") && codePatch.contains("length")) {
            suggestions.add(build(
                    SuggestionType.PERFORMANCE,
                    "MEDIUM",
                    sourceName,
                    "Loop optimization opportunity",
                    "Repeated property access inside loop may be optimized.",
                    "Cache collection length in a local variable before loop."
            ));
        }

        if (suggestions.isEmpty()) {
            suggestions.add(build(
                    SuggestionType.READABILITY,
                    "LOW",
                    sourceName,
                    "Mock analysis complete",
                    "No obvious issues detected in mock mode. Run with real LLM for deeper review.",
                    "Add unit tests and static analysis for stronger confidence."
            ));
        }

        return suggestions;
    }

    @Override
    public CodeExplanationResponse explainCode(String code, String language, String context) {
        String explanation = "Mock explanation: this snippet appears to process data in a loop and accumulate a result. "
                + "In production mode, the LLM would provide deeper logic and domain-specific reasoning.";

        String complexity = "Estimated complexity: O(n) time for a single-pass loop, O(1) extra space.";
        String refactor = "Refactoring hint: extract loop body to a named method and validate edge cases with tests.";

        return new CodeExplanationResponse(explanation, complexity, refactor);
    }

    @Override
    public String summarizePullRequest(String prTitle, String combinedDiff) {
        return "Mock PR Summary: " + prTitle
                + " | changes reviewed in mock mode. Validate security checks, tests, and error handling before merge.";
    }

    private AiSuggestion build(
            SuggestionType type,
            String severity,
            String filePath,
            String title,
            String details,
            String suggestedFix
    ) {
        AiSuggestion suggestion = new AiSuggestion();
        suggestion.setSuggestionType(type);
        suggestion.setSeverity(severity);
        suggestion.setFilePath(filePath);
        suggestion.setTitle(title);
        suggestion.setDetails(details);
        suggestion.setSuggestedFix(suggestedFix);
        return suggestion;
    }
}

