package com.youthguide.api.certificate.repository;

import com.youthguide.api.certificate.domain.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findAllByYearLabel(String yearLabel);

    List<ExamSession> findAllByCertificate_Id(Long certificateId);
}