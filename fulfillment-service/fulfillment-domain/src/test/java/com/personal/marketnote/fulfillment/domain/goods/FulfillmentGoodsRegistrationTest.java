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

    private static final String VALID_FASSTO_CODE = "0000000000000000000001";

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("productKey와 fasstoProductCode가 유효하면 정상 생성된다")
        void shouldCreateWhenProductKeyAndFasstoProductCodeAreValid() {
            // given
            UUID productKey = UUID.randomUUID();
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productKey(productKey)
                    .fasstoProductCode(VALID_FASSTO_CODE)
                    .build();

            // when
            FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(state);

            // then
            assertThat(registration.getProductKey()).isEqualTo(productKey);
            assertThat(registration.getFasstoProductCode()).isEqualTo(VALID_FASSTO_CODE);
        }

        @Test
        @DisplayName("productKey가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowExceptionWhenProductKeyIsNull() {
            // given
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productKey(null)
                    .fasstoProductCode(VALID_FASSTO_CODE)
                    .build();

            // when & then
            assertThatThrownBy(() -> FulfillmentGoodsRegistration.from(state))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("fasstoProductCode가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowExceptionWhenFasstoProductCodeIsNull() {
            // given
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productKey(UUID.randomUUID())
                    .fasstoProductCode(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> FulfillmentGoodsRegistration.from(state))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("fasstoProductCode가 공백이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowExceptionWhenFasstoProductCodeIsBlank() {
            // given
            FulfillmentGoodsRegistrationCreateState state = FulfillmentGoodsRegistrationCreateState.builder()
                    .productKey(UUID.randomUUID())
                    .fasstoProductCode("   ")
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
                    .fasstoProductCode(VALID_FASSTO_CODE)
                    .createdAt(createdAt)
                    .build();

            // when
            FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(state);

            // then
            assertThat(registration.getId()).isEqualTo(100L);
            assertThat(registration.getProductKey()).isEqualTo(productKey);
            assertThat(registration.getFasstoProductCode()).isEqualTo(VALID_FASSTO_CODE);
            assertThat(registration.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
