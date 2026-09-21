package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidGoodsStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoodsStatusTest {

    @Test
    @DisplayName("SALE 상수에 대해 isSale()이 true를 반환한다")
    void shouldReturnTrueForIsSaleWhenSale() {
        assertThat(GoodsStatus.SALE.isSale()).isTrue();
        assertThat(GoodsStatus.SALE.isSuspended()).isFalse();
    }

    @Test
    @DisplayName("SUSPENDED 상수에 대해 isSale()이 false를 반환한다")
    void shouldReturnFalseForIsSaleWhenSuspended() {
        assertThat(GoodsStatus.SUSPENDED.isSale()).isFalse();
        assertThat(GoodsStatus.SUSPENDED.isSuspended()).isTrue();
    }

    @Test
    @DisplayName("from(\"SALE\") 호출 시 SALE 상수를 반환한다")
    void shouldReturnSaleWhenFromSaleName() {
        assertThat(GoodsStatus.from("SALE")).isEqualTo(GoodsStatus.SALE);
    }

    @Test
    @DisplayName("from(\"SUS\") 호출 시 dbValue 매칭으로 SUSPENDED 상수를 반환한다")
    void shouldReturnSuspendedWhenFromSusDbValue() {
        assertThat(GoodsStatus.from("SUS")).isEqualTo(GoodsStatus.SUSPENDED);
    }

    @Test
    @DisplayName("from(\"SUSPENDED\") 호출 시 enum name 매칭으로 SUSPENDED 상수를 반환한다")
    void shouldReturnSuspendedWhenFromSuspendedName() {
        assertThat(GoodsStatus.from("SUSPENDED")).isEqualTo(GoodsStatus.SUSPENDED);
    }

    @Test
    @DisplayName("미정의 문자열로 from() 호출 시 InvalidGoodsStatusException이 발생한다")
    void shouldThrowWhenUnknownValue() {
        assertThatThrownBy(() -> GoodsStatus.from("UNKNOWN"))
                .isInstanceOf(InvalidGoodsStatusException.class);
    }

    @Test
    @DisplayName("null 입력 시 InvalidGoodsStatusException이 발생한다")
    void shouldThrowWhenNullValue() {
        assertThatThrownBy(() -> GoodsStatus.from(null))
                .isInstanceOf(InvalidGoodsStatusException.class);
    }

    @Test
    @DisplayName("빈 문자열 입력 시 InvalidGoodsStatusException이 발생한다")
    void shouldThrowWhenEmptyValue() {
        assertThatThrownBy(() -> GoodsStatus.from(""))
                .isInstanceOf(InvalidGoodsStatusException.class);
    }

    @Test
    @DisplayName("getDbValue()가 SALE은 \"SALE\"을, SUSPENDED는 \"SUS\"를 반환한다")
    void shouldReturnDbValueCorrectly() {
        assertThat(GoodsStatus.SALE.getDbValue()).isEqualTo("SALE");
        assertThat(GoodsStatus.SUSPENDED.getDbValue()).isEqualTo("SUS");
    }
}
