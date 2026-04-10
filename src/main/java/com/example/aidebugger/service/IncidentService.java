package com.example.aidebugger.service;

import com.example.aidebugger.entity.IncidentRecord;
import com.example.aidebugger.repository.IncidentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IncidentService {

    private final IncidentRecordRepository incidentRecordRepository;

    public IncidentService(IncidentRecordRepository incidentRecordRepository) {
        this.incidentRecordRepository = incidentRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentRecord> listIncidents() {
        return incidentRecordRepository.findAll();
    }

    public IncidentRecord createIncident(String source, String description) {
        IncidentRecord incident = new IncidentRecord();
        incident.setSource(source);
        incident.setDescription(description);
        return incidentRecordRepository.save(incident);
    }
}
