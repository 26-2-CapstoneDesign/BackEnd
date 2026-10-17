package com.youthguide.api.home.repository.projection;

import java.time.LocalDate;

public interface CalendarEventProjection {

    Long getCertificateId();

    String getCertificateName();

    String getYearLabel();

    String getRoundLabel();

    String getEventKind();

    String getPhaseLabel();

    LocalDate getStartsOn();

    LocalDate getEndsOn();
}