package com.example.aidebugger.controller;

import com.example.aidebugger.entity.IncidentRecord;
import com.example.aidebugger.service.IncidentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<IncidentRecord> list() {
        return incidentService.listIncidents();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentRecord create(@Valid @RequestBody CreateIncidentRequest request) {
        return incidentService.createIncident(request.source(), request.description());
    }

    public record CreateIncidentRequest(
            @NotBlank String source,
            @NotBlank String description
    ) {
    }
}
