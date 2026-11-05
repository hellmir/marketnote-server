package com.personal.marketnote.user.adapter.in.web.shippingaddress.request;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
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

@DisplayName("RegisterShippingAddressRequest recipientPhoneNumber 유효성 검증")
class RegisterShippingAddressRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegisterShippingAddressRequest buildBase(String recipientPhoneNumber) {
        RegisterShippingAddressRequest request = new RegisterShippingAddressRequest();
        ReflectionTestUtils.setField(request, "addressType", ShippingAddressType.HOME);
        ReflectionTestUtils.setField(request, "address", "서울시 강남구 테헤란로");
        ReflectionTestUtils.setField(request, "addressDetail", "101호");
        ReflectionTestUtils.setField(request, "recipientName", "홍길동");
        ReflectionTestUtils.setField(request, "recipientPhoneNumber", recipientPhoneNumber);
        return request;
    }

    @ParameterizedTest(name = "canonical/하이픈 포함 입력 {0} 은 recipientPhoneNumber 패턴 위반 없음")
    @ValueSource(strings = {
            "01012345678",
            "0101234567",
            "010-1234-5678",
            "010-123-4567",
            "010-12345678"
    })
    @DisplayName("canonical 또는 하이픈 포함 입력은 recipientPhoneNumber 패턴 검증을 통과한다")
    void shouldPassValidationForValidInputs(String recipientPhoneNumber) {
        RegisterShippingAddressRequest request = buildBase(recipientPhoneNumber);

        Set<ConstraintViolation<RegisterShippingAddressRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("recipientPhoneNumber"));
    }

    @ParameterizedTest(name = "비허용 입력 {0} 은 recipientPhoneNumber 패턴 위반 발생")
    @ValueSource(strings = {
            "01012345",
            "02012345678",
            "01212345678",
            "010 1234 5678"
    })
    @DisplayName("비허용 입력은 recipientPhoneNumber 패턴 검증에 실패한다")
    void shouldFailValidationForInvalidInputs(String recipientPhoneNumber) {
        RegisterShippingAddressRequest request = buildBase(recipientPhoneNumber);

        Set<ConstraintViolation<RegisterShippingAddressRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("recipientPhoneNumber"));
    }

    @Test
    @DisplayName("recipientPhoneNumber가 빈 문자열이면 NotBlank 위반이 발생한다")
    void shouldFailValidationWhenBlank() {
        RegisterShippingAddressRequest request = buildBase("");

        Set<ConstraintViolation<RegisterShippingAddressRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("recipientPhoneNumber"));
    }
}
