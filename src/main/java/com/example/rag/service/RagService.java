package com.example.rag.service;

import com.example.rag.model.QueryResponse;
import com.example.rag.model.UploadResponse;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class RagService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "pdf",
            "docx", "dotx", "docm", "dotm",
            "xlsx",
            "pptx", "potx", "ppsx", "pptm", "potm", "ppsm",
            "html",
            "adoc", "asciidoc", "asc",
            "md", "markdown",
            "xml", "nxml",
            "txt",
            "json",
            "csv",
            "bmp", "jpg", "jpeg", "png", "tiff", "tif"
    );

    @Inject
    TextExtractionService textExtractionService;

    @Inject
    ChunkingService chunkingService;

    @Inject
    EmbeddingService embeddingService;

    @Inject
    OracleVectorRepository repository;

    public UploadResponse ingest(String fileName, String contentType, InputStream inputStream) {
        validateExtension(fileName);
        String docId = UUID.randomUUID().toString();

        String text = textExtractionService.extract(inputStream);
        List<TextSegment> chunks = chunkingService.split(text);
        List<Embedding> embeddings = embeddingService.embed(chunks);
        repository.storeEmbeddings(docId, fileName, contentType, chunks, embeddings);

        return new UploadResponse(docId, fileName, contentType, chunks.size());
    }

    public QueryResponse query(String queryText, int maxResults) {
        Embedding queryEmbedding = embeddingService.embedQuery(queryText);
        return new QueryResponse(queryText, repository.search(queryEmbedding, maxResults));
    }

    private void validateExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx == -1 || idx == fileName.length() - 1) {
            throw new IllegalArgumentException("File extension is required");
        }
        String extension = fileName.substring(idx + 1).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
    }
}
