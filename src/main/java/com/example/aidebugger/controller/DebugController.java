package com.example.aidebugger.controller;

import com.example.aidebugger.entity.DocumentEntity;
import com.example.aidebugger.service.EmbeddingService;
import com.example.aidebugger.service.IngestionService;
import com.example.aidebugger.service.LlmService;
import com.example.aidebugger.service.RagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);

    private final IngestionService ingestionService;
    private final RagService ragService;
    private final LlmService llmService;
    private final EmbeddingService embeddingService;

    public DebugController(
            IngestionService ingestionService,
            RagService ragService,
            LlmService llmService,
            EmbeddingService embeddingService
    ) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
        this.llmService = llmService;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED)
    public IngestResponse ingest(@Valid @RequestBody IngestRequest request) {
        log.info("POST /api/ingest - rawText length={}", request.rawText().length());
        try {
            DocumentEntity document = ingestionService.ingest(request.rawText());
            log.info("Ingestion successful, documentId={}", document.getId());
            return new IngestResponse(document.getId(), "INGESTED");
        } catch (Exception e) {
            log.error("Ingestion failed", e);
            throw e;
        }
    }

    @GetMapping("/ask")
    public AskResponse ask(@RequestParam("query") @NotBlank String query) {
        log.info("GET /api/ask - query={}", query);
        try {
            List<String> context = ragService.findRelevantContents(query);
            log.debug("Found {} relevant context items", context.size());
            String answer = llmService.generateAnswer(query, context);
            log.info("Question answered successfully");
            return new AskResponse(answer, context);
        } catch (Exception e) {
            log.error("Question answering failed", e);
            throw e;
        }
    }

    @PostMapping("/embeddings")
    public EmbeddingResponse embeddings(@Valid @RequestBody EmbeddingRequest request) {
        log.info("POST /api/embeddings - text length={}", request.text().length());
        try {
            List<Double> embedding = embeddingService.createEmbedding(request.text());
            log.info("Embedding created successfully, dimension={}", embedding.size());
            return new EmbeddingResponse(embedding);
        } catch (Exception e) {
            log.error("Embedding creation failed", e);
            throw e;
        }
    }

    public record IngestRequest(@NotBlank String rawText) {
    }

    public record IngestResponse(Long documentId, String status) {
    }

    public record AskResponse(String answer, List<String> context) {
    }

    public record EmbeddingRequest(@NotBlank String text) {
    }

    public record EmbeddingResponse(List<Double> embedding) {
    }
}
