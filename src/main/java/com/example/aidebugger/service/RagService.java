package com.example.aidebugger.service;

import com.example.aidebugger.repository.DocumentRepository;
import com.pgvector.PGvector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RagService {

    private final EmbeddingService embeddingService;
    private final DocumentRepository documentRepository;

    public RagService(EmbeddingService embeddingService, DocumentRepository documentRepository) {
        this.embeddingService = embeddingService;
        this.documentRepository = documentRepository;
    }

    public List<String> findRelevantContents(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            throw new IllegalArgumentException("User query must not be blank");
        }

        List<Double> queryEmbedding = embeddingService.createEmbedding(userQuery);
        //PGvector vectorLiteral = new PGvector(queryEmbedding);
        //return documentRepository.findTop5ContentsBySimilarity(queryEmbedding);

        // Convert List<Double> → pgvector literal string: "[0.12, 0.98, ...]"
        String embeddingLiteral = queryEmbedding.toString();
        return documentRepository.findTop5ContentsBySimilarity(embeddingLiteral);
    }
}
