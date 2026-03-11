package com.aicodereviewassistant.dto;

import jakarta.validation.constraints.NotBlank;

public record CodeExplainRequest(
        @NotBlank String code,
        @NotBlank String language,
        String context
) {
}
