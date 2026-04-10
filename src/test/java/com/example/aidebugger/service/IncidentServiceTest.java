package com.example.aidebugger.service;

import com.example.aidebugger.entity.IncidentRecord;
import com.example.aidebugger.repository.IncidentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRecordRepository incidentRecordRepository;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void listIncidentsReturnsRepositoryResults() {
        IncidentRecord incident = new IncidentRecord();
        incident.setSource("monitoring");
        incident.setDescription("test incident");
        when(incidentRecordRepository.findAll()).thenReturn(List.of(incident));

        List<IncidentRecord> result = incidentService.listIncidents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSource()).isEqualTo("monitoring");
    }

    @Test
    void createIncidentBuildsAndSavesEntity() {
        IncidentRecord saved = new IncidentRecord();
        saved.setId(1L);
        saved.setSource("ops");
        saved.setDescription("service unavailable");
        when(incidentRecordRepository.save(org.mockito.ArgumentMatchers.any(IncidentRecord.class))).thenReturn(saved);

        IncidentRecord result = incidentService.createIncident("ops", "service unavailable");

        ArgumentCaptor<IncidentRecord> captor = ArgumentCaptor.forClass(IncidentRecord.class);
        verify(incidentRecordRepository).save(captor.capture());
        IncidentRecord toSave = captor.getValue();
        assertThat(toSave.getSource()).isEqualTo("ops");
        assertThat(toSave.getDescription()).isEqualTo("service unavailable");
        assertThat(result.getId()).isEqualTo(1L);
    }
}
