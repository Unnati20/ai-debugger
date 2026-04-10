package com.example.aidebugger.repository;

import com.example.aidebugger.entity.IncidentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRecordRepository extends JpaRepository<IncidentRecord, Long> {

    List<IncidentRecord> findTop20BySourceContainingIgnoreCaseOrderByCreatedAtDesc(String source);
}
