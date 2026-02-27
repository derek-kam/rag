package com.example.rag.service;

import com.example.rag.model.QueryResultChunk;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class OracleVectorRepository {

    @ConfigProperty(name = "db.url")
    String dbUrl;

    @ConfigProperty(name = "db.username")
    String dbUsername;

    @ConfigProperty(name = "db.password")
    String dbPassword;

    public void storeEmbeddings(String documentId,
                                String fileName,
                                String contentType,
                                List<TextSegment> chunks,
                                List<Embedding> embeddings) {
        String sql = """
                INSERT INTO rag_chunks (document_id, file_name, content_type, chunk_number, chunk_text, embedding)
                VALUES (?, ?, ?, ?, ?, TO_VECTOR(?))
                """;
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < chunks.size(); i++) {
                ps.setString(1, documentId);
                ps.setString(2, fileName);
                ps.setString(3, contentType);
                ps.setInt(4, i + 1);
                ps.setString(5, chunks.get(i).text());
                ps.setString(6, toVectorLiteral(embeddings.get(i)));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to persist chunk embeddings", e);
        }
    }

    public List<QueryResultChunk> search(Embedding queryEmbedding, int maxResults) {
        String sql = """
                SELECT document_id,
                       file_name,
                       chunk_number,
                       chunk_text,
                       (1 - VECTOR_DISTANCE(embedding, TO_VECTOR(?), COSINE)) AS similarity_score
                  FROM rag_chunks
                 ORDER BY VECTOR_DISTANCE(embedding, TO_VECTOR(?), COSINE)
                 FETCH FIRST ? ROWS ONLY
                """;

        List<QueryResultChunk> results = new ArrayList<>();
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String vector = toVectorLiteral(queryEmbedding);
            ps.setString(1, vector);
            ps.setString(2, vector);
            ps.setInt(3, maxResults);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new QueryResultChunk(
                            rs.getString("document_id"),
                            rs.getString("file_name"),
                            rs.getInt("chunk_number"),
                            rs.getDouble("similarity_score"),
                            rs.getString("chunk_text")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query vector store", e);
        }
        return results;
    }

    private Connection openConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        conn.setAutoCommit(false);
        return conn;
    }

    private String toVectorLiteral(Embedding embedding) {
        float[] vector = embedding.vector();
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            builder.append(vector[i]);
            if (i < vector.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
