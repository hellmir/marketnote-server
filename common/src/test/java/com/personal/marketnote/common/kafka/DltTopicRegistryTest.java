package com.personal.marketnote.common.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DltTopicRegistry 테스트")
class DltTopicRegistryTest {

    @Nested
    @DisplayName("isAllowed")
    class IsAllowed {

        @Test
        @DisplayName("SAGA OrderPayment 스텝 토픽 3개가 DLT 대상에 포함된다")
        void shouldAllowOrderPaymentSagaTopics() {
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_ORDER_PAYMENT_INVENTORY)).isTrue();
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_ORDER_PAYMENT_LEDGER)).isTrue();
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_ORDER_PAYMENT_COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("SAGA OrderCancel 스텝 토픽 3개가 DLT 대상에 포함된다")
        void shouldAllowOrderCancelSagaTopics() {
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_ORDER_CANCEL_FULFILLMENT)).isTrue();
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND)).isTrue();
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_ORDER_CANCEL_COMPLETED)).isTrue();
        }

        @Test
        @DisplayName("SAGA 응답 토픽은 DLT 대상에서 제외된다")
        void shouldNotAllowSagaResponseTopic() {
            assertThat(DltTopicRegistry.isAllowed(KafkaTopicConstants.SAGA_RESPONSE)).isFalse();
        }

        @Test
        @DisplayName("등록되지 않은 임의 토픽은 DLT 대상이 아니다")
        void shouldNotAllowUnregisteredTopic() {
            assertThat(DltTopicRegistry.isAllowed("random.unregistered.topic")).isFalse();
        }
    }

    @Nested
    @DisplayName("toDltTopic")
    class ToDltTopic {

        @Test
        @DisplayName("SAGA 스텝 토픽에 .dlt 접미사가 붙는다")
        void shouldAppendDltSuffix() {
            String dltTopic = DltTopicRegistry.toDltTopic(KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND);

            assertThat(dltTopic).isEqualTo(KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND + KafkaTopicConstants.DLT_SUFFIX);
        }
    }

    @Nested
    @DisplayName("getAllDltTopics")
    class GetAllDltTopics {

        @Test
        @DisplayName("전체 DLT 토픽 목록에 SAGA 스텝 DLT 토픽 6개가 포함된다")
        void shouldContainSagaDltTopics() {
            List<String> dltTopics = DltTopicRegistry.getAllDltTopics();

            assertThat(dltTopics).contains(
                    KafkaTopicConstants.SAGA_ORDER_PAYMENT_INVENTORY + KafkaTopicConstants.DLT_SUFFIX,
                    KafkaTopicConstants.SAGA_ORDER_PAYMENT_LEDGER + KafkaTopicConstants.DLT_SUFFIX,
                    KafkaTopicConstants.SAGA_ORDER_PAYMENT_COMPLETED + KafkaTopicConstants.DLT_SUFFIX,
                    KafkaTopicConstants.SAGA_ORDER_CANCEL_FULFILLMENT + KafkaTopicConstants.DLT_SUFFIX,
                    KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND + KafkaTopicConstants.DLT_SUFFIX,
                    KafkaTopicConstants.SAGA_ORDER_CANCEL_COMPLETED + KafkaTopicConstants.DLT_SUFFIX
            );
        }
    }
}
