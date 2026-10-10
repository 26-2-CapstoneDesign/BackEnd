package com.youthguide.api.certificate.dto.qnet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExamScheduleItem(
        String implYy,
        Integer implSeq,
        String qualgbCd,
        String qualgbNm,
        String description,
        String docRegStartDt,
        String docRegEndDt,
        String docExamStartDt,
        String docExamEndDt,
        String docPassDt,
        String pracRegStartDt,
        String pracRegEndDt,
        String pracExamStartDt,
        String pracExamEndDt,
        String pracPassDt
) {}