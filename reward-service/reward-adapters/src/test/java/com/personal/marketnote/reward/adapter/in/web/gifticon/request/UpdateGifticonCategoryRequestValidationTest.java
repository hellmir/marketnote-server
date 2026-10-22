package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateGifticonCategoryRequestValidationTest {
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
    @DisplayName("iconUrl이 javascript 스킴이면 검증에 실패한다")
    void shouldFailWhenIconUrlIsJavascriptScheme() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setIconUrl("javascript:alert(1)");

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("iconUrl"));
    }

    @Test
    @DisplayName("iconUrl이 http 스킴이면 검증에 실패한다")
    void shouldFailWhenIconUrlIsHttpScheme() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setIconUrl("http://example.com/icon.png");

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("iconUrl"));
    }

    @Test
    @DisplayName("iconUrl이 256자이면 검증에 실패한다")
    void shouldFailWhenIconUrlExceedsMaxLength() {
        // given
        String longIconUrl = "https://" + "a".repeat(248);
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setIconUrl(longIconUrl);

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(longIconUrl.length()).isEqualTo(256);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("iconUrl"));
    }

    @Test
    @DisplayName("iconUrl이 JSON에 없으면 검증에 성공한다")
    void shouldPassWhenIconUrlIsNotProvided() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(request.isIconUrlProvided()).isFalse();
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("iconUrl이 명시적 null이면 검증에 성공한다")
    void shouldPassWhenIconUrlIsExplicitlyNull() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setIconUrl(null);

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(request.isIconUrlProvided()).isTrue();
        assertThat(request.getIconUrl()).isNull();
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("displayName이 51자이면 검증에 실패한다")
    void shouldFailWhenDisplayNameExceedsMaxLength() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setDisplayName("a".repeat(51));

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("displayName"));
    }

    @Test
    @DisplayName("displayName이 JSON에 없으면 검증에 성공한다")
    void shouldPassWhenDisplayNameIsNotProvided() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(request.isDisplayNameProvided()).isFalse();
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("displayName이 명시적 null이면 검증에 성공한다")
    void shouldPassWhenDisplayNameIsExplicitlyNull() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setDisplayName(null);

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(request.isDisplayNameProvided()).isTrue();
        assertThat(request.getDisplayName()).isNull();
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("정상 https iconUrl과 정상 displayName이면 검증에 성공한다")
    void shouldPassWhenValidIconUrlAndDisplayName() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setDisplayName("카테고리");
        request.setIconUrl("https://cdn.example.com/icon.png");

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("iconUrl이 정확히 255자이면 검증에 성공한다")
    void shouldPassWhenIconUrlIsExactlyMaxLength() {
        // given
        String iconUrl = "https://" + "a".repeat(247);
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setIconUrl(iconUrl);

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(iconUrl.length()).isEqualTo(255);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("displayName이 정확히 50자이면 검증에 성공한다")
    void shouldPassWhenDisplayNameIsExactlyMaxLength() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setDisplayName("a".repeat(50));

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("iconUrl이 빈 문자열이면 검증에 실패한다")
    void shouldFailWhenIconUrlIsEmpty() {
        // given
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setIconUrl("");

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("iconUrl"));
    }

    @Test
    @DisplayName("displayName이 빈 문자열이면 검증에 성공한다")
    void shouldPassWhenDisplayNameIsEmpty() {
        // given
        // @Size(max = 50)만 걸려 있고 @NotBlank가 없으므로 빈 문자열은 Bean Validation을 통과한다.
        // 서비스 레이어에서 FormatValidator.hasNoValue 로직에 의해 null로 정규화되어 무시된다.
        UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
        request.setDisplayName("");

        // when
        Set<ConstraintViolation<UpdateGifticonCategoryRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }
}
