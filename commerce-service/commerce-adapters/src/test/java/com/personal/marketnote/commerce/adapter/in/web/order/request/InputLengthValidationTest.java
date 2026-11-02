package com.personal.marketnote.commerce.adapter.in.web.order.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("입력값 길이 제한 검증 테스트")
class InputLengthValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("OrderProductItemRequest.imageUrl 길이 제한")
    class ImageUrlLengthTest {

        @Test
        @DisplayName("imageUrl이 2048자를 초과하면 유효성 검증에 실패한다")
        void shouldFailValidationWhenImageUrlExceeds2048Characters() {
            OrderProductItemRequest request = new OrderProductItemRequest();
            setField(request, "productId", 1L);
            setField(request, "sellerId", 1L);
            setField(request, "pricePolicyId", 1L);
            setField(request, "quantity", 1);
            setField(request, "unitAmount", 50000L);
            setField(request, "imageUrl", "https://example.com/" + "a".repeat(2030));

            Set<ConstraintViolation<OrderProductItemRequest>> violations = validator.validate(request);

            assertThat(violations).anyMatch(v ->
                    v.getPropertyPath().toString().equals("imageUrl"));
        }

        @Test
        @DisplayName("imageUrl이 2048자 이내이면 imageUrl 길이 검증에 성공한다")
        void shouldPassValidationWhenImageUrlIsWithin2048Characters() {
            OrderProductItemRequest request = new OrderProductItemRequest();
            setField(request, "productId", 1L);
            setField(request, "sellerId", 1L);
            setField(request, "pricePolicyId", 1L);
            setField(request, "quantity", 1);
            setField(request, "unitAmount", 50000L);
            setField(request, "imageUrl", "https://example.com/image.jpg");

            Set<ConstraintViolation<OrderProductItemRequest>> violations = validator.validate(request);

            assertThat(violations).noneMatch(v ->
                    v.getPropertyPath().toString().equals("imageUrl"));
        }
    }

    @Nested
    @DisplayName("ChangeOrderStatusRequest.reason 길이 제한")
    class ReasonLengthTest {

        @Test
        @DisplayName("reason이 500자를 초과하면 유효성 검증에 실패한다")
        void shouldFailValidationWhenReasonExceeds500Characters() {
            ChangeOrderStatusRequest request = new ChangeOrderStatusRequest();
            setField(request, "reason", "가".repeat(501));

            Set<ConstraintViolation<ChangeOrderStatusRequest>> violations = validator.validate(request);

            assertThat(violations).anyMatch(v ->
                    v.getPropertyPath().toString().equals("reason"));
        }

        @Test
        @DisplayName("reason이 500자 이내이면 reason 길이 검증에 성공한다")
        void shouldPassValidationWhenReasonIsWithin500Characters() {
            ChangeOrderStatusRequest request = new ChangeOrderStatusRequest();
            setField(request, "reason", "가".repeat(500));

            Set<ConstraintViolation<ChangeOrderStatusRequest>> violations = validator.validate(request);

            assertThat(violations).noneMatch(v ->
                    v.getPropertyPath().toString().equals("reason"));
        }
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
