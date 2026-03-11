package com.aicodereviewassistant.service.ai;

import com.aicodereviewassistant.dto.CodeExplanationResponse;
import com.aicodereviewassistant.entity.AiSuggestion;

import java.util.List;

public interface AiCodeAnalysisService {
    List<AiSuggestion> analyzeCodePatch(String sourceName, String codePatch, String languageHint);

    CodeExplanationResponse explainCode(String code, String language, String context);

    String summarizePullRequest(String prTitle, String combinedDiff);
}
