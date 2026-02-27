package com.example.rag.model;

import java.util.List;

public record QueryResponse(
        String query,
        List<QueryResultChunk> matches
) {
}
