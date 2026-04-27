package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.saga.SagaStepDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderCancelSagaDefinition 테스트")
class OrderCancelSagaDefinitionTest {

    private OrderCancelSagaDefinition definition;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        definition = new OrderCancelSagaDefinition(objectMapper);
    }

    @Nested
    @DisplayName("getSagaType")
    class GetSagaType {

        @Test
        @DisplayName("SAGA 타입은 ORDER_CANCEL이다")
        void shouldReturnOrderCancelSagaType() {
            String sagaType = definition.getSagaType();

            assertThat(sagaType).isEqualTo("ORDER_CANCEL");
        }
    }

    @Nested
    @DisplayName("getContextType")
    class GetContextType {

        @Test
        @DisplayName("컨텍스트 타입은 OrderCancelSagaContext이다")
        void shouldReturnContextType() {
            Class<OrderCancelSagaContext> contextType = definition.getContextType();

            assertThat(contextType).isEqualTo(OrderCancelSagaContext.class);
        }
    }

    @Nested
    @DisplayName("getSteps")
    class GetSteps {

        @Test
        @DisplayName("3개의 스텝이 정의되어 있다")
        void shouldHaveThreeSteps() {
            List<SagaStepDefinition<OrderCancelSagaContext>> steps = definition.getSteps();

            assertThat(steps).hasSize(3);
        }

        @Test
        @DisplayName("첫 번째 스텝은 CANCEL_FULFILLMENT_RELEASE이며 보상이 없다")
        void shouldHaveCancelFulfillmentReleaseAsFirstStepWithoutCompensation() {
            SagaStepDefinition<OrderCancelSagaContext> firstStep = definition.getSteps().get(0);

            assertThat(firstStep.stepName()).isEqualTo("CANCEL_FULFILLMENT_RELEASE");
            assertThat(firstStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_CANCEL_FULFILLMENT);
            assertThat(firstStep.hasCompensation()).isFalse();
        }

        @Test
        @DisplayName("두 번째 스텝은 REFUND_PAYMENT이며 보상이 없다")
        void shouldHaveRefundPaymentAsSecondStepWithoutCompensation() {
            SagaStepDefinition<OrderCancelSagaContext> secondStep = definition.getSteps().get(1);

            assertThat(secondStep.stepName()).isEqualTo("REFUND_PAYMENT");
            assertThat(secondStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_CANCEL_REFUND);
            assertThat(secondStep.hasCompensation()).isFalse();
        }

        @Test
        @DisplayName("세 번째 스텝은 COMPLETE_CANCELLATION이며 보상이 없다")
        void shouldHaveCompleteCancellationAsLastStepWithoutCompensation() {
            SagaStepDefinition<OrderCancelSagaContext> thirdStep = definition.getSteps().get(2);

            assertThat(thirdStep.stepName()).isEqualTo("COMPLETE_CANCELLATION");
            assertThat(thirdStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_CANCEL_COMPLETED);
            assertThat(thirdStep.hasCompensation()).isFalse();
        }
    }

    @Nested
    @DisplayName("action 메시지 생성")
    class ActionMessages {

        private OrderCancelSagaContext context;

        @BeforeEach
        void setUp() {
            context = new OrderCancelSagaContext(
                    1L, "order-key-1", 100L,
                    50000L, 50000L, 0L, 3000L,
                    true, 0L,
                    "PREPARING", "CANCEL_ORDER", "구매 의사 취소",
                    List.of(
                            new OrderProductItem(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 2, 25000L),
                            new OrderProductItem(20L, null, 1, 10000L)
                    )
            );
        }

        @Test
        @DisplayName("풀필먼트 취소 액션 메시지에 orderId와 originalStatus가 포함된다")
        void shouldBuildFulfillmentCancelAction() {
            String actionJson = definition.getSteps().get(0).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"originalStatus\"");
            assertThat(actionJson).contains("PREPARING");
        }

        @Test
        @DisplayName("PG 환불 액션 메시지에 orderId, orderKey, cancelAmount가 포함된다")
        void shouldBuildRefundPaymentAction() {
            String actionJson = definition.getSteps().get(1).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"orderKey\"");
            assertThat(actionJson).contains("\"cancelAmount\"");
            assertThat(actionJson).contains("50000");
        }

        @Test
        @DisplayName("취소 완료 액션 메시지에 orderId, reasonCategory, orderProducts가 포함된다")
        void shouldBuildCompleteCancellationAction() {
            String actionJson = definition.getSteps().get(2).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"reasonCategory\"");
            assertThat(actionJson).contains("\"orderProducts\"");
            assertThat(actionJson).contains("CANCEL_ORDER");
        }
    }
}
