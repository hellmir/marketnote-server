package com.personal.marketnote.product.adapter.in.web.pricepolicy.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterPricePolicyRequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("정가가 음수이면 검증에 실패한다")
    void shouldFailWhenPriceIsNegative() {
        RegisterPricePolicyRequest request = buildRequest(-1L, 0L, 0L);

        Set<ConstraintViolation<RegisterPricePolicyRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    @DisplayName("현재 판매가가 음수이면 검증에 실패한다")
    void shouldFailWhenDiscountPriceIsNegative() {
        RegisterPricePolicyRequest request = buildRequest(10000L, -1L, 0L);

        Set<ConstraintViolation<RegisterPricePolicyRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("discountPrice"));
    }

    @Test
    @DisplayName("적립 포인트가 음수이면 검증에 실패한다")
    void shouldFailWhenAccumulatedPointIsNegative() {
        RegisterPricePolicyRequest request = buildRequest(10000L, 8000L, -1L);

        Set<ConstraintViolation<RegisterPricePolicyRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("accumulatedPoint"));
    }

    @Test
    @DisplayName("정가, 현재 판매가, 적립 포인트가 모두 0이면 검증에 성공한다")
    void shouldPassWhenAllValuesAreZero() {
        RegisterPricePolicyRequest request = buildRequest(0L, 0L, 0L);

        Set<ConstraintViolation<RegisterPricePolicyRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("정가, 현재 판매가, 적립 포인트가 양수이면 검증에 성공한다")
    void shouldPassWhenAllValuesArePositive() {
        RegisterPricePolicyRequest request = buildRequest(10000L, 8000L, 200L);

        Set<ConstraintViolation<RegisterPricePolicyRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    private RegisterPricePolicyRequest buildRequest(Long price, Long discountPrice, Long accumulatedPoint) {
        RegisterPricePolicyRequest request = new RegisterPricePolicyRequest();
        ReflectionTestUtils.setField(request, "price", price);
        ReflectionTestUtils.setField(request, "discountPrice", discountPrice);
        ReflectionTestUtils.setField(request, "accumulatedPoint", accumulatedPoint);
        return request;
    }
}
