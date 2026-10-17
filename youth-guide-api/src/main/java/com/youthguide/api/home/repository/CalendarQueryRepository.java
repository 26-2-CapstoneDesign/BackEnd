package com.youthguide.api.home.repository;

import com.youthguide.api.home.domain.MemberCertificateInterest;
import com.youthguide.api.home.domain.MemberCertificateInterestId;
import com.youthguide.api.home.repository.projection.CalendarEventProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarQueryRepository extends Repository<MemberCertificateInterest, MemberCertificateInterestId> {

    @Query(
            value = """
                    SELECT
                        c.certificate_id AS certificateId,
                        c.display_name AS certificateName,
                        s.year_label AS yearLabel,
                        s.round_label AS roundLabel,
                        e.event_kind AS eventKind,
                        e.phase_label AS phaseLabel,
                        r.starts_on AS startsOn,
                        r.ends_on AS endsOn
                    FROM member_certificate_interest i
                    JOIN certificate c
                        ON c.certificate_id = i.certificate_id
                    JOIN exam_session s
                        ON s.certificate_id = c.certificate_id
                    JOIN exam_event e
                        ON e.exam_session_id = s.exam_session_id
                    JOIN exam_event_revision r
                        ON r.exam_event_id = e.exam_event_id
                    WHERE i.member_id = :memberId
                      AND NOT EXISTS (
                          SELECT 1
                          FROM member_exam_session_exclusion x
                          WHERE x.member_id = i.member_id
                            AND x.exam_session_id = s.exam_session_id
                      )
                      AND r.revision_no = (
                          SELECT MAX(latest.revision_no)
                          FROM exam_event_revision latest
                          WHERE latest.exam_event_id = e.exam_event_id
                      )
                      AND r.starts_on <= :rangeEnd
                      AND COALESCE(r.ends_on, r.starts_on) >= :rangeStart
                    ORDER BY r.starts_on ASC, c.display_name ASC, e.exam_event_id ASC
                    """,
            nativeQuery = true
    )
    List<CalendarEventProjection> findEventsInRange(
            @Param("memberId") Long memberId,
            @Param("rangeStart") LocalDate rangeStart,
            @Param("rangeEnd") LocalDate rangeEnd
    );
}