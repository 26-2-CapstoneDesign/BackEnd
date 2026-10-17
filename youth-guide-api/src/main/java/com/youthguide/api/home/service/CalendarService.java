package com.youthguide.api.home.service;

import com.youthguide.api.certificate.service.CertificateQueryService;
import com.youthguide.api.home.dto.CalendarResponse;
import com.youthguide.api.home.dto.CalendarResponse.Event;
import com.youthguide.api.home.repository.CalendarQueryRepository;
import com.youthguide.api.home.repository.projection.CalendarEventProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarQueryRepository calendarQueryRepository;

    public CalendarResponse getMonthlyCalendar(Long memberId, YearMonth yearMonth) {
        LocalDate rangeStart = yearMonth.atDay(1);
        LocalDate rangeEnd = yearMonth.atEndOfMonth();

        List<Event> events = calendarQueryRepository.findEventsInRange(memberId, rangeStart, rangeEnd).stream()
                .map(CalendarService::toEvent)
                .toList();

        return new CalendarResponse(yearMonth.getYear(), yearMonth.getMonthValue(), events);
    }

    private static Event toEvent(CalendarEventProjection row) {
        LocalDate start = row.getStartsOn();
        LocalDate end = row.getEndsOn() != null ? row.getEndsOn() : start;
        return new Event(
                row.getCertificateId(),
                row.getCertificateName(),
                row.getYearLabel(),
                row.getRoundLabel(),
                row.getEventKind(),
                CertificateQueryService.EVENT_LABEL.getOrDefault(row.getEventKind(), row.getEventKind()),
                row.getPhaseLabel(),
                start,
                end
        );
    }
}