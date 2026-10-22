package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderReturnSagaContext.ReturnProductItem;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.saga.SagaStepDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderReturnSagaDefinition 테스트")
class OrderReturnSagaDefinitionTest {

    private OrderReturnSagaDefinition definition;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        definition = new OrderReturnSagaDefinition(objectMapper);
    }

    @Nested
    @DisplayName("getSagaType")
    class GetSagaType {

        @Test
        @DisplayName("SAGA 타입은 ORDER_RETURN이다")
        void shouldReturnOrderReturnSagaType() {
            String sagaType = definition.getSagaType();

            assertThat(sagaType).isEqualTo("ORDER_RETURN");
        }
    }

    @Nested
    @DisplayName("getContextType")
    class GetContextType {

        @Test
        @DisplayName("컨텍스트 타입은 OrderReturnSagaContext이다")
        void shouldReturnContextType() {
            Class<OrderReturnSagaContext> contextType = definition.getContextType();

            assertThat(contextType).isEqualTo(OrderReturnSagaContext.class);
        }
    }

    @Nested
    @DisplayName("getSteps")
    class GetSteps {

        @Test
        @DisplayName("6개의 스텝이 정의되어 있다")
        void shouldHaveSixSteps() {
            List<SagaStepDefinition<OrderReturnSagaContext>> steps = definition.getSteps();

            assertThat(steps).hasSize(6);
        }

        @Test
        @DisplayName("첫 번째 스텝은 REFUND_PAYMENT이며 보상이 없다")
        void shouldHaveRefundPaymentAsFirstStepWithoutCompensation() {
            SagaStepDefinition<OrderReturnSagaContext> firstStep = definition.getSteps().get(0);

            assertThat(firstStep.stepName()).isEqualTo("REFUND_PAYMENT");
            assertThat(firstStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_RETURN_REFUND);
            assertThat(firstStep.hasCompensation()).isFalse();
        }

        @Test
        @DisplayName("두 번째 스텝은 RECORD_REVERSE_LEDGER이며 보상이 있다")
        void shouldHaveRecordReverseLedgerAsSecondStepWithCompensation() {
            SagaStepDefinition<OrderReturnSagaContext> secondStep = definition.getSteps().get(1);

            assertThat(secondStep.stepName()).isEqualTo("RECORD_REVERSE_LEDGER");
            assertThat(secondStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_RETURN_LEDGER);
            assertThat(secondStep.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("세 번째 스텝은 CANCEL_SETTLEMENT_ALLOCATION이며 보상이 있다")
        void shouldHaveCancelSettlementAllocationAsThirdStepWithCompensation() {
            SagaStepDefinition<OrderReturnSagaContext> thirdStep = definition.getSteps().get(2);

            assertThat(thirdStep.stepName()).isEqualTo("CANCEL_SETTLEMENT_ALLOCATION");
            assertThat(thirdStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_RETURN_SETTLEMENT);
            assertThat(thirdStep.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("네 번째 스텝은 RESTORE_INVENTORY이며 보상이 있다")
        void shouldHaveRestoreInventoryAsFourthStepWithCompensation() {
            SagaStepDefinition<OrderReturnSagaContext> fourthStep = definition.getSteps().get(3);

            assertThat(fourthStep.stepName()).isEqualTo("RESTORE_INVENTORY");
            assertThat(fourthStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_RETURN_INVENTORY);
            assertThat(fourthStep.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("다섯 번째 스텝은 REFUND_POINT이며 보상이 있다")
        void shouldHaveRefundPointAsFifthStepWithCompensation() {
            SagaStepDefinition<OrderReturnSagaContext> fifthStep = definition.getSteps().get(4);

            assertThat(fifthStep.stepName()).isEqualTo("REFUND_POINT");
            assertThat(fifthStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_RETURN_POINT_REFUND);
            assertThat(fifthStep.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("여섯 번째 스텝은 PUBLISH_RETURN_COMPLETED이며 보상이 없다")
        void shouldHavePublishReturnCompletedAsLastStepWithoutCompensation() {
            SagaStepDefinition<OrderReturnSagaContext> sixthStep = definition.getSteps().get(5);

            assertThat(sixthStep.stepName()).isEqualTo("PUBLISH_RETURN_COMPLETED");
            assertThat(sixthStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_RETURN_COMPLETED);
            assertThat(sixthStep.hasCompensation()).isFalse();
        }
    }

    @Nested
    @DisplayName("action 메시지 생성")
    class ActionMessages {

        private OrderReturnSagaContext context;

        @BeforeEach
        void setUp() {
            List<ReturnProductItem> returnProducts = List.of(
                    new ReturnProductItem(
                            10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 2, 25000L
                    ),
                    new ReturnProductItem(20L, null, 1, 10000L)
            );
            context = new OrderReturnSagaContext(
                    1L, "order-key-1", 100L,
                    50000L, 60000L, 5000L, 3000L,
                    true, 0L, 5000L,
                    returnProducts, "RET-SAGA-001"
            );
        }

        @Test
        @DisplayName("PG 환불 액션 메시지에 orderId, orderKey, returnAmount가 포함된다")
        void shouldBuildRefundPaymentAction() {
            String actionJson = definition.getSteps().get(0).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"orderKey\"");
            assertThat(actionJson).contains("\"returnAmount\"");
            assertThat(actionJson).contains("\"isFullReturn\"");
        }

        @Test
        @DisplayName("역분개 액션 메시지에 orderId, returnAmount, idempotencyKey가 포함된다")
        void shouldBuildReverseLedgerAction() {
            String actionJson = definition.getSteps().get(1).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"returnAmount\"");
            assertThat(actionJson).contains("\"idempotencyKey\"");
            assertThat(actionJson).contains("SAGA_ORDER_RETURN:1");
        }

        @Test
        @DisplayName("역분개 보상 메시지에 orderId, paymentAmount, idempotencyKey가 포함된다")
        void shouldBuildReverseLedgerCompensation() {
            String compensationJson = definition.getSteps().get(1).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"paymentAmount\"");
            assertThat(compensationJson).contains("\"idempotencyKey\"");
            assertThat(compensationJson).contains("SAGA_ORDER_RETURN_COMP:1");
        }

        @Test
        @DisplayName("정산 취소 액션 메시지에 orderId가 포함된다")
        void shouldBuildCancelSettlementAction() {
            String actionJson = definition.getSteps().get(2).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
        }

        @Test
        @DisplayName("정산 취소 보상 메시지에 orderId가 포함된다")
        void shouldBuildCancelSettlementCompensation() {
            String compensationJson = definition.getSteps().get(2).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
        }

        @Test
        @DisplayName("재고 복구 액션 메시지에 orderId, returnProducts, isFullReturn이 포함된다")
        void shouldBuildRestoreInventoryAction() {
            String actionJson = definition.getSteps().get(3).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"returnProducts\"");
            assertThat(actionJson).contains("\"isFullReturn\"");
        }

        @Test
        @DisplayName("재고 복구 보상 메시지에 orderId, returnProducts가 포함된다")
        void shouldBuildRestoreInventoryCompensation() {
            String compensationJson = definition.getSteps().get(3).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"returnProducts\"");
        }

        @Test
        @DisplayName("포인트 환불 액션 메시지에 orderId, buyerId, refundPointAmount가 포함된다")
        void shouldBuildRefundPointAction() {
            String actionJson = definition.getSteps().get(4).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"refundPointAmount\"");
        }

        @Test
        @DisplayName("포인트 환불 보상 메시지에 orderId, buyerId, refundPointAmount가 포함된다")
        void shouldBuildRefundPointCompensation() {
            String compensationJson = definition.getSteps().get(4).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"buyerId\"");
            assertThat(compensationJson).contains("\"refundPointAmount\"");
        }

        @Test
        @DisplayName("반품 완료 액션 메시지에 orderId, orderKey, buyerId, returnProducts가 포함된다")
        void shouldBuildReturnCompletedAction() {
            String actionJson = definition.getSteps().get(5).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"orderKey\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"returnProducts\"");
            assertThat(actionJson).contains("\"isFullReturn\"");
        }

        @Test
        @DisplayName("반품 완료 액션 메시지에 컨텍스트의 returnId가 포함된다")
        void shouldBuildReturnCompletedActionWithReturnId() {
            String actionJson = definition.getSteps().get(5).action().apply(context);

            assertThat(actionJson).contains("\"returnId\"");
            assertThat(actionJson).contains("RET-SAGA-001");
        }
    }
}
