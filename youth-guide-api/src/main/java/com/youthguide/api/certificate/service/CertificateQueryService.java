package com.youthguide.api.certificate.service;

import com.youthguide.api.certificate.domain.Certificate;
import com.youthguide.api.certificate.domain.ExamEvent;
import com.youthguide.api.certificate.domain.ExamEventRevision;
import com.youthguide.api.certificate.domain.ExamSession;
import com.youthguide.api.certificate.dto.CertificateDetailResponse;
import com.youthguide.api.certificate.dto.CertificateDetailResponse.Event;
import com.youthguide.api.certificate.dto.CertificateDetailResponse.Schedule;
import com.youthguide.api.certificate.repository.CertificateRepository;
import com.youthguide.api.certificate.repository.ExamEventRepository;
import com.youthguide.api.certificate.repository.ExamEventRevisionRepository;
import com.youthguide.api.certificate.repository.ExamSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateQueryService {

    // 화면에 보여줄 순서 + 한글 이름
    private static final List<String> EVENT_ORDER = List.of(
            "WRITTEN_REGISTRATION", "WRITTEN_ADDITIONAL_REGISTRATION", "WRITTEN_EXAM",
            "WRITTEN_RESULT", "PRACTICAL_REGISTRATION", "PRACTICAL_EXAM", "FINAL_RESULT");

    public static final Map<String, String> EVENT_LABEL = Map.of(
            "WRITTEN_REGISTRATION", "필기 원서접수",
            "WRITTEN_ADDITIONAL_REGISTRATION", "필기 추가접수",
            "WRITTEN_EXAM", "필기시험",
            "WRITTEN_RESULT", "필기 합격발표",
            "PRACTICAL_REGISTRATION", "실기 원서접수",
            "PRACTICAL_EXAM", "실기시험",
            "FINAL_RESULT", "최종 합격발표");

    private final CertificateRepository certificateRepository;
    private final ExamSessionRepository sessionRepository;
    private final ExamEventRepository eventRepository;
    private final ExamEventRevisionRepository revisionRepository;

    public CertificateDetailResponse getDetail(Long certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "자격증을 찾을 수 없음"));

        List<ExamSession> sessions = sessionRepository.findAllByCertificate_Id(certificateId);
        if (sessions.isEmpty()) {
            return new CertificateDetailResponse(cert.getId(), cert.getDisplayName(), List.of());
        }

        // 회차별 이벤트
        Map<Long, List<ExamEvent>> eventsBySession = eventRepository.findAllByExamSessionIn(sessions).stream()
                .collect(Collectors.groupingBy(e -> e.getExamSession().getId()));

        // 이벤트별 최신 날짜 (revision_no 제일 큰 것)
        List<Long> eventIds = eventsBySession.values().stream().flatMap(List::stream).map(ExamEvent::getId).toList();
        Map<Long, ExamEventRevision> latest = new HashMap<>();
        for (ExamEventRevision r : revisionRepository.findAllByExamEventIdIn(eventIds)) {
            latest.merge(r.getExamEventId(), r, (a, b) -> a.getRevisionNo() >= b.getRevisionNo() ? a : b);
        }

        List<Schedule> schedules = sessions.stream()
                .sorted(Comparator.comparing(ExamSession::getYearLabel)
                        .thenComparingInt(s -> roundNumber(s.getRoundLabel())))
                .map(s -> new Schedule(
                        s.getYearLabel(),
                        s.getRoundLabel(),
                        s.getDisplayName(),
                        eventsBySession.getOrDefault(s.getId(), List.of()).stream()
                                .sorted(Comparator.comparingInt(e -> EVENT_ORDER.indexOf(e.getEventKind())))
                                .map(e -> {
                                    ExamEventRevision r = latest.get(e.getId());
                                    return new Event(
                                            e.getEventKind(),
                                            EVENT_LABEL.getOrDefault(e.getEventKind(), e.getEventKind()),
                                            e.getPhaseLabel(),
                                            r == null ? null : r.getStartsOn(),
                                            r == null ? null : r.getEndsOn());
                                })
                                .toList()))
                .toList();

        return new CertificateDetailResponse(cert.getId(), cert.getDisplayName(), schedules);
    }

    /** "제12회" → 12 (문자열 정렬하면 제10회가 제2회보다 앞에 와서) */
    private static int roundNumber(String label) {
        if (label == null) return Integer.MAX_VALUE;
        String digits = label.replaceAll("\\D", "");
        return digits.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(digits);
    }
}