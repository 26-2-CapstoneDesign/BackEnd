package com.youthguide.api.home.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(
        int year,
        int month,
        List<Event> events
) {
    public record Event(
            Long certificateId,
            String certificateName,
            String year,
            String round,
            String kind,
            String label,
            String phase,
            LocalDate start,
            LocalDate end
    ) {}
}