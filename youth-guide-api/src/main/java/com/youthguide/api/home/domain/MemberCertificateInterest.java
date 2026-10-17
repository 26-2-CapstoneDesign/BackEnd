package com.youthguide.api.home.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "member_certificate_interest")
@IdClass(MemberCertificateInterestId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCertificateInterest {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Id
    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static MemberCertificateInterest of(Long memberId, Long certificateId) {
        MemberCertificateInterest interest = new MemberCertificateInterest();
        interest.memberId = memberId;
        interest.certificateId = certificateId;
        interest.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        return interest;
    }
}