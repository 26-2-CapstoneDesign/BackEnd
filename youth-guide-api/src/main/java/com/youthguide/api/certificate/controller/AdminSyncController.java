package com.youthguide.api.certificate.controller;

import com.youthguide.api.certificate.dto.ExamSyncResult;
import com.youthguide.api.certificate.dto.SyncResult;
import com.youthguide.api.certificate.service.CertificateSyncService;
import com.youthguide.api.certificate.service.ExamScheduleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
@RequestMapping("/api/v1/dev/sync")
@RequiredArgsConstructor
public class AdminSyncController {

    private final CertificateSyncService certificateSyncService;
    private final ExamScheduleSyncService examScheduleSyncService;

    @PostMapping("/certificates")
    public SyncResult syncCertificates() {
        return certificateSyncService.sync();
    }

    /** 기본은 dryRun=true (저장 안 하고 개수만 확인) */
    @PostMapping("/exam-schedules")
    public ExamSyncResult syncExamSchedules(
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "true") boolean dryRun) {
        return examScheduleSyncService.sync(year != null ? year : Year.now().getValue(), dryRun);
    }
}