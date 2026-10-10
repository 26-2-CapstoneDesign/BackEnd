package com.youthguide.api.certificate.service;

import com.youthguide.api.global.config.QnetApiProperties;
import com.youthguide.api.global.config.QnetApiProperties.Endpoint;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class QnetApiService {

    private final QnetApiProperties props;
    private final RestClient restClient;

    public QnetApiService(QnetApiProperties props, RestClient restClient) {
        this.props = props;
        this.restClient = restClient;
    }

    public String fetchCertificateList(int pageNo, int numOfRows) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(numOfRows));
        return get("certificate-list", props.certificateList(), params);
    }

    public String fetchExamSchedule(String implYy, String qualgbCd, String jmCd,
                                    int pageNo, int numOfRows) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("dataFormat", "json");
        params.put("implYy", implYy);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(numOfRows));
        if (hasText(qualgbCd)) params.put("qualgbCd", qualgbCd);
        if (hasText(jmCd)) params.put("jmCd", jmCd);
        return get("exam-schedule", props.examSchedule(), params);
    }

    private String get(String name, Endpoint endpoint, Map<String, String> params) {
        validate(name, endpoint);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint.baseUrl())
                .queryParam("serviceKey", encodeKey(endpoint.key()));
        params.forEach((k, v) -> builder.queryParam(k, encode(v)));
        URI uri = builder.build(true).toUri();

        try {
            byte[] bytes = restClient.get().uri(uri).retrieve().body(byte[].class);
            return bytes == null ? "" : new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Q-Net API 호출 실패(" + name + "): HTTP " + e.getStatusCode().value(), e);
        }
    }

    private void validate(String name, Endpoint endpoint) {
        if (endpoint == null
                || !hasText(endpoint.baseUrl()) || endpoint.baseUrl().contains("<")
                || !hasText(endpoint.key()) || endpoint.key().contains("<")) {
            throw new IllegalStateException(
                    "qnet.api." + name + " 설정(base-url/key)이 비었거나 자리표시자 상태야");
        }
    }

    private String encodeKey(String key) {
        return key.contains("%") ? key : encode(key);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}