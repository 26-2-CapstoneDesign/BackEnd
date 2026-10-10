package com.youthguide.api.certificate.domain;

import com.youthguide.api.global.ingest.domain.DataSource;
import com.youthguide.api.global.ingest.domain.SourceRecord;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_source_key")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CertificateSourceKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_source_key_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private DataSource source;

    @Column(name = "code_namespace", nullable = false, length = 128)
    private String codeNamespace;

    @Column(name = "external_code", nullable = false)
    private String externalCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_record_id", nullable = false)
    private SourceRecord evidenceRecord;

    @Column(name = "linked_at", insertable = false, updatable = false)
    private LocalDateTime linkedAt;

    @Column(name = "reviewed_by", length = 128)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", columnDefinition = "text")
    private String reviewNote;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    public static CertificateSourceKey link(Certificate certificate, DataSource source,
                                            String codeNamespace, String externalCode,
                                            SourceRecord evidence,
                                            String reviewedBy, String reviewNote) {
        CertificateSourceKey k = new CertificateSourceKey();
        k.certificate = certificate;
        k.source = source;
        k.codeNamespace = codeNamespace;
        k.externalCode = externalCode;
        k.evidenceRecord = evidence;
        k.reviewedBy = reviewedBy;
        k.reviewedAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
        k.reviewNote = reviewNote;
        k.active = true;
        return k;
    }
}