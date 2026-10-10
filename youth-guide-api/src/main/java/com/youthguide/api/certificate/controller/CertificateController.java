package com.youthguide.api.certificate.controller;

import com.youthguide.api.certificate.service.QnetApiService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.youthguide.api.global.util.QnetResponseParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dev/qnet")
public class CertificateController {

    private final QnetApiService qnetApiService;

    private final QnetResponseParser parser;

    public CertificateController(QnetApiService qnetApiService, QnetResponseParser parser) {
        this.qnetApiService = qnetApiService;
        this.parser = parser;
    }

    @GetMapping(value = "/certificates", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> certificates(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) {
        return ResponseEntity.ok(qnetApiService.fetchCertificateList(pageNo, numOfRows));
    }

    @GetMapping(value = "/exam-schedules", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> examSchedules(
            @RequestParam String implYy,
            @RequestParam(required = false) String qualgbCd,
            @RequestParam(required = false) String jmCd,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows) {
        return ResponseEntity.ok(
                qnetApiService.fetchExamSchedule(implYy, qualgbCd, jmCd, pageNo, numOfRows));
    }

    @GetMapping(value = "/exam-schedules/parsed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> examSchedulesParsed(
            @RequestParam String implYy,
            @RequestParam(required = false) String qualgbCd,
            @RequestParam(required = false) String jmCd) {
        String json = qnetApiService.fetchExamSchedule(implYy, qualgbCd, jmCd, 1, 5);
        List<Map<String, Object>> items = parser.extractItems(json);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", items.size());
        result.put("fields", items.isEmpty() ? List.of() : items.get(0).keySet());
        result.put("sample", items.stream().limit(2).toList());
        return ResponseEntity.ok(result);
    }

}