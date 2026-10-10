package com.youthguide.api.certificate.repository;

import com.youthguide.api.certificate.domain.CertificateSourceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateSourceKeyRepository extends JpaRepository<CertificateSourceKey, Long> {

    @Query("""
            select k from CertificateSourceKey k
            join fetch k.certificate
            where k.source.id = :sourceId and k.codeNamespace = :namespace
            """)
    List<CertificateSourceKey> findAllWithCertificate(@Param("sourceId") Long sourceId,
                                                      @Param("namespace") String namespace);
}