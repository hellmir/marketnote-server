package com.personal.marketnote.community.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RatingPointTest {

    @Test
    @DisplayName("isFive는 5점일 때 true를 반환한다")
    void shouldReturnTrueWhenPointIsFive() {
        assertThat(RatingPoint.isFive(5)).isTrue();
        assertThat(RatingPoint.isFive(4)).isFalse();
        assertThat(RatingPoint.isFive(3)).isFalse();
        assertThat(RatingPoint.isFive(2)).isFalse();
        assertThat(RatingPoint.isFive(1)).isFalse();
    }

    @Test
    @DisplayName("isFour는 4점일 때 true를 반환한다")
    void shouldReturnTrueWhenPointIsFour() {
        assertThat(RatingPoint.isFour(4)).isTrue();
        assertThat(RatingPoint.isFour(5)).isFalse();
        assertThat(RatingPoint.isFour(3)).isFalse();
        assertThat(RatingPoint.isFour(2)).isFalse();
        assertThat(RatingPoint.isFour(1)).isFalse();
    }

    @Test
    @DisplayName("isThree는 3점일 때 true를 반환한다")
    void shouldReturnTrueWhenPointIsThree() {
        assertThat(RatingPoint.isThree(3)).isTrue();
        assertThat(RatingPoint.isThree(5)).isFalse();
        assertThat(RatingPoint.isThree(4)).isFalse();
        assertThat(RatingPoint.isThree(2)).isFalse();
        assertThat(RatingPoint.isThree(1)).isFalse();
    }

    @Test
    @DisplayName("isTwo는 2점일 때 true를 반환한다")
    void shouldReturnTrueWhenPointIsTwo() {
        assertThat(RatingPoint.isTwo(2)).isTrue();
        assertThat(RatingPoint.isTwo(5)).isFalse();
        assertThat(RatingPoint.isTwo(4)).isFalse();
        assertThat(RatingPoint.isTwo(3)).isFalse();
        assertThat(RatingPoint.isTwo(1)).isFalse();
    }

    @Test
    @DisplayName("isOne은 1점일 때 true를 반환한다")
    void shouldReturnTrueWhenPointIsOne() {
        assertThat(RatingPoint.isOne(1)).isTrue();
        assertThat(RatingPoint.isOne(5)).isFalse();
        assertThat(RatingPoint.isOne(4)).isFalse();
        assertThat(RatingPoint.isOne(3)).isFalse();
        assertThat(RatingPoint.isOne(2)).isFalse();
    }

    @Test
    @DisplayName("유효 범위(1~5) 밖의 값은 모든 술어 메서드에서 false를 반환한다")
    void shouldReturnFalseForOutOfRangeValues() {
        int[] outOfRange = {0, -1, 6, 100, Integer.MAX_VALUE, Integer.MIN_VALUE};

        for (int value : outOfRange) {
            assertThat(RatingPoint.isFive(value)).isFalse();
            assertThat(RatingPoint.isFour(value)).isFalse();
            assertThat(RatingPoint.isThree(value)).isFalse();
            assertThat(RatingPoint.isTwo(value)).isFalse();
            assertThat(RatingPoint.isOne(value)).isFalse();
        }
    }

    @Test
    @DisplayName("각 RatingPoint 상수는 올바른 value와 description을 가진다")
    void shouldHaveCorrectValueAndDescription() {
        assertThat(RatingPoint.FIVE.getValue()).isEqualTo(5);
        assertThat(RatingPoint.FIVE.getDescription()).isEqualTo("5점");
        assertThat(RatingPoint.FOUR.getValue()).isEqualTo(4);
        assertThat(RatingPoint.FOUR.getDescription()).isEqualTo("4점");
        assertThat(RatingPoint.THREE.getValue()).isEqualTo(3);
        assertThat(RatingPoint.THREE.getDescription()).isEqualTo("3점");
        assertThat(RatingPoint.TWO.getValue()).isEqualTo(2);
        assertThat(RatingPoint.TWO.getDescription()).isEqualTo("2점");
        assertThat(RatingPoint.ONE.getValue()).isEqualTo(1);
        assertThat(RatingPoint.ONE.getDescription()).isEqualTo("1점");
    }

    @Test
    @DisplayName("RatingPoint는 5개의 상수를 가진다")
    void shouldHaveFiveConstants() {
        assertThat(RatingPoint.values()).hasSize(5);
    }
}
