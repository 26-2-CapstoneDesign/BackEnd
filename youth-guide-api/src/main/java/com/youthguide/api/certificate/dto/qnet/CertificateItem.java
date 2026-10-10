package com.youthguide.api.certificate.dto.qnet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CertificateItem(
        String jmcd,          // 종목코드
        String jmfldnm,       // 종목명
        String qualgbcd,      // 자격구분코드
        String qualgbnm,      // 자격구분명
        String seriescd,      // 계열코드
        String seriesnm,      // 계열명
        String obligfldcd,    // 대직무분야코드
        String obligfldnm,    // 대직무분야명
        String mdobligfldcd,  // 중직무분야코드
        String mdobligfldnm   // 중직무분야명
) {}