package com.youthguide.api.review.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostCreateRequestTest {

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
        CommunityPostCreateRequest request = new CommunityPostCreateRequest(
                "200",
                "가".repeat(20),
                "시험 후기 본문"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void requiresNumericCertificateId() {
        CommunityPostCreateRequest missing = new CommunityPostCreateRequest(null, "제목", "본문");
        CommunityPostCreateRequest blank = new CommunityPostCreateRequest(" ", "제목", "본문");
        CommunityPostCreateRequest nonNumeric = new CommunityPostCreateRequest("certificate", "제목", "본문");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("certificateId"));
        assertThat(validator.validate(blank)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("certificateId"));
        assertThat(validator.validate(nonNumeric)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("certificateId"));
    }

    @Test
    void requiresTitleAndLimitsItToTwentyCharacters() {
        CommunityPostCreateRequest missing = new CommunityPostCreateRequest("200", null, "본문");
        CommunityPostCreateRequest blank = new CommunityPostCreateRequest("200", " ", "본문");
        CommunityPostCreateRequest tooLong = new CommunityPostCreateRequest("200", "가".repeat(21), "본문");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("title"));
        assertThat(validator.validate(blank)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("title"));
        assertThat(validator.validate(tooLong)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("title"));
    }

    @Test
    void requiresBody() {
        CommunityPostCreateRequest missing = new CommunityPostCreateRequest("200", "제목", null);
        CommunityPostCreateRequest blank = new CommunityPostCreateRequest("200", "제목", " ");

        assertThat(validator.validate(missing)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("body"));
        assertThat(validator.validate(blank)).anyMatch(violation ->
                violation.getPropertyPath().toString().equals("body"));
    }
}
