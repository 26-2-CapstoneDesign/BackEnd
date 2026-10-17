package com.youthguide.api.review.dto;

import com.youthguide.api.review.domain.CommunityPostReportReason;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostReportCreateRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsEveryReasonWithoutDetailExceptOther() {
        assertThat(validator.validate(new CommunityPostReportCreateRequest(
                CommunityPostReportReason.SPAM, null
        ))).isEmpty();
        assertThat(validator.validate(new CommunityPostReportCreateRequest(
                CommunityPostReportReason.ABUSE, null
        ))).isEmpty();
        assertThat(validator.validate(new CommunityPostReportCreateRequest(
                CommunityPostReportReason.FALSE_INFORMATION, null
        ))).isEmpty();
        assertThat(validator.validate(new CommunityPostReportCreateRequest(
                CommunityPostReportReason.INAPPROPRIATE, null
        ))).isEmpty();
    }

    @Test
    void requiresNonBlankDetailForOtherAndNormalizesIt() {
        CommunityPostReportCreateRequest valid = new CommunityPostReportCreateRequest(
                CommunityPostReportReason.OTHER,
                "  기타 신고 사유  "
        );

        assertThat(validator.validate(valid)).isEmpty();
        assertThat(valid.normalizedDetail()).isEqualTo("기타 신고 사유");
        assertThat(validator.validate(new CommunityPostReportCreateRequest(
                CommunityPostReportReason.OTHER, null
        ))).isNotEmpty();
        assertThat(validator.validate(new CommunityPostReportCreateRequest(
                CommunityPostReportReason.OTHER, "   "
        ))).isNotEmpty();
    }

    @Test
    void rejectsDetailForNonOtherReason() {
        CommunityPostReportCreateRequest request = new CommunityPostReportCreateRequest(
                CommunityPostReportReason.SPAM,
                "상세 사유"
        );

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void requiresReasonAndLimitsDetailToTwoHundredFiftyFiveCharacters() {
        CommunityPostReportCreateRequest missingReason = new CommunityPostReportCreateRequest(null, null);
        CommunityPostReportCreateRequest maxLength = new CommunityPostReportCreateRequest(
                CommunityPostReportReason.OTHER,
                "가".repeat(255)
        );
        CommunityPostReportCreateRequest tooLong = new CommunityPostReportCreateRequest(
                CommunityPostReportReason.OTHER,
                "가".repeat(256)
        );

        assertThat(validator.validate(missingReason)).isNotEmpty();
        assertThat(validator.validate(maxLength)).isEmpty();
        assertThat(validator.validate(tooLong)).isNotEmpty();
    }
}
