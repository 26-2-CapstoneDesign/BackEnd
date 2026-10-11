package com.youthguide.api.review.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostUpdateRequestTest {

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
    void acceptsValidRequestAndTwentyCharacterTitle() {
        CommunityPostUpdateRequest request = new CommunityPostUpdateRequest(
                "200",
                "가".repeat(20),
                "수정한 시험 후기 본문",
                "7"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void requiresNumericCertificateId() {
        CommunityPostUpdateRequest missing = new CommunityPostUpdateRequest(null, "제목", "본문", "7");
        CommunityPostUpdateRequest nonNumeric = new CommunityPostUpdateRequest("certificate", "제목", "본문", "7");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("certificateId"));
        assertThat(validator.validate(nonNumeric)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("certificateId"));
    }

    @Test
    void requiresTitleAndLimitsItToTwentyCharacters() {
        CommunityPostUpdateRequest missing = new CommunityPostUpdateRequest("200", null, "본문", "7");
        CommunityPostUpdateRequest blank = new CommunityPostUpdateRequest("200", " ", "본문", "7");
        CommunityPostUpdateRequest tooLong = new CommunityPostUpdateRequest("200", "가".repeat(21), "본문", "7");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("title"));
        assertThat(validator.validate(blank)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("title"));
        assertThat(validator.validate(tooLong)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("title"));
    }

    @Test
    void requiresBody() {
        CommunityPostUpdateRequest missing = new CommunityPostUpdateRequest("200", "제목", null, "7");
        CommunityPostUpdateRequest blank = new CommunityPostUpdateRequest("200", "제목", " ", "7");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("body"));
        assertThat(validator.validate(blank)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("body"));
    }

    @Test
    void requiresNumericVersion() {
        CommunityPostUpdateRequest missing = new CommunityPostUpdateRequest("200", "제목", "본문", null);
        CommunityPostUpdateRequest blank = new CommunityPostUpdateRequest("200", "제목", "본문", " ");
        CommunityPostUpdateRequest nonNumeric = new CommunityPostUpdateRequest("200", "제목", "본문", "version");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("version"));
        assertThat(validator.validate(blank)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("version"));
        assertThat(validator.validate(nonNumeric)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("version"));
    }
}
