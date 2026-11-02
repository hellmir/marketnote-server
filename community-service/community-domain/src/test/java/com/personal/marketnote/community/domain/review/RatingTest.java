package com.personal.marketnote.community.domain.review;

import com.personal.marketnote.community.domain.review.exception.InvalidRatingPointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatingTest {

    @ParameterizedTest
    @ValueSource(floats = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f})
    @DisplayName("1~5 정수 값으로 생성 시 정상 생성된다")
    void createRatingWithValidIntegerValue(float value) {
        Rating rating = Rating.of(value);

        assertThat(rating.getValue()).isEqualTo((int) value);
    }

    @Test
    @DisplayName("0으로 생성 시 InvalidRatingPointException 발생한다")
    void throwsExceptionWhenRatingIsZero() {
        assertThatThrownBy(() -> Rating.of(0.0f))
                .isInstanceOf(InvalidRatingPointException.class);
    }

    @Test
    @DisplayName("6으로 생성 시 InvalidRatingPointException 발생한다")
    void throwsExceptionWhenRatingIsSix() {
        assertThatThrownBy(() -> Rating.of(6.0f))
                .isInstanceOf(InvalidRatingPointException.class);
    }

    @Test
    @DisplayName("소수(3.5)로 생성 시 InvalidRatingPointException 발생한다")
    void throwsExceptionWhenRatingIsFractional() {
        assertThatThrownBy(() -> Rating.of(3.5f))
                .isInstanceOf(InvalidRatingPointException.class);
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환한다")
    void getValueReturnsOriginalValue() {
        Rating rating = Rating.of(4.0f);

        assertThat(rating.getValue()).isEqualTo(4);
    }
}
