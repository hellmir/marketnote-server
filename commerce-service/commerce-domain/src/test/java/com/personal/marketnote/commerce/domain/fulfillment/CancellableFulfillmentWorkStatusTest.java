package com.personal.marketnote.commerce.domain.fulfillment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancellableFulfillmentWorkStatusTest {

    @Nested
    @DisplayName("취소 가능 여부 검증 (isCancellable)")
    class IsCancellableTest {

        @Test
        @DisplayName("NOT_REGISTERED(출고 미접수)는 취소 가능하다")
        void notRegistered_isCancellable() {
            assertThat(CancellableFulfillmentWorkStatus.NOT_REGISTERED.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("REGISTERED(출고 접수)는 취소 가능하다")
        void registered_isCancellable() {
            assertThat(CancellableFulfillmentWorkStatus.REGISTERED.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PICKING(피킹 진행 중)은 취소 가능하다")
        void picking_isCancellable() {
            assertThat(CancellableFulfillmentWorkStatus.PICKING.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("PICKED(피킹 완료)는 취소 불가능하다")
        void picked_isNotCancellable() {
            assertThat(CancellableFulfillmentWorkStatus.PICKED.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("PACKING(포장 중)은 취소 불가능하다")
        void packing_isNotCancellable() {
            assertThat(CancellableFulfillmentWorkStatus.PACKING.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("RELEASED(출고 완료)는 취소 불가능하다")
        void released_isNotCancellable() {
            assertThat(CancellableFulfillmentWorkStatus.RELEASED.isCancellable()).isFalse();
        }
    }

    @Nested
    @DisplayName("풀필먼트 상태 코드 매핑 (from)")
    class FromTest {

        @Test
        @DisplayName("NOT_REGISTERED 문자열로 NOT_REGISTERED enum을 반환한다")
        void from_notRegistered() {
            CancellableFulfillmentWorkStatus status = CancellableFulfillmentWorkStatus.from("NOT_REGISTERED");
            assertThat(status).isEqualTo(CancellableFulfillmentWorkStatus.NOT_REGISTERED);
        }

        @Test
        @DisplayName("REGISTERED 문자열로 REGISTERED enum을 반환한다")
        void from_registered() {
            CancellableFulfillmentWorkStatus status = CancellableFulfillmentWorkStatus.from("REGISTERED");
            assertThat(status).isEqualTo(CancellableFulfillmentWorkStatus.REGISTERED);
        }

        @Test
        @DisplayName("PICKING 문자열로 PICKING enum을 반환한다")
        void from_picking() {
            CancellableFulfillmentWorkStatus status = CancellableFulfillmentWorkStatus.from("PICKING");
            assertThat(status).isEqualTo(CancellableFulfillmentWorkStatus.PICKING);
        }

        @Test
        @DisplayName("PICKED 문자열로 PICKED enum을 반환한다")
        void from_picked() {
            CancellableFulfillmentWorkStatus status = CancellableFulfillmentWorkStatus.from("PICKED");
            assertThat(status).isEqualTo(CancellableFulfillmentWorkStatus.PICKED);
        }

        @Test
        @DisplayName("PACKING 문자열로 PACKING enum을 반환한다")
        void from_packing() {
            CancellableFulfillmentWorkStatus status = CancellableFulfillmentWorkStatus.from("PACKING");
            assertThat(status).isEqualTo(CancellableFulfillmentWorkStatus.PACKING);
        }

        @Test
        @DisplayName("RELEASED 문자열로 RELEASED enum을 반환한다")
        void from_released() {
            CancellableFulfillmentWorkStatus status = CancellableFulfillmentWorkStatus.from("RELEASED");
            assertThat(status).isEqualTo(CancellableFulfillmentWorkStatus.RELEASED);
        }

        @Test
        @DisplayName("유효하지 않은 문자열은 InvalidFulfillmentWorkStatusException을 발생시킨다")
        void from_invalidCode_throwsException() {
            assertThatThrownBy(() -> CancellableFulfillmentWorkStatus.from("INVALID"))
                    .isInstanceOf(InvalidFulfillmentWorkStatusException.class)
                    .hasMessageContaining("ERR_FULFILLMENT_01")
                    .hasMessageContaining("INVALID");
        }

        @Test
        @DisplayName("null은 InvalidFulfillmentWorkStatusException을 발생시킨다")
        void from_null_throwsException() {
            assertThatThrownBy(() -> CancellableFulfillmentWorkStatus.from(null))
                    .isInstanceOf(InvalidFulfillmentWorkStatusException.class)
                    .hasMessageContaining("ERR_FULFILLMENT_01");
        }
    }
}
