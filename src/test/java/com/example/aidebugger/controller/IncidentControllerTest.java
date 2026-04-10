package com.example.aidebugger.controller;

import com.example.aidebugger.entity.IncidentRecord;
import com.example.aidebugger.service.IncidentService;
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

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    @Test
    void listReturnsOkAndBody() throws Exception {
        IncidentRecord incident = new IncidentRecord();
        incident.setId(1L);
        incident.setSource("monitoring");
        incident.setDescription("high memory usage");
        incident.setStatus("OPEN");
        when(incidentService.listIncidents()).thenReturn(List.of(incident));

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].source").value("monitoring"));
    }

    @Test
    void createReturnsCreated() throws Exception {
        IncidentRecord created = new IncidentRecord();
        created.setId(2L);
        created.setSource("ops");
        created.setDescription("latency spike");
        created.setStatus("OPEN");
        when(incidentService.createIncident("ops", "latency spike")).thenReturn(created);

        String payload = """
                {
                  "source": "ops",
                  "description": "latency spike"
                }
                """;

        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createWithBlankFieldsReturnsBadRequest() throws Exception {
        String payload = """
                {
                  "source": "",
                  "description": ""
                }
                """;

        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
