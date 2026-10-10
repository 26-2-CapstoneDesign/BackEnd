package com.youthguide.api.certificate.dto.qnet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** 시험일정 API는 response 래퍼 없이 header/body가 바로 옴 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExamScheduleResponse(Header header, Body body) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<ExamScheduleItem> items,
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount
    ) {}

    public List<ExamScheduleItem> items() {
        if (body == null || body.items() == null) return List.of();
        return body.items();
    }

    public boolean isSuccess() {
        return header != null && "00".equals(header.resultCode());
    }
}