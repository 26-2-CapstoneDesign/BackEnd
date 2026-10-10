package com.youthguide.api.certificate.repository;

import com.youthguide.api.certificate.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
}