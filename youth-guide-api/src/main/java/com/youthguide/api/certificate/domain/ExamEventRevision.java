package com.youthguide.api.certificate.domain;

import com.youthguide.api.global.ingest.domain.SourceRecord;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "exam_event_revision")
@IdClass(ExamEventRevisionId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamEventRevision implements Persistable<ExamEventRevisionId> {

    @Id
    @Column(name = "exam_event_id")
    private Long examEventId;

    @Id
    @Column(name = "revision_no")
    private Long revisionNo;

    @Column(name = "starts_on")
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "publication_state", nullable = false, length = 32)
    private String publicationState;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_record_id", nullable = false)
    private SourceRecord evidenceRecord;

    @Column(name = "recorded_at", insertable = false, updatable = false)
    private LocalDateTime recordedAt;

    // 복합키를 직접 넣는 엔티티라 save() 때 불필요한 SELECT 안 하게 처리
    @Transient
    private boolean isNew = true;

    public static ExamEventRevision of(Long examEventId, long revisionNo, LocalDate startsOn,
                                       LocalDate endsOn, String publicationState, SourceRecord evidence) {
        ExamEventRevision r = new ExamEventRevision();
        r.examEventId = examEventId;
        r.revisionNo = revisionNo;
        r.startsOn = startsOn;
        r.endsOn = endsOn;
        r.publicationState = publicationState;
        r.evidenceRecord = evidence;
        return r;
    }

    public boolean sameDates(LocalDate start, LocalDate end) {
        return Objects.equals(startsOn, start) && Objects.equals(endsOn, end);
    }

    @Override
    public ExamEventRevisionId getId() {
        return new ExamEventRevisionId(examEventId, revisionNo);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }
}