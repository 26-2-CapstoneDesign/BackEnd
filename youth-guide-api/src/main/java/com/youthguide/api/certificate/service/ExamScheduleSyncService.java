package com.youthguide.api.certificate.service;

import com.youthguide.api.certificate.domain.*;
import com.youthguide.api.certificate.dto.ExamSyncResult;
import com.youthguide.api.certificate.dto.qnet.CertificateItem;
import com.youthguide.api.certificate.dto.qnet.ExamScheduleItem;
import com.youthguide.api.certificate.repository.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamScheduleSyncService {

    private static final String SOURCE_KEY = "QNET_NATIONAL_EXAM_SCHEDULE";
    private static final String SOURCE_NAME = "Q-Net 국가자격 시험일정";
    private static final String OPERATION_KEY = "getQualExamSchdList";
    private static final String ADAPTER_VERSION = "qnet-exam-schd/1";
    private static final String TECHNICAL = "T";          // 국가기술자격
    private static final String PUBLISHED = "PUBLISHED";

    // "산업기사"가 "기사"보다 먼저 와야 함
    private static final List<String> GRADES = List.of("기술사", "기능장", "산업기사", "기능사", "기사");
    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    private final QnetApiClient client;
    private final QnetApiProperties props;
    private final ImportRunRecorder recorder;
    private final DataSourceRepository dataSourceRepository;
    private final ImportRunRepository importRunRepository;
    private final SourceRecordRepository sourceRecordRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateSourceKeyRepository sourceKeyRepository;
    private final ExamSessionRepository sessionRepository;
    private final ExamEventRepository eventRepository;
    private final ExamEventRevisionRepository revisionRepository;
    private final TransactionTemplate tx;
    private final ObjectMapper objectMapper;

    private record Round(ExamScheduleItem item, int pageNo, int ordinal, String grade,
                         ExamScheduleItem additional) {}

    private record EventSpec(String kind, String phase, LocalDate start, LocalDate end) {}

    private record Counts(int sessions, int events, int revisions, int unchanged) {}

    public ExamSyncResult sync(int year, boolean dryRun) {
        // 1) 등급별 자격증 목록 만들기 (jmcd → 등급은 자격증 목록 API에서)
        DataSource certSource = dataSourceRepository.findBySourceKey(CertificateSyncService.SOURCE_KEY)
                .orElseThrow(() -> new IllegalStateException("자격증 동기화를 먼저 실행해야 함"));

        Map<String, String> gradeByJmcd = client.fetchAllCertificates().items().stream()
                .map(PagedItem::item)
                .filter(i -> i.jmcd() != null && i.seriesnm() != null)
                .collect(Collectors.toMap(i -> i.jmcd().strip(), i -> i.seriesnm().strip(), (a, b) -> a));

        Map<String, List<Long>> certIdsByGrade = new HashMap<>();
        for (CertificateSourceKey k : sourceKeyRepository.findAllWithCertificate(
                certSource.getId(), CertificateSyncService.CODE_NAMESPACE)) {
            String grade = gradeByJmcd.get(k.getExternalCode());
            if (grade != null) {
                certIdsByGrade.computeIfAbsent(grade, g -> new ArrayList<>()).add(k.getCertificate().getId());
            }
        }

        // 2) 시험일정 수집
        FetchResult<ExamScheduleItem> fetched = client.fetchExamSchedules(year);

        // 국가기술자격만, 완전히 같은 건 제거하고 설명별로 묶기
        Map<String, List<PagedItem<ExamScheduleItem>>> byDesc = new LinkedHashMap<>();
        Set<ExamScheduleItem> seen = new HashSet<>();
        for (PagedItem<ExamScheduleItem> p : fetched.items()) {
            ExamScheduleItem it = p.item();
            if (!TECHNICAL.equals(it.qualgbCd()) || !seen.add(it)) continue;
            byDesc.computeIfAbsent(it.description(), d -> new ArrayList<>()).add(p);
        }

        List<Round> rounds = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();

        for (var entry : byDesc.entrySet()) {
            ExamScheduleItem first = entry.getValue().get(0).item();
            String grade = resolveGrade(first.description());
            if (grade == null || !certIdsByGrade.containsKey(grade)) {
                unmatched.add(entry.getKey());
                continue;
            }
            // 기능사는 정기(1~4회)만
            if ("기능사".equals(grade) && (first.implSeq() == null || first.implSeq() < 1 || first.implSeq() > 4)) {
                continue;
            }

            // 시험 날짜가 같은 것끼리 묶어서 제일 많은 그룹 = 정식 회차
            List<PagedItem<ExamScheduleItem>> main = entry.getValue().stream()
                    .collect(Collectors.groupingBy(p -> examKey(p.item())))
                    .values().stream()
                    .max(Comparator.comparingInt(List::size))
                    .orElseThrow();

            if (main.size() < entry.getValue().size()) {
                log.info("[다른 시험 제외] {} → {}건 중 {}건 사용",
                        entry.getKey(), entry.getValue().size(), main.size());
            }

            // 접수 빠른 게 정기접수, 그다음이 추가접수
            main.sort(Comparator.comparing(p -> sortableDate(p.item().docRegStartDt())));
            PagedItem<ExamScheduleItem> regular = main.get(0);
            ExamScheduleItem additional = main.size() > 1 ? main.get(1).item() : null;

            rounds.add(new Round(regular.item(), regular.pageNo(), regular.ordinal(), grade, additional));
        }

        // 미리보기용 통계
        Map<String, Long> roundsByGrade = rounds.stream()
                .collect(Collectors.groupingBy(Round::grade, TreeMap::new, Collectors.counting()));
        Map<String, Integer> certsByGrade = new TreeMap<>();
        certIdsByGrade.forEach((g, ids) -> certsByGrade.put(g, ids.size()));
        long projected = rounds.stream().mapToLong(r -> certIdsByGrade.get(r.grade()).size()).sum();

        if (dryRun) {
            return new ExamSyncResult(true, null, fetched.items().size(), rounds.size(),
                    roundsByGrade, certsByGrade, projected, unmatched, 0, 0, 0, 0);
        }

        // 3) 실제 저장
        DataSource source = recorder.getOrCreateSource(SOURCE_KEY, SOURCE_NAME, props.examSchedule().baseUrl());
        String requestContext = "{\"implYy\":" + year + ",\"dataFormat\":\"json\"}";
        Long runId = recorder.start(source.getId(), OPERATION_KEY, requestContext, ADAPTER_VERSION);

        try {
            Counts c = tx.execute(s -> persist(runId, year, rounds, certIdsByGrade));
            recorder.finish(runId, ImportRun.SUCCEEDED, toLong(fetched.totalCount()), fetched.items().size());

            ExamSyncResult result = new ExamSyncResult(false, runId, fetched.items().size(), rounds.size(),
                    roundsByGrade, certsByGrade, projected, unmatched,
                    c.sessions(), c.events(), c.revisions(), c.unchanged());
            log.info("[시험일정 동기화] {}", result);
            return result;

        } catch (RuntimeException e) {
            recorder.finish(runId, ImportRun.FAILED, toLong(fetched.totalCount()), fetched.items().size());
            throw e;
        }
    }

    private Counts persist(Long runId, int year, List<Round> rounds, Map<String, List<Long>> certIdsByGrade) {
        ImportRun run = importRunRepository.getReferenceById(runId);
        String yearLabel = String.valueOf(year);

        // 기존 데이터 한 번에 로딩 (N+1 방지)
        List<ExamSession> existingSessions = sessionRepository.findAllByYearLabel(yearLabel);
        Map<String, ExamSession> sessionByKey = new HashMap<>();
        for (ExamSession s : existingSessions) {
            sessionByKey.put(sessionKey(s.getCertificate().getId(), s.getRoundLabel()), s);
        }

        Map<String, ExamEvent> eventByKey = new HashMap<>();
        if (!existingSessions.isEmpty()) {
            for (ExamEvent e : eventRepository.findAllByExamSessionIn(existingSessions)) {
                eventByKey.put(eventKey(e.getExamSession().getId(), e.getEventKind()), e);
            }
        }

        Map<Long, ExamEventRevision> latest = new HashMap<>();
        if (!eventByKey.isEmpty()) {
            List<Long> eventIds = eventByKey.values().stream().map(ExamEvent::getId).toList();
            for (ExamEventRevision r : revisionRepository.findAllByExamEventIdIn(eventIds)) {
                latest.merge(r.getExamEventId(), r,
                        (a, b) -> a.getRevisionNo() >= b.getRevisionNo() ? a : b);
            }
        }

        int sessionsCreated = 0, eventsCreated = 0, revisionsAdded = 0, unchanged = 0;

        for (Round round : rounds) {
            ExamScheduleItem it = round.item();

            String payload = objectMapper.writeValueAsString(it);
            SourceRecord record = sourceRecordRepository.save(SourceRecord.of(
                    run, "pageNo=" + round.pageNo(), round.ordinal(),
                    payload, "application/json", sha256(payload)));

            String roundLabel = "제" + it.implSeq() + "회";
            List<EventSpec> specs = eventSpecs(it, round.additional());

            for (Long certId : certIdsByGrade.get(round.grade())) {
                String sk = sessionKey(certId, roundLabel);
                ExamSession session = sessionByKey.get(sk);
                if (session == null) {
                    session = sessionRepository.save(ExamSession.of(
                            certificateRepository.getReferenceById(certId),
                            yearLabel, roundLabel, it.description(), record));
                    sessionByKey.put(sk, session);
                    sessionsCreated++;
                }

                for (EventSpec spec : specs) {
                    String ek = eventKey(session.getId(), spec.kind());
                    ExamEvent event = eventByKey.get(ek);
                    if (event == null) {
                        event = eventRepository.save(ExamEvent.of(session, spec.kind(), spec.phase()));
                        eventByKey.put(ek, event);
                        eventsCreated++;
                    }

                    ExamEventRevision last = latest.get(event.getId());
                    if (last != null && last.sameDates(spec.start(), spec.end())) {
                        unchanged++;
                        continue;
                    }

                    long nextNo = last == null ? 1 : last.getRevisionNo() + 1;
                    ExamEventRevision rev = revisionRepository.save(ExamEventRevision.of(
                            event.getId(), nextNo, spec.start(), spec.end(), PUBLISHED, record));
                    latest.put(event.getId(), rev);
                    revisionsAdded++;
                }
            }
        }

        return new Counts(sessionsCreated, eventsCreated, revisionsAdded, unchanged);
    }

    private static List<EventSpec> eventSpecs(ExamScheduleItem it, ExamScheduleItem additional) {
        List<EventSpec> specs = new ArrayList<>();
        add(specs, "WRITTEN_REGISTRATION", "필기", it.docRegStartDt(), it.docRegEndDt());
        if (additional != null) {
            add(specs, "WRITTEN_ADDITIONAL_REGISTRATION", "필기",
                    additional.docRegStartDt(), additional.docRegEndDt());
        }
        add(specs, "WRITTEN_EXAM", "필기", it.docExamStartDt(), it.docExamEndDt());
        add(specs, "WRITTEN_RESULT", "필기", it.docPassDt(), it.docPassDt());
        add(specs, "PRACTICAL_REGISTRATION", "실기", it.pracRegStartDt(), it.pracRegEndDt());
        add(specs, "PRACTICAL_EXAM", "실기", it.pracExamStartDt(), it.pracExamEndDt());
        add(specs, "FINAL_RESULT", "실기", it.pracPassDt(), it.pracPassDt());
        return specs;
    }

    /** 시험 날짜 묶음 (접수일 빼고) */
    private static String examKey(ExamScheduleItem it) {
        return String.join("|", nz(it.docExamStartDt()), nz(it.docExamEndDt()),
                nz(it.pracExamStartDt()), nz(it.pracExamEndDt()), nz(it.pracPassDt()));
    }

    /** 빈 날짜는 맨 뒤로 정렬되게 */
    private static String sortableDate(String s) {
        return (s == null || s.isBlank()) ? "99999999" : s.strip();
    }

    private static String nz(String s) {
        return s == null ? "" : s.strip();
    }

    private static void add(List<EventSpec> specs, String kind, String phase, String start, String end) {
        LocalDate s = date(start);
        if (s == null) return;
        LocalDate e = date(end);
        if (e != null && e.isBefore(s)) e = s;   // ck_exam_event_revision_date_order 방지
        specs.add(new EventSpec(kind, phase, s, e));
    }

    private static LocalDate date(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s.strip().substring(0, 8), YMD);
    }

    private static String resolveGrade(String description) {
        if (description == null) return null;
        for (String g : GRADES) {
            if (description.contains(g)) return g;
        }
        return null;
    }

    private static String sessionKey(Long certId, String roundLabel) {
        return certId + "|" + roundLabel;
    }

    private static String eventKey(Long sessionId, String kind) {
        return sessionId + "|" + kind;
    }

    private static Long toLong(Integer i) {
        return i == null ? null : i.longValue();
    }

    private static String sha256(String s) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}