package com.youthguide.api.home.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MemberCertificateInterestId implements Serializable {
    private Long memberId;
    private Long certificateId;
}