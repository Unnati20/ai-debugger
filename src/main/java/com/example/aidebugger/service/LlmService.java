package com.example.aidebugger.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LlmService {

    private final OllamaService ollamaService;

    public LlmService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public String generateAnswer(String userQuery, List<String> ragContext) {
        if (userQuery == null || userQuery.isBlank()) {
            throw new IllegalArgumentException("User query must not be blank");
        }

        String prompt = """
                You are an AI debugging assistant.

                Use ONLY the context below to answer.

                Context:
                %s

                Question:
                %s

                Provide:
                - Root cause
                - Explanation
                - Suggested fix
                """.formatted(formatContext(ragContext), userQuery);

        return ollamaService.generate(prompt);
    }

    private String formatContext(List<String> ragContext) {
        if (ragContext == null || ragContext.isEmpty()) {
            return "No relevant context found.";
        }
        return ragContext.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
