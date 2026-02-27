package com.example.rag.model;

public record QueryResultChunk(
        String documentId,
        String fileName,
        int chunkNumber,
        double score,
        String text
) {
}
