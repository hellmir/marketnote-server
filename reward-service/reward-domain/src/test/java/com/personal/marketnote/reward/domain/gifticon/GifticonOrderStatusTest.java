package com.personal.marketnote.reward.domain.gifticon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GifticonOrderStatusTest {

    @Nested
    @DisplayName("술어 메서드")
    class PredicateMethods {

        @Test
        @DisplayName("PENDING 상태는 isPending이 true이다")
        void pendingIsPending() {
            assertThat(GifticonOrderStatus.PENDING.isPending()).isTrue();
            assertThat(GifticonOrderStatus.ISSUED.isPending()).isFalse();
        }

        @Test
        @DisplayName("ISSUED 상태는 isIssued이 true이다")
        void issuedIsIssued() {
            assertThat(GifticonOrderStatus.ISSUED.isIssued()).isTrue();
            assertThat(GifticonOrderStatus.PENDING.isIssued()).isFalse();
        }

        @Test
        @DisplayName("USED 상태는 isUsed이 true이다")
        void usedIsUsed() {
            assertThat(GifticonOrderStatus.USED.isUsed()).isTrue();
            assertThat(GifticonOrderStatus.ISSUED.isUsed()).isFalse();
        }

        @Test
        @DisplayName("EXPIRED 상태는 isExpired이 true이다")
        void expiredIsExpired() {
            assertThat(GifticonOrderStatus.EXPIRED.isExpired()).isTrue();
            assertThat(GifticonOrderStatus.ISSUED.isExpired()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 상태는 isCancelled이 true이다")
        void cancelledIsCancelled() {
            assertThat(GifticonOrderStatus.CANCELLED.isCancelled()).isTrue();
            assertThat(GifticonOrderStatus.ISSUED.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("SEND_FAILED 상태는 isSendFailed이 true이다")
        void sendFailedIsSendFailed() {
            assertThat(GifticonOrderStatus.SEND_FAILED.isSendFailed()).isTrue();
            assertThat(GifticonOrderStatus.ISSUED.isSendFailed()).isFalse();
        }

        @Test
        @DisplayName("ISSUED 상태만 isAvailable이 true이다")
        void onlyIssuedIsAvailable() {
            assertThat(GifticonOrderStatus.ISSUED.isAvailable()).isTrue();
            assertThat(GifticonOrderStatus.PENDING.isAvailable()).isFalse();
            assertThat(GifticonOrderStatus.USED.isAvailable()).isFalse();
            assertThat(GifticonOrderStatus.EXPIRED.isAvailable()).isFalse();
            assertThat(GifticonOrderStatus.CANCELLED.isAvailable()).isFalse();
            assertThat(GifticonOrderStatus.SEND_FAILED.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("USED, EXPIRED, CANCELLED 상태는 isTerminal이 true이다")
        void terminalStatuses() {
            assertThat(GifticonOrderStatus.USED.isTerminal()).isTrue();
            assertThat(GifticonOrderStatus.EXPIRED.isTerminal()).isTrue();
            assertThat(GifticonOrderStatus.CANCELLED.isTerminal()).isTrue();
            assertThat(GifticonOrderStatus.PENDING.isTerminal()).isFalse();
            assertThat(GifticonOrderStatus.ISSUED.isTerminal()).isFalse();
            assertThat(GifticonOrderStatus.SEND_FAILED.isTerminal()).isFalse();
        }
    }

    @Nested
    @DisplayName("핀상태 코드 매핑 (fromPinStatus)")
    class FromPinStatus {

        @ParameterizedTest
        @ValueSource(strings = {"01", "06"})
        @DisplayName("핀상태 01(발행), 06(재발행)은 ISSUED로 매핑된다")
        void issuedPinStatuses(String pinStatus) {
            assertThat(GifticonOrderStatus.fromPinStatus(pinStatus)).isEqualTo(GifticonOrderStatus.ISSUED);
        }

        @Test
        @DisplayName("핀상태 02(교환)는 USED로 매핑된다")
        void usedPinStatus() {
            assertThat(GifticonOrderStatus.fromPinStatus("02")).isEqualTo(GifticonOrderStatus.USED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"08", "11"})
        @DisplayName("핀상태 08(기간만료), 11(잔액기간만료)은 EXPIRED로 매핑된다")
        void expiredPinStatuses(String pinStatus) {
            assertThat(GifticonOrderStatus.fromPinStatus(pinStatus)).isEqualTo(GifticonOrderStatus.EXPIRED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"03", "05", "07", "10"})
        @DisplayName("핀상태 03(반품), 05(환불), 07(구매취소), 10(잔액환불)은 CANCELLED로 매핑된다")
        void cancelledPinStatuses(String pinStatus) {
            assertThat(GifticonOrderStatus.fromPinStatus(pinStatus)).isEqualTo(GifticonOrderStatus.CANCELLED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"04", "09", "12", "99"})
        @DisplayName("매핑되지 않는 핀상태 코드는 CANCELLED로 매핑된다")
        void unmappedPinStatusesFallbackToCancelled(String pinStatus) {
            assertThat(GifticonOrderStatus.fromPinStatus(pinStatus)).isEqualTo(GifticonOrderStatus.CANCELLED);
        }
    }
}
