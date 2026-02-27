package com.example.rag.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class TextExtractionService {

    private final Tika tika = new Tika();

    public String extract(InputStream inputStream) {
        try {
            return tika.parseToString(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse input document", e);
        }
    }
}
