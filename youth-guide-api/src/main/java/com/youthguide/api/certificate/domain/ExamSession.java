package com.youthguide.api.certificate.domain;

import com.youthguide.api.global.ingest.domain.SourceRecord;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @Column(name = "year_label", length = 32)
    private String yearLabel;

    @Column(name = "round_label", length = 64)
    private String roundLabel;

    @Column(name = "qualification_route", length = 128)
    private String qualificationRoute;

    @Column(name = "display_name")
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_record_id", nullable = false)
    private SourceRecord evidenceRecord;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ExamSession of(Certificate certificate, String yearLabel, String roundLabel,
                                 String displayName, SourceRecord evidence) {
        ExamSession s = new ExamSession();
        s.certificate = certificate;
        s.yearLabel = yearLabel;
        s.roundLabel = roundLabel;
        s.displayName = displayName;
        s.evidenceRecord = evidence;
        return s;
    }
}