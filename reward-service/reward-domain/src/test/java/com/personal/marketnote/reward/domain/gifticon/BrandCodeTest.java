package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.BrandCodeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandCodeTest {

    @Test
    @DisplayName("유효 브랜드 코드로 생성 시 정상 생성된다")
    void createWithValidValue() {
        BrandCode brandCode = BrandCode.of("B001");

        assertThat(brandCode.getValue()).isEqualTo("B001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("null/빈 문자열/공백으로 생성 시 BrandCodeNoValueException이 발생한다")
    void createWithBlank(String value) {
        assertThatThrownBy(() -> BrandCode.of(value))
                .isInstanceOf(BrandCodeNoValueException.class);
    }

    @Test
    @DisplayName("getValue()가 생성 시 전달한 값을 반환한다")
    void getValue() {
        BrandCode brandCode = BrandCode.of("STARBUCKS");

        assertThat(brandCode.getValue()).isEqualTo("STARBUCKS");
    }

    @ParameterizedTest
    @ValueSource(strings = {"B001", "X", "valid-code"})
    @DisplayName("fromSnapshot은 유효값을 그대로 반환한다")
    void fromSnapshotWithValid(String value) {
        BrandCode brandCode = BrandCode.fromSnapshot(value);

        assertThat(brandCode.getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("fromSnapshot은 null/blank DB 레거시 값도 검증 없이 통과한다")
    void fromSnapshotWithBlank(String value) {
        BrandCode brandCode = BrandCode.fromSnapshot(value);

        assertThat(brandCode.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("같은 값으로 생성한 BrandCode는 동등하다")
    void equality() {
        BrandCode a = BrandCode.of("B001");
        BrandCode b = BrandCode.of("B001");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
