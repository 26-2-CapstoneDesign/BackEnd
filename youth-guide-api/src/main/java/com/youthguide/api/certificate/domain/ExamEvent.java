package com.youthguide.api.certificate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @Column(name = "event_kind", nullable = false, length = 64)
    private String eventKind;

    @Column(name = "phase_label", length = 64)
    private String phaseLabel;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ExamEvent of(ExamSession session, String eventKind, String phaseLabel) {
        ExamEvent e = new ExamEvent();
        e.examSession = session;
        e.eventKind = eventKind;
        e.phaseLabel = phaseLabel;
        return e;
    }
}