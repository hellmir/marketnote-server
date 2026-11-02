package com.personal.marketnote.community.adapter.in.web.review.request;

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

@DisplayName("리뷰 내용 최소 글자 수 검증 테스트")
class ReviewContentLengthValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("RegisterReviewRequest content 길이 제한")
    class RegisterReviewContentLengthTest {

        @Test
        @DisplayName("리뷰 작성 시 20자 미만 내용이면 검증 실패한다")
        void shouldFailValidationWhenContentIsLessThan20Characters() {
            RegisterReviewRequest request = new RegisterReviewRequest();
            setField(request, "orderId", 1L);
            setField(request, "productId", 1L);
            setField(request, "pricePolicyId", 1L);
            setField(request, "quantity", 1);
            setField(request, "reviewerName", "홍길동");
            setField(request, "rating", 5.0f);
            setField(request, "content", "가".repeat(19));
            setField(request, "isPhoto", false);

            Set<ConstraintViolation<RegisterReviewRequest>> violations = validator.validate(request);

            assertThat(violations).anyMatch(v ->
                    v.getPropertyPath().toString().equals("content"));
        }

        @Test
        @DisplayName("리뷰 작성 시 20자 이상 내용이면 정상적으로 등록된다")
        void shouldPassValidationWhenContentIsAtLeast20Characters() {
            RegisterReviewRequest request = new RegisterReviewRequest();
            setField(request, "orderId", 1L);
            setField(request, "productId", 1L);
            setField(request, "pricePolicyId", 1L);
            setField(request, "quantity", 1);
            setField(request, "reviewerName", "홍길동");
            setField(request, "rating", 5.0f);
            setField(request, "content", "가".repeat(20));
            setField(request, "isPhoto", false);

            Set<ConstraintViolation<RegisterReviewRequest>> violations = validator.validate(request);

            assertThat(violations).noneMatch(v ->
                    v.getPropertyPath().toString().equals("content"));
        }
    }

    @Nested
    @DisplayName("UpdateReviewRequest content 길이 제한")
    class UpdateReviewContentLengthTest {

        @Test
        @DisplayName("리뷰 수정 시 20자 미만 내용이면 검증 실패한다")
        void shouldFailValidationWhenContentIsLessThan20Characters() {
            UpdateReviewRequest request = new UpdateReviewRequest();
            setField(request, "rating", 5.0f);
            setField(request, "content", "가".repeat(19));

            Set<ConstraintViolation<UpdateReviewRequest>> violations = validator.validate(request);

            assertThat(violations).anyMatch(v ->
                    v.getPropertyPath().toString().equals("content"));
        }

        @Test
        @DisplayName("리뷰 수정 시 20자 이상 내용이면 정상적으로 수정된다")
        void shouldPassValidationWhenContentIsAtLeast20Characters() {
            UpdateReviewRequest request = new UpdateReviewRequest();
            setField(request, "rating", 5.0f);
            setField(request, "content", "가".repeat(20));

            Set<ConstraintViolation<UpdateReviewRequest>> violations = validator.validate(request);

            assertThat(violations).noneMatch(v ->
                    v.getPropertyPath().toString().equals("content"));
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
