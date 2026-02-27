package com.example.rag.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record QueryRequest(
        @NotBlank String query,
        @Min(1) int maxResults
) {
}
