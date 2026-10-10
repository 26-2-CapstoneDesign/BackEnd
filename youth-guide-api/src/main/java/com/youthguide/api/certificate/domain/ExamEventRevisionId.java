package com.youthguide.api.certificate.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExamEventRevisionId implements Serializable {
    private Long examEventId;
    private Long revisionNo;
}