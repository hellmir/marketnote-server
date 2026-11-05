package com.personal.marketnote.user.adapter.in.web.user.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateUserInfoRequest phoneNumber 유효성 검증")
class UpdateUserInfoRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest(name = "canonical/하이픈 포함 입력 {0} 은 phoneNumber 패턴 위반 없음")
    @ValueSource(strings = {
            "01012345678",
            "0101234567",
            "010-1234-5678",
            "010-123-4567",
            "010-12345678"
    })
    @DisplayName("canonical 또는 하이픈 포함 입력은 phoneNumber 패턴 검증을 통과한다")
    void shouldPassPhoneNumberValidationForValidInputs(String phoneNumber) {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest();
        ReflectionTestUtils.setField(request, "phoneNumber", phoneNumber);

        Set<ConstraintViolation<UpdateUserInfoRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
    }

    @ParameterizedTest(name = "비허용 입력 {0} 은 phoneNumber 패턴 위반 발생")
    @ValueSource(strings = {
            "01012345",
            "02012345678",
            "01212345678",
            "010 1234 5678",
            "010.1234.5678"
    })
    @DisplayName("비허용 입력은 phoneNumber 패턴 검증에 실패한다")
    void shouldFailPhoneNumberValidationForInvalidInputs(String phoneNumber) {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest();
        ReflectionTestUtils.setField(request, "phoneNumber", phoneNumber);

        Set<ConstraintViolation<UpdateUserInfoRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
    }

    @Test
    @DisplayName("phoneNumber가 null이면 패턴 검증은 통과한다 (NOT_REQUIRED 필드)")
    void shouldPassPhoneNumberValidationWhenNull() {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest();
        ReflectionTestUtils.setField(request, "phoneNumber", null);

        Set<ConstraintViolation<UpdateUserInfoRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("phoneNumber"));
    }
}
