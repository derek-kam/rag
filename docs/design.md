# Helidon MP RAG Service Design

## Goals
- Ingest enterprise documents through an HTTP upload endpoint.
- Extract and split text into semantic chunks.
- Generate embeddings for each chunk.
- Persist vectors in Oracle Database and support vector similarity search.
- Expose a query endpoint for retrieval of relevant document chunks.

## Architecture

```text
Client
  ├─ POST /api/rag/documents (multipart file)
  └─ POST /api/rag/query (JSON query)

Helidon MP Service
  ├─ RagResource (JAX-RS endpoints)
  ├─ RagService (workflow orchestration)
  ├─ TextExtractionService (Apache Tika)
  ├─ ChunkingService (LangChain4j sentence splitter)
  ├─ EmbeddingService (AllMiniLmL6V2 local model)
  └─ OracleVectorRepository (Oracle JDBC vector SQL)

Oracle Database 23ai
  ├─ rag_chunks table with VECTOR(384, FLOAT32)
  └─ VECTOR INDEX for cosine retrieval
```

## Ingestion Flow
1. Validate file extension against supported formats.
2. Parse file content into plain text using Apache Tika.
3. Split extracted text into overlapping chunks.
4. Generate 384-dimension embeddings for each chunk.
5. Insert chunk metadata + vector into `rag_chunks`.

## Query Flow
1. Receive query text and optional `maxResults`.
2. Generate query embedding (same model as ingestion).
3. Execute Oracle vector similarity SQL:
   - `ORDER BY VECTOR_DISTANCE(embedding, TO_VECTOR(:query), COSINE)`
4. Return top-k chunks with score and source metadata.

## Supported File Types
- PDF
- DOCX family: DOCX, DOTX, DOCM, DOTM
- XLSX
- PPTX family: PPTX, POTX, PPSX, PPTM, POTM, PPSM
- HTML
- AsciiDoc: ADOC, ASCIIDOC, ASC
- Markdown: MD, MARKDOWN
- XML: XML, NXML
- TXT
- JSON
- CSV
- Images: BMP, JPG, JPEG, PNG, TIFF, TIF

## Notes & Production Hardening
- For image OCR, ensure Tesseract is installed and configured for Tika.
- Move from `DriverManager` to connection pooling (UCP/Hikari) for high throughput.
- Add authN/authZ, document-level ACL filtering, and tenant partitioning.
- Optional: store full documents in object storage and keep URI in metadata.
- Tune vector index parameters (`TARGET ACCURACY`) based on latency/SLA.
