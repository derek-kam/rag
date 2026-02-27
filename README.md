# Helidon MP RAG Service (Oracle Vector Store)

A Retrieval-Augmented Generation (RAG) retrieval service built with **Helidon MP**.

## Features
- Upload documents through REST API.
- Supported formats:
  - PDF
  - DOCX (DOCX, DOTX, DOCM, DOTM)
  - XLSX
  - PPTX (PPTX, POTX, PPSX, PPTM, POTM, PPSM)
  - HTML
  - ASCIIDOC (ADOC, ASCIIDOC, ASC)
  - MD (MD, MARKDOWN)
  - XML (XML, NXML)
  - TXT
  - JSON
  - CSV
  - IMAGE (BMP, JPG, JPEG, PNG, TIFF, TIF)
- Automatic text extraction (Apache Tika).
- Chunking/text splitting (LangChain4j).
- Embedding generation (AllMiniLmL6V2).
- Oracle Database 23ai vector persistence + vector index.
- Similarity query endpoint.

## Project Structure
- `src/main/java/com/example/rag/api`: JAX-RS API endpoints.
- `src/main/java/com/example/rag/service`: ingestion/query workflow services.
- `db/create_rag_schema.sql`: Oracle schema and vector index script.
- `docs/design.md`: design document.

## Prerequisites
- Java 21+
- Maven 3.9+
- Oracle Database 23ai+ with VECTOR support

## Database Setup
Run:

```sql
@db/create_rag_schema.sql
```

## Configuration
Set environment variables (optional; defaults provided):

```bash
export DB_URL="jdbc:oracle:thin:@localhost:1521/FREEPDB1"
export DB_USERNAME="RAG_APP"
export DB_PASSWORD="RagPassword123"
```

Config lives in `src/main/resources/META-INF/microprofile-config.properties`.

## Build and Run

```bash
mvn clean package
mvn exec:java -Dexec.mainClass=com.example.rag.AppMain
```

Service URL: `http://localhost:8080`

## API

### 1) Upload document
`POST /api/rag/documents`

Multipart form field:
- `file` (binary file)

Example:

```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -F "file=@/path/to/sample.pdf"
```

Sample response:

```json
{
  "documentId": "f03258a1-2f95-4a26-b2e1-a8cf7609e41a",
  "fileName": "sample.pdf",
  "contentType": "application/pdf",
  "chunksStored": 14
}
```

### 2) Query retrieval
`POST /api/rag/query`

Example:

```bash
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{"query":"What are the payment terms?", "maxResults":5}'
```

Sample response:

```json
{
  "query": "What are the payment terms?",
  "matches": [
    {
      "documentId": "f03258a1-2f95-4a26-b2e1-a8cf7609e41a",
      "fileName": "contract.pdf",
      "chunkNumber": 3,
      "score": 0.87,
      "text": "Payment must be made within 30 days..."
    }
  ]
}
```

## Notes
- Image OCR quality depends on Tika OCR setup (Tesseract availability).
- This service currently implements retrieval (R in RAG). You can layer an LLM generation endpoint on top of `/api/rag/query` results.
