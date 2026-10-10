package com.youthguide.api.global.ingest.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "import_run")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportRun {

    public static final String RUNNING = "RUNNING";
    public static final String SUCCEEDED = "SUCCEEDED";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_run_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private DataSource source;

    @Column(name = "operation_key", nullable = false, length = 128)
    private String operationKey;

    @Column(name = "request_context", nullable = false, columnDefinition = "longtext")
    private String requestContext;

    @Column(name = "adapter_version", nullable = false, length = 64)
    private String adapterVersion;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(nullable = false, length = 32)
    private String outcome;

    @Column(name = "expected_count")
    private Long expectedCount;

    @Column(name = "received_count", nullable = false)
    private long receivedCount;

    public static ImportRun start(DataSource source, String operationKey,
                                  String requestContext, String adapterVersion) {
        ImportRun r = new ImportRun();
        r.source = source;
        r.operationKey = operationKey;
        r.requestContext = requestContext;
        r.adapterVersion = adapterVersion;
        r.startedAt = LocalDateTime.now(ZoneOffset.UTC);
        r.outcome = RUNNING;
        return r;
    }

    public void finish(String outcome, Long expectedCount, long receivedCount) {
        this.outcome = outcome;
        this.expectedCount = expectedCount;
        this.receivedCount = receivedCount;
        this.finishedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}