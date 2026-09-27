package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.GoodsCodeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoodsCodeTest {

    @Test
    @DisplayName("유효 상품 코드로 생성 시 정상 생성된다")
    void createWithValidValue() {
        GoodsCode goodsCode = GoodsCode.of("G001");

        assertThat(goodsCode.getValue()).isEqualTo("G001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("null/빈 문자열/공백으로 생성 시 GoodsCodeNoValueException이 발생한다")
    void createWithBlank(String value) {
        assertThatThrownBy(() -> GoodsCode.of(value))
                .isInstanceOf(GoodsCodeNoValueException.class);
    }

    @Test
    @DisplayName("getValue()가 생성 시 전달한 값을 반환한다")
    void getValue() {
        GoodsCode goodsCode = GoodsCode.of("STARBUCKS-AMERICANO");

        assertThat(goodsCode.getValue()).isEqualTo("STARBUCKS-AMERICANO");
    }

    @ParameterizedTest
    @ValueSource(strings = {"G001", "X", "valid-code"})
    @DisplayName("fromSnapshot은 유효값을 그대로 반환한다")
    void fromSnapshotWithValid(String value) {
        GoodsCode goodsCode = GoodsCode.fromSnapshot(value);

        assertThat(goodsCode.getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("fromSnapshot은 null/blank DB 레거시 값도 검증 없이 통과한다")
    void fromSnapshotWithBlank(String value) {
        GoodsCode goodsCode = GoodsCode.fromSnapshot(value);

        assertThat(goodsCode.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("같은 값으로 생성한 GoodsCode는 동등하다")
    void equality() {
        GoodsCode a = GoodsCode.of("G001");
        GoodsCode b = GoodsCode.of("G001");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
