package com.personal.marketnote.fulfillment.domain.goods;

import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FulfillmentGoodsRegistration 테스트")
class FulfillmentGoodsRegistrationTest {

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("productKey가 유효하면 정상 생성된다")
        void shouldCreateWhenProductKeyIsValid() {
            // given
            UUID productKey = UUID.randomUUID();
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productKey(productKey)
                    .build();

            // when
            FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(state);

            // then
            assertThat(registration.getProductKey()).isEqualTo(productKey);
        }

        @Test
        @DisplayName("productKey가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowExceptionWhenProductKeyIsNull() {
            // given
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productKey(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> FulfillmentGoodsRegistration.from(state))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 모든 필드가 그대로 복원된다")
        void shouldRestoreAllFieldsFromSnapshotState() {
            // given
            UUID productKey = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 11, 10, 0, 0);
            FulfillmentGoodsRegistrationSnapshotState state = FulfillmentGoodsRegistrationSnapshotState.builder()
                    .id(100L)
                    .productKey(productKey)
                    .createdAt(createdAt)
                    .build();

            // when
            FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(state);

            // then
            assertThat(registration.getId()).isEqualTo(100L);
            assertThat(registration.getProductKey()).isEqualTo(productKey);
            assertThat(registration.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
