package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoCargoStatusMapper 테스트")
class FasstoCargoStatusMapperTest {

    @ParameterizedTest
    @ValueSource(strings = {"출고요청", "출고지시", "피킹중"})
    @DisplayName("출고요청/출고지시/피킹중은 PREPARING으로 매핑된다")
    void preparingStatuses(String crgStNm) {
        assertThat(FasstoCargoStatusMapper.toShippingStatus(crgStNm)).isEqualTo(ShippingStatus.PREPARING);
    }

    @Test
    @DisplayName("배송완료는 DELIVERED로 매핑된다")
    void deliveredStatus() {
        assertThat(FasstoCargoStatusMapper.toShippingStatus("배송완료")).isEqualTo(ShippingStatus.DELIVERED);
    }

    @Test
    @DisplayName("배송불가는 DELIVERY_FAILED로 매핑된다")
    void deliveryFailedStatus() {
        assertThat(FasstoCargoStatusMapper.toShippingStatus("배송불가")).isEqualTo(ShippingStatus.DELIVERY_FAILED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"피킹완료", "패킹중", "패킹완료", "출고완료", "미집하", "운송장 등록", "집하완료", "상품출발", "화물도착", "배송출발", "배송중"})
    @DisplayName("출고요청/출고지시/피킹중/배송완료/배송불가가 아닌 상태는 모두 SHIPPING으로 매핑된다")
    void shippingStatuses(String crgStNm) {
        assertThat(FasstoCargoStatusMapper.toShippingStatus(crgStNm)).isEqualTo(ShippingStatus.SHIPPING);
    }

    @Test
    @DisplayName("null이면 PREPARING으로 매핑된다")
    void nullStatus() {
        assertThat(FasstoCargoStatusMapper.toShippingStatus(null)).isEqualTo(ShippingStatus.PREPARING);
    }

    @Test
    @DisplayName("빈 문자열이면 PREPARING으로 매핑된다")
    void emptyStatus() {
        assertThat(FasstoCargoStatusMapper.toShippingStatus("")).isEqualTo(ShippingStatus.PREPARING);
    }

    @Test
    @DisplayName("알 수 없는 상태명은 SHIPPING으로 매핑된다")
    void unknownStatus() {
        assertThat(FasstoCargoStatusMapper.toShippingStatus("알수없는상태")).isEqualTo(ShippingStatus.SHIPPING);
    }
}
