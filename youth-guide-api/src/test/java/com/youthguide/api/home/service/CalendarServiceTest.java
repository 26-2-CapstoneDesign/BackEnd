package com.youthguide.api.home.service;

import com.youthguide.api.home.dto.CalendarResponse;
import com.youthguide.api.home.repository.CalendarQueryRepository;
import com.youthguide.api.home.repository.projection.CalendarEventProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarQueryRepository calendarQueryRepository;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void queriesWholeMonthAndMapsLabels() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);
        when(calendarQueryRepository.findEventsInRange(7L, start, end)).thenReturn(List.of(
                row("PRACTICAL_EXAM", LocalDate.of(2026, 11, 7), LocalDate.of(2026, 11, 20)),
                row("WRITTEN_RESULT", LocalDate.of(2026, 11, 25), null)));

        CalendarResponse response = calendarService.getMonthlyCalendar(7L, YearMonth.of(2026, 11));

        verify(calendarQueryRepository).findEventsInRange(7L, start, end);
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(11);
        assertThat(response.events()).hasSize(2);
        assertThat(response.events().get(0).label()).isEqualTo("실기시험");
        assertThat(response.events().get(1).label()).isEqualTo("필기 합격발표");
        assertThat(response.events().get(1).end()).isEqualTo(LocalDate.of(2026, 11, 25));
    }

    @Test
    void returnsEmptyEventsWhenNoInterest() {
        when(calendarQueryRepository.findEventsInRange(7L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)))
                .thenReturn(List.of());

        CalendarResponse response = calendarService.getMonthlyCalendar(7L, YearMonth.of(2026, 2));

        assertThat(response.events()).isEmpty();
    }

    private static CalendarEventProjection row(String kind, LocalDate startsOn, LocalDate endsOn) {
        return new CalendarEventProjection() {
            public Long getCertificateId() { return 12L; }
            public String getCertificateName() { return "정보처리기사"; }
            public String getYearLabel() { return "2026"; }
            public String getRoundLabel() { return "제3회"; }
            public String getEventKind() { return kind; }
            public String getPhaseLabel() { return "실기"; }
            public LocalDate getStartsOn() { return startsOn; }
            public LocalDate getEndsOn() { return endsOn; }
        };
    }
}