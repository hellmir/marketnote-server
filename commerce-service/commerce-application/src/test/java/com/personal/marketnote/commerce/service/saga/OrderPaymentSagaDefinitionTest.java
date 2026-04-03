package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext;
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

@DisplayName("OrderPaymentSagaDefinition 테스트")
class OrderPaymentSagaDefinitionTest {

    private OrderPaymentSagaDefinition definition;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        definition = new OrderPaymentSagaDefinition(objectMapper);
    }

    @Nested
    @DisplayName("getSagaType")
    class GetSagaType {

        @Test
        @DisplayName("SAGA 타입은 ORDER_PAYMENT이다")
        void shouldReturnOrderPaymentSagaType() {
            // when
            String sagaType = definition.getSagaType();

            // then
            assertThat(sagaType).isEqualTo("ORDER_PAYMENT");
        }
    }

    @Nested
    @DisplayName("getContextType")
    class GetContextType {

        @Test
        @DisplayName("컨텍스트 타입은 OrderPaymentSagaContext이다")
        void shouldReturnContextType() {
            // when
            Class<OrderPaymentSagaContext> contextType = definition.getContextType();

            // then
            assertThat(contextType).isEqualTo(OrderPaymentSagaContext.class);
        }
    }

    @Nested
    @DisplayName("getSteps")
    class GetSteps {

        @Test
        @DisplayName("3개의 스텝이 정의되어 있다")
        void shouldHaveThreeSteps() {
            // when
            List<SagaStepDefinition<OrderPaymentSagaContext>> steps = definition.getSteps();

            // then
            assertThat(steps).hasSize(3);
        }

        @Test
        @DisplayName("첫 번째 스텝은 DEDUCT_INVENTORY이다")
        void shouldHaveDeductInventoryAsFirstStep() {
            // when
            SagaStepDefinition<OrderPaymentSagaContext> firstStep = definition.getSteps().get(0);

            // then
            assertThat(firstStep.stepName()).isEqualTo("DEDUCT_INVENTORY");
            assertThat(firstStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_INVENTORY);
            assertThat(firstStep.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("두 번째 스텝은 RECORD_LEDGER이다")
        void shouldHaveRecordLedgerAsSecondStep() {
            // when
            SagaStepDefinition<OrderPaymentSagaContext> secondStep = definition.getSteps().get(1);

            // then
            assertThat(secondStep.stepName()).isEqualTo("RECORD_LEDGER");
            assertThat(secondStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_LEDGER);
            assertThat(secondStep.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("세 번째 스텝은 PUBLISH_PAYMENT_COMPLETED이며 보상이 없다")
        void shouldHavePublishPaymentCompletedAsLastStepWithoutCompensation() {
            // when
            SagaStepDefinition<OrderPaymentSagaContext> thirdStep = definition.getSteps().get(2);

            // then
            assertThat(thirdStep.stepName()).isEqualTo("PUBLISH_PAYMENT_COMPLETED");
            assertThat(thirdStep.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_COMPLETED);
            assertThat(thirdStep.hasCompensation()).isFalse();
        }
    }

    @Nested
    @DisplayName("action 메시지 생성")
    class ActionMessages {

        private OrderPaymentSagaContext context;

        @BeforeEach
        void setUp() {
            context = new OrderPaymentSagaContext(
                    1L, "order-key-1", 100L, 50000L, 60000L, 10000L, 500L,
                    List.of(
                            new OrderProductItem(10L, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 2, 25000L),
                            new OrderProductItem(20L, null, 1, 10000L)
                    )
            );
        }

        @Test
        @DisplayName("재고 차감 액션 메시지에 orderId와 orderProducts가 포함된다")
        void shouldBuildInventoryDeductionAction() {
            // when
            String actionJson = definition.getSteps().get(0).action().apply(context);

            // then
            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"orderProducts\"");
            assertThat(actionJson).contains("\"pricePolicyId\"");
        }

        @Test
        @DisplayName("분개 액션 메시지에 orderId와 paymentAmount가 포함된다")
        void shouldBuildLedgerRecordAction() {
            // when
            String actionJson = definition.getSteps().get(1).action().apply(context);

            // then
            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"paymentAmount\"");
            assertThat(actionJson).contains("50000");
        }

        @Test
        @DisplayName("결제 완료 액션 메시지에 orderId, buyerId, totalAmount, orderProducts가 포함된다")
        void shouldBuildPaymentCompletedAction() {
            // when
            String actionJson = definition.getSteps().get(2).action().apply(context);

            // then
            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"totalAmount\"");
            assertThat(actionJson).contains("\"orderProducts\"");
            assertThat(actionJson).contains("\"totalAccumulatedPoint\"");
        }
    }

    @Nested
    @DisplayName("compensation 메시지 생성")
    class CompensationMessages {

        private OrderPaymentSagaContext context;

        @BeforeEach
        void setUp() {
            context = new OrderPaymentSagaContext(
                    1L, "order-key-1", 100L, 50000L, 60000L, 10000L, 500L,
                    List.of(new OrderProductItem(10L, null, 2, 25000L))
            );
        }

        @Test
        @DisplayName("재고 복구 보상 메시지에 orderId와 orderProducts가 포함된다")
        void shouldBuildInventoryRestorationCompensation() {
            // when
            String compensationJson = definition.getSteps().get(0).compensation().apply(context);

            // then
            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"orderProducts\"");
        }

        @Test
        @DisplayName("역분개 보상 메시지에 orderId, cancelAmount, idempotencyKey가 포함된다")
        void shouldBuildLedgerReverseCompensation() {
            // when
            String compensationJson = definition.getSteps().get(1).compensation().apply(context);

            // then
            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"cancelAmount\"");
            assertThat(compensationJson).contains("\"idempotencyKey\"");
            assertThat(compensationJson).contains("SAGA_COMP:1");
        }
    }
}
