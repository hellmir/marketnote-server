package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidValidDaysException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidDaysTest {

    @Test
    @DisplayName("1로 생성 시 정상 생성된다")
    void createWithOne() {
        ValidDays validDays = ValidDays.of(1);

        assertThat(validDays.getValue()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 30, 90, 365, 3650})
    @DisplayName("양수로 생성 시 정상 생성된다")
    void createWithPositive(int value) {
        ValidDays validDays = ValidDays.of(value);

        assertThat(validDays.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("0으로 생성 시 InvalidValidDaysException이 발생한다")
    void createWithZero() {
        assertThatThrownBy(() -> ValidDays.of(0))
                .isInstanceOf(InvalidValidDaysException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -30, Integer.MIN_VALUE})
    @DisplayName("음수로 생성 시 InvalidValidDaysException이 발생한다")
    void createWithNegative(int value) {
        assertThatThrownBy(() -> ValidDays.of(value))
                .isInstanceOf(InvalidValidDaysException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -30, 1, 365})
    @DisplayName("fromSnapshot은 DB 복원 경로로 0/음수도 검증 없이 통과한다")
    void fromSnapshotAcceptsAnyValue(int value) {
        ValidDays validDays = ValidDays.fromSnapshot(value);

        assertThat(validDays.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("같은 값으로 생성한 ValidDays는 동등하다")
    void equality() {
        ValidDays a = ValidDays.of(30);
        ValidDays b = ValidDays.of(30);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
