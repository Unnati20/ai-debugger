package com.example.aidebugger.controller;

import com.example.aidebugger.entity.DocumentEntity;
import com.example.aidebugger.service.IngestionService;
import com.example.aidebugger.service.LlmService;
import com.example.aidebugger.service.RagService;
import com.pgvector.PGvector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DebugController.class)
class DebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngestionService ingestionService;

    @MockBean
    private RagService ragService;

    @MockBean
    private LlmService llmService;

    @Test
    void ingestReturnsCreated() throws Exception {
        DocumentEntity saved = new DocumentEntity();
        saved.setId(10L);
        saved.setContent("sample");
        saved.setEmbedding(java.util.Arrays.asList(0.1, 0.2));
        when(ingestionService.ingest("Payment service failure log")).thenReturn(saved);

        String payload = """
                {
                  "rawText": "Payment service failure log"
                }
                """;

        mockMvc.perform(post("/api/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentId").value(10))
                .andExpect(jsonPath("$.status").value("INGESTED"));
    }

    @Test
    void askReturnsAnswerAndContext() throws Exception {
        List<String> context = List.of("Runbook step 1", "Runbook step 2");
        when(ragService.findRelevantContents("How to fix payment failure?")).thenReturn(context);
        when(llmService.generateAnswer("How to fix payment failure?", context))
                .thenReturn("Check gateway health and retry failed events.");

        mockMvc.perform(get("/api/ask")
                        .param("query", "How to fix payment failure?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Check gateway health and retry failed events."))
                .andExpect(jsonPath("$.context[0]").value("Runbook step 1"))
                .andExpect(jsonPath("$.context[1]").value("Runbook step 2"));
    }

    @Test
    void askWithBlankQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/ask")
                        .param("query", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
