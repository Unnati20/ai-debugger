package com.example.aidebugger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    // ===========================================
    // NEW HUGGING FACE API IMPLEMENTATION (ACTIVE)
    // ===========================================

    private final RestTemplate restTemplate;
    private final String huggingFaceUrl;
    private final String apiKey;
    private final String model;

    public OllamaService(
            RestTemplate restTemplate,
            @Value("${huggingface.api-key:}") String apiKey,
            @Value("${huggingface.url:https://router.huggingface.co/v1/chat/completions}") String huggingFaceUrl,
            @Value("${huggingface.model:meta-llama/Llama-3.1-8B-Instruct:novita}") String model
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.huggingFaceUrl = huggingFaceUrl;
        this.model = model;
    }

    // ===========================================
    // OLD OLLAMA CODE - COMMENTED OUT FOR REFERENCE
    // ===========================================
    /*
    // OLD OLLAMA SERVICE CODE - COMMENTED OUT
    private final RestTemplate restTemplate;
    private final String ollamaUrl;

    public OllamaService(
            RestTemplate restTemplate,
            @Value("${olama.url:http://host.docker.internal:11434/api/generate}") String ollamaUrl,
            @Value("${olama.chat-model:mistral}") String model
    ) {
        this.restTemplate = restTemplate;
        this.ollamaUrl = ollamaUrl;
        this.model = model;
    }
    */

    // ===========================================
    // OLD COHERE CODE - COMMENTED OUT FOR REFERENCE
    // ===========================================
    /*
    // Cohere client
    private final Cohere cohere;
    private final String model;

    // NEW COHERE-BASED CONSTRUCTOR
    public OllamaService(
            @Value("${cohere.api-key:}") String cohereApiKey,
            @Value("${cohere.chat-model:command-a-03-2025}") String model
    ) {
        this.cohere = Cohere.builder().token(cohereApiKey).clientName("aidebugger").build();
        this.model = model;
    }
    */

    // ===========================================
    // NEW HUGGING FACE API GENERATE METHOD (ACTIVE)
    // ===========================================
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be blank");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(message),
                    "stream", false
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    huggingFaceUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("choices")) {
                throw new IllegalStateException("Hugging Face API returned invalid response");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("Hugging Face API returned empty choices");
            }

            Map<String, Object> firstChoice = choices.get(0);
            if (!firstChoice.containsKey("message")) {
                throw new IllegalStateException("Hugging Face API returned invalid choice format");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
            if (messageObj == null || !messageObj.containsKey("content")) {
                throw new IllegalStateException("Hugging Face API returned invalid message format");
            }

            Object content = messageObj.get("content");
            if (content == null) {
                throw new IllegalStateException("Hugging Face API returned empty content");
            }

            return content.toString().trim();

        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to call Hugging Face API", e);
        }
    }

    // ===========================================
    // OLD OLLAMA AND COHERE CODE - COMMENTED OUT FOR REFERENCE
    // ===========================================
    /*
    // OLD OLLAMA GENERATE METHOD - COMMENTED OUT
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be blank");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("prompt", prompt);
        request.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(ollamaUrl, entity, Map.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Failed to call Ollama API", ex);
        }

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("response")) {
            throw new IllegalStateException("Ollama API returned an invalid response");
        }

        Object responseText = body.get("response");
        if (responseText == null) {
            throw new IllegalStateException("Ollama API returned empty response");
        }

        return responseText.toString().trim();
    }

    // OLD COHERE GENERATE METHOD - COMMENTED OUT
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt must not be blank");
        }

        try {
            ChatResponse response = cohere.v2()
                    .chat(
                            V2ChatRequest.builder()
                                    .model(model)
                                    .messages(
                                            List.of(
                                                    ChatMessageV2.user(
                                                            UserMessage.builder()
                                                                    .content(
                                                                            UserMessageContent.of(prompt))
                                                                    .build())))
                                    .build());

            if (response == null) {
                throw new IllegalStateException("Cohere API returned null response");
            }

            return response.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to call Cohere API", e);
        }
    }
    */
}
