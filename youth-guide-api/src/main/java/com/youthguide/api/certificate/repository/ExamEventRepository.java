package com.youthguide.api.certificate.repository;

import com.youthguide.api.certificate.domain.ExamEvent;
import com.youthguide.api.certificate.domain.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExamEventRepository extends JpaRepository<ExamEvent, Long> {
    List<ExamEvent> findAllByExamSessionIn(Collection<ExamSession> sessions);
}