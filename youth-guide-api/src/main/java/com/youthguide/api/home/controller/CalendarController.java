package com.youthguide.api.home.controller;

import com.youthguide.api.global.security.LoginMember;
import com.youthguide.api.home.dto.CalendarResponse;
import com.youthguide.api.home.service.CalendarService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1/home/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final CalendarService calendarService;

    @GetMapping
    public CalendarResponse getMonthlyCalendar(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestParam(required = false) @Min(2000) @Max(2100) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        YearMonth now = YearMonth.now(SEOUL);
        YearMonth target = YearMonth.of(
                year != null ? year : now.getYear(),
                month != null ? month : now.getMonthValue());
        return calendarService.getMonthlyCalendar(loginMember.getMemberId(), target);
    }
}