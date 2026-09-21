package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.reward.domain.exception.InvalidRewardQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RewardQuantityTest {

    @Test
    @DisplayName("0으로 생성 시 정상 생성된다")
    void shouldCreateWithZero() {
        RewardQuantity quantity = RewardQuantity.of(0L);

        assertThat(quantity.getValue()).isEqualTo(0L);
    }

    @Test
    @DisplayName("양수로 생성 시 정상 생성된다")
    void shouldCreateWithPositiveValue() {
        RewardQuantity quantity = RewardQuantity.of(100L);

        assertThat(quantity.getValue()).isEqualTo(100L);
    }

    @Test
    @DisplayName("음수로 생성 시 InvalidRewardQuantityException이 발생한다")
    void shouldThrowWhenNegativeValue() {
        assertThatThrownBy(() -> RewardQuantity.of(-1L))
                .isInstanceOf(InvalidRewardQuantityException.class);
    }

    @Test
    @DisplayName("getValue()가 생성 시 전달한 값을 반환한다")
    void shouldReturnValuePassedOnCreation() {
        RewardQuantity quantity = RewardQuantity.of(500L);

        assertThat(quantity.getValue()).isEqualTo(500L);
    }

    @Test
    @DisplayName("zero()가 값 0의 RewardQuantity를 반환한다")
    void shouldReturnZeroRewardQuantity() {
        RewardQuantity zero = RewardQuantity.zero();

        assertThat(zero.getValue()).isEqualTo(0L);
    }

    @Test
    @DisplayName("add()가 두 RewardQuantity의 합을 새 인스턴스로 반환한다")
    void shouldAddTwoRewardQuantities() {
        RewardQuantity a = RewardQuantity.of(100L);
        RewardQuantity b = RewardQuantity.of(50L);

        RewardQuantity result = a.add(b);

        assertThat(result.getValue()).isEqualTo(150L);
        assertThat(a.getValue()).isEqualTo(100L);
    }

    @Test
    @DisplayName("equals()와 hashCode()가 값 기반으로 동작한다")
    void shouldImplementEqualsAndHashCodeByValue() {
        RewardQuantity a = RewardQuantity.of(100L);
        RewardQuantity b = RewardQuantity.of(100L);
        RewardQuantity c = RewardQuantity.of(200L);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
