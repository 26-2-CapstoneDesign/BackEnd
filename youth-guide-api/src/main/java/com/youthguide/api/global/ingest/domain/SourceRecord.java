package com.youthguide.api.global.ingest.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "source_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "source_record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_run_id", nullable = false)
    private ImportRun importRun;

    @Column(name = "page_locator", nullable = false)
    private String pageLocator;

    @Column(name = "item_ordinal", nullable = false)
    private long itemOrdinal;

    @Column(name = "external_locator", columnDefinition = "text")
    private String externalLocator;

    @Column(nullable = false, columnDefinition = "longtext")
    private String payload;

    @Column(name = "media_type", nullable = false, length = 128)
    private String mediaType;

    @Column(name = "content_digest", nullable = false)
    private String contentDigest;

    @Column(name = "observed_at", insertable = false, updatable = false)
    private LocalDateTime observedAt;

    public static SourceRecord of(ImportRun run, String pageLocator, long itemOrdinal,
                                  String payload, String mediaType, String contentDigest) {
        SourceRecord r = new SourceRecord();
        r.importRun = run;
        r.pageLocator = pageLocator;
        r.itemOrdinal = itemOrdinal;
        r.payload = payload;
        r.mediaType = mediaType;
        r.contentDigest = contentDigest;
        return r;
    }
}