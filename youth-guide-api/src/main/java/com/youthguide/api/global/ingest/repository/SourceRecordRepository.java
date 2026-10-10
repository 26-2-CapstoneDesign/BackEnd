package com.youthguide.api.global.ingest.repository;

import com.youthguide.api.global.ingest.domain.SourceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRecordRepository extends JpaRepository<SourceRecord, Long> {
}