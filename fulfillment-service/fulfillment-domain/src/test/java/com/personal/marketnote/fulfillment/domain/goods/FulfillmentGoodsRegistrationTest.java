package com.personal.marketnote.fulfillment.domain.goods;

import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FulfillmentGoodsRegistration 테스트")
class FulfillmentGoodsRegistrationTest {

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("productId가 유효하면 정상 생성된다")
        void shouldCreateWhenProductIdIsValid() {
            // given
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productId(1L)
                    .build();

            // when
            FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(state);

            // then
            assertThat(registration.getProductId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("productId가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowExceptionWhenProductIdIsNull() {
            // given
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productId(null)
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
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 11, 10, 0, 0);
            FulfillmentGoodsRegistrationSnapshotState state = FulfillmentGoodsRegistrationSnapshotState.builder()
                    .id(100L)
                    .productId(200L)
                    .createdAt(createdAt)
                    .build();

            // when
            FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(state);

            // then
            assertThat(registration.getId()).isEqualTo(100L);
            assertThat(registration.getProductId()).isEqualTo(200L);
            assertThat(registration.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
