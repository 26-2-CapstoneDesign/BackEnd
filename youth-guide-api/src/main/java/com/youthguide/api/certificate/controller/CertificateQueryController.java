package com.youthguide.api.certificate.controller;

import com.youthguide.api.certificate.dto.CertificateDetailResponse;
import com.youthguide.api.certificate.service.CertificateQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateQueryController {

    private final CertificateQueryService certificateQueryService;

    @GetMapping("/{id}")
    public CertificateDetailResponse getDetail(@PathVariable Long id) {
        return certificateQueryService.getDetail(id);
    }
}