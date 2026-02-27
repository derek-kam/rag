package com.example.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ChunkingService {

    private static final int MAX_SEGMENT_SIZE = 700;
    private static final int MAX_OVERLAP = 80;

    private final DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(MAX_SEGMENT_SIZE, MAX_OVERLAP);

    public List<TextSegment> split(String text) {
        return splitter.split(Document.from(text));
    }
}
