package com.youthguide.api.certificate.dto.qnet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QnetResponse<T>(Response<T> response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response<T>(Header header, Body<T> body) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body<T>(Items<T> items, Integer totalCount, Integer numOfRows, Integer pageNo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items<T>(
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<T> item
    ) {}

    public List<T> items() {
        if (response == null || response.body() == null
                || response.body().items() == null || response.body().items().item() == null) {
            return List.of();
        }
        return response.body().items().item();
    }

    public boolean isSuccess() {
        return response != null && response.header() != null
                && "00".equals(response.header().resultCode());
    }
}