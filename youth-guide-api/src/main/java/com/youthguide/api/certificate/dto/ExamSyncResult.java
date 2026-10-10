package com.youthguide.api.certificate.dto;

import java.util.List;
import java.util.Map;

public record ExamSyncResult(
        boolean dryRun,
        Long importRunId,
        int fetched,                          // API에서 받은 전체 건수
        int technicalRounds,                  // 그중 국가기술자격(T) + 등급 매칭된 회차
        Map<String, Long> roundsByGrade,      // 등급별 회차 수
        Map<String, Integer> certificatesByGrade, // 등급별 자격증 수
        long projectedSessions,               // 만들어질 exam_session 예상 수
        List<String> unmatched,               // 등급 못 찾은 회차 설명
        int sessionsCreated,
        int eventsCreated,
        int revisionsAdded,
        int unchanged
) {}