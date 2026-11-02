package com.personal.marketnote.commerce.adapter.in.web.order.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChangeOrderStatusRequest 유효성 검증 테스트")
class ChangeOrderStatusRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("orderStatus가 null이면 유효성 검증에 실패한다")
    void shouldFailValidationWhenOrderStatusIsNull() {
        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest();

        Set<ConstraintViolation<ChangeOrderStatusRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("orderStatus")
                        && v.getMessage().equals("주문 상태는 필수값입니다."));
    }
}
