package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.CategoryCodeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryCodeTest {

    @Test
    @DisplayName("유효 카테고리 코드로 생성 시 정상 생성된다")
    void createWithValidValue() {
        CategoryCode categoryCode = CategoryCode.of("C001");

        assertThat(categoryCode.getValue()).isEqualTo("C001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("null/빈 문자열/공백으로 생성 시 CategoryCodeNoValueException이 발생한다")
    void createWithBlank(String value) {
        assertThatThrownBy(() -> CategoryCode.of(value))
                .isInstanceOf(CategoryCodeNoValueException.class);
    }

    @Test
    @DisplayName("getValue()가 생성 시 전달한 값을 반환한다")
    void getValue() {
        CategoryCode categoryCode = CategoryCode.of("CAFE");

        assertThat(categoryCode.getValue()).isEqualTo("CAFE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"C001", "X", "valid-code"})
    @DisplayName("fromSnapshot은 유효값을 그대로 반환한다")
    void fromSnapshotWithValid(String value) {
        CategoryCode categoryCode = CategoryCode.fromSnapshot(value);

        assertThat(categoryCode.getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("fromSnapshot은 null/blank DB 레거시 값도 검증 없이 통과한다")
    void fromSnapshotWithBlank(String value) {
        CategoryCode categoryCode = CategoryCode.fromSnapshot(value);

        assertThat(categoryCode.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("같은 값으로 생성한 CategoryCode는 동등하다")
    void equality() {
        CategoryCode a = CategoryCode.of("C001");
        CategoryCode b = CategoryCode.of("C001");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
