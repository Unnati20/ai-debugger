package com.example.aidebugger.service;

import com.example.aidebugger.entity.DocumentEntity;
import com.example.aidebugger.entity.IncidentRecord;
import com.example.aidebugger.repository.DocumentRepository;
import com.example.aidebugger.repository.IncidentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class McpToolService {

    private final IncidentRecordRepository incidentRecordRepository;
    private final DocumentRepository documentRepository;
    private final RagService ragService;

    public McpToolService(
            IncidentRecordRepository incidentRecordRepository,
            DocumentRepository documentRepository,
            RagService ragService
    ) {
        this.incidentRecordRepository = incidentRecordRepository;
        this.documentRepository = documentRepository;
        this.ragService = ragService;
    }

    public List<LogEntry> getLogs() {
        return incidentRecordRepository
                .findTop20BySourceContainingIgnoreCaseOrderByCreatedAtDesc("log")
                .stream()
                .map(this::toLogEntry)
                .toList();
    }

    public List<RunbookEntry> getRunbooks() {
        return documentRepository
                .findTop20ByContentContainingIgnoreCaseOrderByIdDesc("runbook")
                .stream()
                .map(this::toRunbookEntry)
                .toList();
    }

    public List<String> searchSimilarIncidents(String query) {
        return ragService.findRelevantContents(query);
    }

    private LogEntry toLogEntry(IncidentRecord incidentRecord) {
        return new LogEntry(
                incidentRecord.getId(),
                incidentRecord.getSource(),
                incidentRecord.getDescription(),
                incidentRecord.getStatus(),
                incidentRecord.getCreatedAt()
        );
    }

    private RunbookEntry toRunbookEntry(DocumentEntity document) {
        return new RunbookEntry(document.getId(), document.getContent());
    }

    public record LogEntry(
            Long id,
            String source,
            String description,
            String status,
            Instant createdAt
    ) {
    }

    public record RunbookEntry(Long id, String content) {
    }
}
