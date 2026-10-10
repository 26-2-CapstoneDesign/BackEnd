package com.youthguide.api.certificate.repository;

import com.youthguide.api.certificate.domain.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    Optional<ExamSchedule> findByImplYyAndImplSeqAndQualTypeCd(Integer implYy, Integer implSeq, String qualTypeCd);
    List<ExamSchedule> findAllByImplYyOrderByImplSeqAsc(Integer implYy);
}