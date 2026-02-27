package com.example.rag.model;

public record UploadResponse(
        String documentId,
        String fileName,
        String contentType,
        int chunksStored
) {
}
