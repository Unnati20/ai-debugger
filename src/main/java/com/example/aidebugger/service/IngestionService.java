package com.example.aidebugger.service;

import com.example.aidebugger.entity.DocumentEntity;
import com.example.aidebugger.repository.DocumentRepository;
import com.pgvector.PGvector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final EmbeddingService embeddingService;
    private final DocumentRepository documentRepository;

    public IngestionService(EmbeddingService embeddingService, DocumentRepository documentRepository) {
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
    }

    public DocumentEntity ingest(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            log.warn("Ingest called with blank text");
            throw new IllegalArgumentException("Raw text must not be blank");
        }

        log.info("Starting ingestion for text length={}", rawText.length());
        List<Double> embedding = embeddingService.createEmbedding(rawText);
        log.debug("Embedding created, size={}", embedding.size());

        
   

        DocumentEntity document = new DocumentEntity();
        document.setContent(rawText);
        document.setEmbedding(embedding);

        DocumentEntity saved = documentRepository.save(document);
        log.info("Ingestion completed for documentId={}", saved.getId());
        return saved;
    }
}
