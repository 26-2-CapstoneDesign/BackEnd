package com.youthguide.api.global.ingest.repository;

import com.youthguide.api.global.ingest.domain.ImportRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportRunRepository extends JpaRepository<ImportRun, Long> {
}