package com.aicodereviewassistant.mapper;

import com.aicodereviewassistant.dto.AiSuggestionDto;
import com.aicodereviewassistant.dto.CodeReviewResponse;
import com.aicodereviewassistant.entity.AiSuggestion;
import com.aicodereviewassistant.entity.CodeReview;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodeReviewMapper {

    public CodeReviewResponse toResponse(CodeReview review, List<AiSuggestion> suggestions) {
        List<AiSuggestionDto> suggestionDtos = suggestions.stream()
                .map(this::toSuggestionDto)
                .toList();

        return new CodeReviewResponse(
                review.getId(),
                review.getSourceName(),
                review.getSourceType(),
                review.getStatus(),
                review.getRiskScore(),
                review.getOverallSummary(),
                review.getCreatedAt(),
                suggestionDtos
        );
    }

    public AiSuggestionDto toSuggestionDto(AiSuggestion suggestion) {
        return new AiSuggestionDto(
                suggestion.getId(),
                suggestion.getSuggestionType(),
                suggestion.getSeverity(),
                suggestion.getFilePath(),
                suggestion.getLineNumber(),
                suggestion.getTitle(),
                suggestion.getDetails(),
                suggestion.getSuggestedFix()
        );
    }
}
