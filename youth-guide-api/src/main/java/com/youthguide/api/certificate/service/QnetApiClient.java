package com.youthguide.api.certificate.service;

import com.youthguide.api.certificate.dto.qnet.CertificateItem;
import com.youthguide.api.certificate.dto.qnet.ExamScheduleItem;
import com.youthguide.api.certificate.dto.qnet.ExamScheduleResponse;
import com.youthguide.api.certificate.dto.qnet.QnetResponse;
import com.youthguide.api.global.config.QnetApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QnetApiClient {

    public static final int PAGE_SIZE = 500;
    private static final int SCHEDULE_PAGE_SIZE = 50;

    private final QnetApiProperties props;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public record PagedItem<T>(int pageNo, int ordinal, T item) {}

    public record FetchResult<T>(List<PagedItem<T>> items, Integer totalCount) {}

    // ───────── 자격증 목록 ─────────

    public FetchResult<CertificateItem> fetchAllCertificates() {
        return fetchAll(props.certificateList(),
                new ParameterizedTypeReference<QnetResponse<CertificateItem>>() {});
    }

    private <T> FetchResult<T> fetchAll(QnetApiProperties.Endpoint api,
                                        ParameterizedTypeReference<QnetResponse<T>> type) {
        List<PagedItem<T>> result = new ArrayList<>();
        Integer total = null;
        int page = 1;

        while (true) {
            final int pageNo = page;
            QnetResponse<T> res = restClient.get()
                    .uri(api.baseUrl(), b -> b
                            .queryParam("serviceKey", api.key())
                            .queryParam("_type", "json")
                            .queryParam("numOfRows", PAGE_SIZE)
                            .queryParam("pageNo", pageNo)
                            .build())
                    .retrieve()
                    .body(type);

            if (res == null || !res.isSuccess()) {
                var header = res == null || res.response() == null ? null : res.response().header();
                throw new IllegalStateException("Q-Net API 호출 실패: " + header);
            }

            List<T> items = res.items();
            for (int i = 0; i < items.size(); i++) {
                result.add(new PagedItem<>(pageNo, i, items.get(i)));
            }

            total = res.response().body().totalCount();
            if (items.isEmpty() || total == null || result.size() >= total) break;
            page++;
        }

        log.info("[Q-Net] {} → {}건 수집 (totalCount={})", api.baseUrl(), result.size(), total);
        return new FetchResult<>(result, total);
    }

    // ───────── 시험일정 ─────────

    public FetchResult<ExamScheduleItem> fetchExamSchedules(int year) {
        var api = props.examSchedule();
        List<PagedItem<ExamScheduleItem>> result = new ArrayList<>();
        Integer total = null;
        int page = 1;

        while (true) {
            final int pageNo = page;
            // content-type이 애매하게 올 수 있어서 문자열로 받고 직접 파싱
            String raw = restClient.get()
                    .uri(api.baseUrl(), b -> b
                            .queryParam("serviceKey", api.key())
                            .queryParam("dataFormat", "json")
                            .queryParam("numOfRows", SCHEDULE_PAGE_SIZE)
                            .queryParam("pageNo", pageNo)
                            .queryParam("implYy", year)
                            .build())
                    .retrieve()
                    .body(String.class);

            ExamScheduleResponse res = objectMapper.readValue(raw, ExamScheduleResponse.class);
            if (res == null || !res.isSuccess()) {
                throw new IllegalStateException("시험일정 API 호출 실패: " + (res == null ? raw : res.header()));
            }

            List<ExamScheduleItem> items = res.items();
            for (int i = 0; i < items.size(); i++) {
                result.add(new PagedItem<>(pageNo, i, items.get(i)));
            }

            total = res.body().totalCount();
            if (items.isEmpty() || total == null || result.size() >= total) break;
            page++;
        }

        log.info("[Q-Net] 시험일정 {}년 → {}건 수집 (totalCount={})", year, result.size(), total);
        return new FetchResult<>(result, total);
    }
}