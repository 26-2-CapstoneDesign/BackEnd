package com.youthguide.api.global.ingest.repository;

import com.youthguide.api.global.ingest.domain.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
    Optional<DataSource> findBySourceKey(String sourceKey);
}