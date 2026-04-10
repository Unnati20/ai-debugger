package com.example.aidebugger.service;

import com.cohere.api.Cohere;
import com.cohere.api.resources.v2.requests.V2EmbedRequest;
import com.cohere.api.types.EmbedByTypeResponse;
import com.cohere.api.types.EmbedByTypeResponseEmbeddings;
import com.cohere.api.types.EmbedInputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    // ===========================================
    // NEW COHERE API IMPLEMENTATION (ACTIVE)
    // ===========================================

    private final Cohere cohere;
    private final String model;

    public EmbeddingService(
            @Value("${cohere.api-key:}") String cohereApiKey,
            @Value("${cohere.embedding-model:embed-v4.0}") String model
    ) {
        this.cohere = Cohere.builder().token(cohereApiKey).clientName("aidebugger").build();
        this.model = model;
        log.info("EmbeddingService initialized with Cohere: model={}", this.model);
    }

    // ===========================================
    // OLD OLLAMA CODE - COMMENTED OUT FOR REFERENCE
    // ===========================================
    /*
    // OLD OLLAMA SERVICE CODE - COMMENTED OUT
    private final RestTemplate restTemplate;
    private final String embeddingsUrl;
    private final String model;

    public EmbeddingService(
            RestTemplate restTemplate,
            @Value("${ollama.url:http://host.docker.internal:11434}") String baseUrl,
            @Value("${ollama.embedding-model:nomic-embed-text}") String model
    ) {
        this.restTemplate = restTemplate;
        this.embeddingsUrl = baseUrl + "/api/embeddings";
        this.model = model;
        log.info("EmbeddingService initialized: url={}, model={}", this.embeddingsUrl, this.model);
    }
    */

    // ===========================================
    // NEW COHERE API CREATE EMBEDDING METHOD (ACTIVE)
    // ===========================================
    public List<Double> createEmbedding(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            log.warn("Attempt to create embedding with blank text");
            throw new IllegalArgumentException("Input text must not be blank");
        }

        log.debug("Creating embedding for text length={}", inputText.length());

        try {
            log.debug("Calling Cohere embeddings API: model={}", model);

            EmbedByTypeResponse response = cohere.v2().embed(
                    V2EmbedRequest.builder()
                            .model(model)
                            .inputType(EmbedInputType.SEARCH_DOCUMENT)
                            .texts(List.of(inputText))
                            .build()
            );

            if (response == null || response.getEmbeddings() == null) {
                log.error("Cohere API returned invalid response");
                throw new IllegalStateException("Cohere embeddings API returned empty response");
            }

            EmbedByTypeResponseEmbeddings embeddings = response.getEmbeddings();
            List<List<Double>> floatVectors = embeddings.getFloat()
                    .orElseThrow(() -> {
                        log.error("Cohere response contained no float embeddings");
                        return new IllegalStateException("Cohere embeddings API returned no float embeddings");
                    });

            if (floatVectors.isEmpty() || floatVectors.get(0) == null || floatVectors.get(0).isEmpty()) {
                log.error("Cohere API returned empty embedding");
                throw new IllegalStateException("Cohere embeddings API returned empty embedding");
            }

            List<Double> embedding = floatVectors.get(0);

            log.debug("Embedding created successfully with Cohere, dimension={}", embedding.size());
            return embedding;

        } catch (Exception e) {
            log.error("Cohere API call failed", e);
            throw new IllegalStateException("Failed to call Cohere embeddings API", e);
        }
    }

    // ===========================================
    // OLD OLLAMA CODE - COMMENTED OUT FOR REFERENCE
    // ===========================================
    /*
    // OLD OLLAMA CREATE EMBEDDING METHOD - COMMENTED OUT
    public List<Double> createEmbedding(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            log.warn("Attempt to create embedding with blank text");
            throw new IllegalArgumentException("Input text must not be blank");
        }

        log.debug("Creating embedding for text length={}", inputText.length());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        EmbeddingRequest requestBody = new EmbeddingRequest(model, inputText);
        HttpEntity<EmbeddingRequest> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<EmbeddingResponse> response;
        try {
            log.debug("Calling Ollama API: url={}, model={}", embeddingsUrl, model);
            response = restTemplate.exchange(
                    embeddingsUrl,
                    HttpMethod.POST,
                    request,
                    EmbeddingResponse.class
            );
            log.debug("Ollama API response status={}", response.getStatusCode());
        } catch (RestClientException ex) {
            log.error("Ollama API call failed", ex);
            throw new IllegalStateException("Failed to call Ollama embeddings API", ex);
        }

        EmbeddingResponse responseBody = response.getBody();
        if (responseBody == null || responseBody.embedding() == null || responseBody.embedding().isEmpty()) {
            log.error("Ollama API returned invalid response: null={}, empty={}", responseBody == null, responseBody != null && responseBody.embedding().isEmpty());
            throw new IllegalStateException("Ollama embeddings API returned empty response");
        }

        log.debug("Embedding created successfully, dimension={}", responseBody.embedding().size());
        return responseBody.embedding();
    }

    // Ollama request format
    private record EmbeddingRequest(String model, String prompt) {}

    // Ollama response format
    private record EmbeddingResponse(List<Double> embedding) {}
    */
}
