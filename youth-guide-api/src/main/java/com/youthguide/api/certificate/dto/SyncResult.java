package com.youthguide.api.certificate.dto;

public record SyncResult(
        Long importRunId,
        int fetched,
        int inserted,
        int renamed,
        int unchanged
) {}