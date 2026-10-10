package com.youthguide.api.certificate.domain;

import com.youthguide.api.certificate.dto.qnet.ExamScheduleItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "exam_schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exam_schedule", columnNames = {"impl_yy", "impl_seq", "qual_type_cd"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamSchedule {

    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "impl_yy", nullable = false)
    private Integer implYy;

    @Column(name = "impl_seq", nullable = false)
    private Integer implSeq;

    @Column(name = "qual_type_cd", nullable = false, length = 5)
    private String qualTypeCd;

    @Column(length = 200)
    private String description;

    private LocalDate docRegStart;
    private LocalDate docRegEnd;
    private LocalDate docExamStart;
    private LocalDate docExamEnd;
    private LocalDate docPass;
    private LocalDate pracRegStart;
    private LocalDate pracRegEnd;
    private LocalDate pracExamStart;
    private LocalDate pracExamEnd;
    private LocalDate pracPass;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    public static ExamSchedule from(ExamScheduleItem item) {
        ExamSchedule e = new ExamSchedule();
        e.implYy = Integer.valueOf(item.implYy());
        e.implSeq = Integer.valueOf(item.implSeq());
        e.qualTypeCd = item.qualgbCd();
        e.apply(item);
        return e;
    }

    public void apply(ExamScheduleItem item) {
        this.description = item.description();
        this.docRegStart = date(item.docRegStartDt());
        this.docRegEnd = date(item.docRegEndDt());
        this.docExamStart = date(item.docExamStartDt());
        this.docExamEnd = date(item.docExamEndDt());
        this.docPass = date(item.docPassDt());
        this.pracRegStart = date(item.pracRegStartDt());
        this.pracRegEnd = date(item.pracRegEndDt());
        this.pracExamStart = date(item.pracExamStartDt());
        this.pracExamEnd = date(item.pracExamEndDt());
        this.pracPass = date(item.pracPassDt());
        this.syncedAt = LocalDateTime.now();
    }

    private static LocalDate date(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s.trim().substring(0, 8), YMD);
    }
}