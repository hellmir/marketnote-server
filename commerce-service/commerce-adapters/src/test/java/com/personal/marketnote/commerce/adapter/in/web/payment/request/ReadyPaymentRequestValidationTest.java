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

@DisplayName("ReadyPaymentRequest 유효성 검증 테스트")
class ReadyPaymentRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("goodName이 100자를 초과하면 유효성 검증에 실패한다")
    void shouldFailValidationWhenGoodNameExceeds100Characters() {
        ReadyPaymentRequest request = new ReadyPaymentRequest();
        setField(request, "orderKey", "valid-order-key");
        setField(request, "payMethod", "CARD");
        setField(request, "goodName", "가".repeat(101));

        Set<ConstraintViolation<ReadyPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("goodName"));
    }

    @Test
    @DisplayName("goodName이 100자 이내이면 goodName 길이 검증에 성공한다")
    void shouldPassValidationWhenGoodNameIsWithin100Characters() {
        ReadyPaymentRequest request = new ReadyPaymentRequest();
        setField(request, "orderKey", "valid-order-key");
        setField(request, "payMethod", "CARD");
        setField(request, "goodName", "가".repeat(100));

        Set<ConstraintViolation<ReadyPaymentRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v ->
                v.getPropertyPath().toString().equals("goodName")
                        && v.getMessage().contains("100"));
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
