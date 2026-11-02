package com.personal.marketnote.reward.configuration;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RewardPropertiesValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("share-point-rate가 정상 범위 내 값이면 검증을 통과한다")
    void shouldPassWhenShareRateWithinRange() {
        RewardProperties properties = new RewardProperties();
        properties.setSharePointRate(new BigDecimal("0.1"));

        Set<ConstraintViolation<RewardProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("share-point-rate가 null이면 @NotNull 위반으로 검증에 실패한다")
    void shouldFailWhenShareRateIsNull() {
        RewardProperties properties = new RewardProperties();
        properties.setSharePointRate(null);

        Set<ConstraintViolation<RewardProperties>> violations = validator.validate(properties);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RewardProperties> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("sharePointRate");
        assertThat(violation.getConstraintDescriptor().getAnnotation().annotationType())
                .isEqualTo(NotNull.class);
    }

    @Test
    @DisplayName("share-point-rate가 음수이면 @DecimalMin 위반으로 검증에 실패한다")
    void shouldFailWhenShareRateIsNegative() {
        RewardProperties properties = new RewardProperties();
        properties.setSharePointRate(new BigDecimal("-0.1"));

        Set<ConstraintViolation<RewardProperties>> violations = validator.validate(properties);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RewardProperties> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("sharePointRate");
        assertThat(violation.getConstraintDescriptor().getAnnotation().annotationType())
                .isEqualTo(DecimalMin.class);
    }

    @Test
    @DisplayName("share-point-rate가 1.0을 초과하면 @DecimalMax 위반으로 검증에 실패한다")
    void shouldFailWhenShareRateExceedsOne() {
        RewardProperties properties = new RewardProperties();
        properties.setSharePointRate(new BigDecimal("1.1"));

        Set<ConstraintViolation<RewardProperties>> violations = validator.validate(properties);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RewardProperties> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("sharePointRate");
        assertThat(violation.getConstraintDescriptor().getAnnotation().annotationType())
                .isEqualTo(DecimalMax.class);
    }

    @Test
    @DisplayName("share-point-rate가 경계값 0.0이면 검증을 통과한다")
    void shouldPassWhenShareRateIsLowerBound() {
        RewardProperties properties = new RewardProperties();
        properties.setSharePointRate(new BigDecimal("0.0"));

        Set<ConstraintViolation<RewardProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("share-point-rate가 경계값 1.0이면 검증을 통과한다")
    void shouldPassWhenShareRateIsUpperBound() {
        RewardProperties properties = new RewardProperties();
        properties.setSharePointRate(new BigDecimal("1.0"));

        Set<ConstraintViolation<RewardProperties>> violations = validator.validate(properties);

        assertThat(violations).isEmpty();
    }
}
