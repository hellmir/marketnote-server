package com.personal.marketnote.fulfillment.domain.delivery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FulfillmentWorkStatus 테스트")
class FulfillmentWorkStatusTest {

    @ParameterizedTest
    @EnumSource(value = FulfillmentWorkStatus.class, names = {"NOT_REGISTERED", "REGISTERED", "PICKING"})
    @DisplayName("출고 미접수/접수/피킹 진행 중 상태는 취소 가능하다")
    void cancellableStatuses(FulfillmentWorkStatus status) {
        assertThat(status.isCancellable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = FulfillmentWorkStatus.class, names = {"PICKED", "PACKING", "RELEASED"})
    @DisplayName("피킹 완료/포장 중/출고 완료 상태는 취소 불가하다")
    void nonCancellableStatuses(FulfillmentWorkStatus status) {
        assertThat(status.isCancellable()).isFalse();
    }
}
