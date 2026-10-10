package com.youthguide.api.certificate.repository;

import com.youthguide.api.certificate.domain.ExamEventRevision;
import com.youthguide.api.certificate.domain.ExamEventRevisionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExamEventRevisionRepository extends JpaRepository<ExamEventRevision, ExamEventRevisionId> {
    List<ExamEventRevision> findAllByExamEventIdIn(Collection<Long> examEventIds);
}