package com.example.rag.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class EmbeddingService {

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public List<Embedding> embed(List<TextSegment> segments) {
        return embeddingModel.embedAll(segments).content();
    }

    public Embedding embedQuery(String query) {
        return embeddingModel.embed(query).content();
    }
}
