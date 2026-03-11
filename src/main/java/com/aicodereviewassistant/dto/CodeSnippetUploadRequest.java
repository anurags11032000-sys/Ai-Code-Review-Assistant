package com.aicodereviewassistant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CodeSnippetUploadRequest(
        @NotBlank String fileName,
        @NotBlank String language,
        @NotBlank String sourceCode,
        @Email String userEmail
) {
}
