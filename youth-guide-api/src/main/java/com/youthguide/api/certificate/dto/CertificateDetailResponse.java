package com.youthguide.api.certificate.dto;

import java.time.LocalDate;
import java.util.List;

public record CertificateDetailResponse(
        Long id,
        String name,
        List<Schedule> schedules
) {
    public record Schedule(String year, String round, String description, List<Event> events) {}

    public record Event(String kind, String label, String phase, LocalDate start, LocalDate end) {}
}