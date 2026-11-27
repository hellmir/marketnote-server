package com.personal.marketnote.commerce.adapter.in.web.payment.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResolveUnknownPaymentRequest 유효성 검증 테스트")
class ResolveUnknownPaymentRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("appTime 이 14자리 숫자가 아닌 문자열이면 형식 검증에 실패한다")
    void shouldFailValidationWhenAppTimeIsNonNumericString() {
        ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
            "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", "abc"
        );

        Set<ConstraintViolation<ResolveUnknownPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appTime"));
    }

    @Test
    @DisplayName("appTime 이 14자리 미만 숫자이면 형식 검증에 실패한다")
    void shouldFailValidationWhenAppTimeIsShorterThan14Digits() {
        ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
            "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", "2024"
        );

        Set<ConstraintViolation<ResolveUnknownPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appTime"));
    }

    @Test
    @DisplayName("appTime 이 14자리이지만 숫자가 아닌 문자를 포함하면 형식 검증에 실패한다")
    void shouldFailValidationWhenAppTimeContainsNonDigitCharacter() {
        ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
            "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", "20240101000000X"
        );

        Set<ConstraintViolation<ResolveUnknownPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("appTime"));
    }

    @Test
    @DisplayName("appTime 이 빈 문자열이면 형식 검증을 통과한다 (Optional 허용)")
    void shouldPassValidationWhenAppTimeIsEmptyString() {
        ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
            "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", ""
        );

        Set<ConstraintViolation<ResolveUnknownPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("appTime"));
    }

    @Test
    @DisplayName("appTime 이 null 이면 형식 검증을 통과한다 (Optional 허용)")
    void shouldPassValidationWhenAppTimeIsNull() {
        ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
            "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", null
        );

        Set<ConstraintViolation<ResolveUnknownPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("appTime"));
    }

    @Test
    @DisplayName("appTime 이 정확히 14자리 숫자이면 형식 검증을 통과한다")
    void shouldPassValidationWhenAppTimeIsExactly14Digits() {
        ResolveUnknownPaymentRequest request = new ResolveUnknownPaymentRequest(
            "COMPLETE", "0000", "정상", "PG_KEY_1", "APPROVAL_1", "20260115120000"
        );

        Set<ConstraintViolation<ResolveUnknownPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("appTime"));
    }
}
