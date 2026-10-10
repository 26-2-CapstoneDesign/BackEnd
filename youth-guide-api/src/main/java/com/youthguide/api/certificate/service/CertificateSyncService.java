package com.youthguide.api.certificate.service;

import com.youthguide.api.certificate.domain.Certificate;
import com.youthguide.api.certificate.domain.CertificateSourceKey;
import com.youthguide.api.certificate.dto.SyncResult;
import com.youthguide.api.certificate.dto.qnet.CertificateItem;
import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.certificate.repository.CertificateSourceKeyRepository;
import com.youthguide.api.certificate.service.QnetApiClient.FetchResult;
import com.youthguide.api.certificate.service.QnetApiClient.PagedItem;
import com.youthguide.api.global.config.QnetApiProperties;
import com.youthguide.api.global.ingest.domain.DataSource;
import com.youthguide.api.global.ingest.domain.ImportRun;
import com.youthguide.api.global.ingest.domain.SourceRecord;
import com.youthguide.api.global.ingest.repository.DataSourceRepository;
import com.youthguide.api.global.ingest.repository.ImportRunRepository;
import com.youthguide.api.global.ingest.repository.SourceRecordRepository;
import com.youthguide.api.global.ingest.service.ImportRunRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateSyncService {

    public static final String SOURCE_KEY = "QNET_NATIONAL_QUALIFICATION_LIST";
    private static final String SOURCE_NAME = "Q-Net 국가자격 종목 목록";
    public static final String CODE_NAMESPACE = "QNET_JMCD";
    private static final String OPERATION_KEY = "getList";
    private static final String ADAPTER_VERSION = "qnet-cert-list/1";
    private static final String REVIEWER = "system:qnet-sync";

    private final QnetApiClient client;
    private final QnetApiProperties props;
    private final ImportRunRecorder recorder;
    private final DataSourceRepository dataSourceRepository;
    private final ImportRunRepository importRunRepository;
    private final SourceRecordRepository sourceRecordRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateSourceKeyRepository sourceKeyRepository;
    private final TransactionTemplate tx;
    private final ObjectMapper objectMapper;

    public SyncResult sync() {
        DataSource source = recorder.getOrCreateSource(
                SOURCE_KEY, SOURCE_NAME, props.certificateList().baseUrl());

        String requestContext = "{\"numOfRows\":" + QnetApiClient.PAGE_SIZE + ",\"_type\":\"json\"}";
        Long runId = recorder.start(source.getId(), OPERATION_KEY, requestContext, ADAPTER_VERSION);

        FetchResult<CertificateItem> fetched = null;
        try {
            fetched = client.fetchAllCertificates();

            final FetchResult<CertificateItem> data = fetched;
            SyncResult result = tx.execute(status -> persist(source.getId(), runId, data));

            recorder.finish(runId, ImportRun.SUCCEEDED, toLong(fetched.totalCount()), fetched.items().size());
            log.info("[자격증 동기화] {}", result);
            return result;

        } catch (RuntimeException e) {
            long received = fetched == null ? 0 : fetched.items().size();
            Long expected = fetched == null ? null : toLong(fetched.totalCount());
            recorder.finish(runId, ImportRun.FAILED, expected, received);
            throw e;
        }
    }

    private SyncResult persist(Long sourceId, Long runId, FetchResult<CertificateItem> data) {
        DataSource source = dataSourceRepository.getReferenceById(sourceId);
        ImportRun run = importRunRepository.getReferenceById(runId);

        Map<String, CertificateSourceKey> existing = sourceKeyRepository
                .findAllWithCertificate(sourceId, CODE_NAMESPACE).stream()
                .collect(Collectors.toMap(CertificateSourceKey::getExternalCode, Function.identity()));

        int inserted = 0, renamed = 0, unchanged = 0;

        for (PagedItem<CertificateItem> p : data.items()) {
            CertificateItem item = p.item();

            String payload = objectMapper.writeValueAsString(item);
            SourceRecord record = sourceRecordRepository.save(SourceRecord.of(
                    run, "pageNo=" + p.pageNo(), p.ordinal(),
                    payload, "application/json", sha256(payload)));

            String jmcd = trim(item.jmcd());
            String name = trim(item.jmfldnm());
            if (jmcd == null || name == null) continue;

            CertificateSourceKey key = existing.get(jmcd);
            if (key != null) {
                if (key.getCertificate().rename(name)) renamed++;
                else unchanged++;
            } else {
                Certificate cert = certificateRepository.save(Certificate.of(name));
                CertificateSourceKey newKey = sourceKeyRepository.save(
                        CertificateSourceKey.link(cert, source, CODE_NAMESPACE, jmcd, record,
                                REVIEWER,
                                "Q-Net 공식 종목코드 자동 매핑 (import_run=" + runId + ")"));
                existing.put(jmcd, newKey);
                inserted++;
            }
        }

        return new SyncResult(runId, data.items().size(), inserted, renamed, unchanged);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }

    private static Long toLong(Integer i) {
        return i == null ? null : i.longValue();
    }

    private static String sha256(String s) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}