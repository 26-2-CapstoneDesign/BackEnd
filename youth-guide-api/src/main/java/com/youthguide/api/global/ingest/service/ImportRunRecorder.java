package com.youthguide.api.global.ingest.service;

import com.youthguide.api.global.ingest.domain.DataSource;
import com.youthguide.api.global.ingest.domain.ImportRun;
import com.youthguide.api.global.ingest.repository.DataSourceRepository;
import com.youthguide.api.global.ingest.repository.ImportRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * import_run 시작/종료는 본 작업이 롤백돼도 남아야 해서 별도 트랜잭션으로 기록
 */
@Service
@RequiredArgsConstructor
public class ImportRunRecorder {

    private final DataSourceRepository dataSourceRepository;
    private final ImportRunRepository importRunRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DataSource getOrCreateSource(String sourceKey, String displayName, String officialUrl) {
        return dataSourceRepository.findBySourceKey(sourceKey)
                .orElseGet(() -> dataSourceRepository.save(
                        DataSource.of(sourceKey, displayName, officialUrl)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long start(Long sourceId, String operationKey, String requestContext, String adapterVersion) {
        DataSource source = dataSourceRepository.getReferenceById(sourceId);
        return importRunRepository.save(
                ImportRun.start(source, operationKey, requestContext, adapterVersion)).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(Long runId, String outcome, Long expectedCount, long receivedCount) {
        importRunRepository.findById(runId)
                .orElseThrow()
                .finish(outcome, expectedCount, receivedCount);
    }
}