package com.personal.marketnote.commerce.service.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext.OrderProductItem;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.saga.SagaStepDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderPaymentCompletedSagaDefinition 테스트")
class OrderPaymentCompletedSagaDefinitionTest {

    private OrderPaymentCompletedSagaDefinition definition;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        definition = new OrderPaymentCompletedSagaDefinition(objectMapper);
    }

    @Nested
    @DisplayName("getSagaType")
    class GetSagaType {

        @Test
        @DisplayName("SAGA 타입은 ORDER_PAYMENT_COMPLETED이다")
        void shouldReturnOrderPaymentCompletedSagaType() {
            String sagaType = definition.getSagaType();

            assertThat(sagaType).isEqualTo("ORDER_PAYMENT_COMPLETED");
        }
    }

    @Nested
    @DisplayName("getContextType")
    class GetContextType {

        @Test
        @DisplayName("컨텍스트 타입은 OrderPaymentCompletedSagaContext이다")
        void shouldReturnContextType() {
            Class<OrderPaymentCompletedSagaContext> contextType = definition.getContextType();

            assertThat(contextType).isEqualTo(OrderPaymentCompletedSagaContext.class);
        }
    }

    @Nested
    @DisplayName("getSteps")
    class GetSteps {

        @Test
        @DisplayName("5개의 스텝이 정의되어 있다")
        void shouldHaveFiveSteps() {
            List<SagaStepDefinition<OrderPaymentCompletedSagaContext>> steps = definition.getSteps();

            assertThat(steps).hasSize(5);
        }

        @Test
        @DisplayName("첫 번째 스텝은 REGISTER_FULFILLMENT_DELIVERY 이다")
        void shouldHaveFulfillmentAsFirstStep() {
            SagaStepDefinition<OrderPaymentCompletedSagaContext> step = definition.getSteps().get(0);

            assertThat(step.stepName()).isEqualTo("REGISTER_FULFILLMENT_DELIVERY");
            assertThat(step.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_FULFILLMENT);
            assertThat(step.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("두 번째 스텝은 DEDUCT_ORDER_POINT 이다")
        void shouldHaveOrderPointAsSecondStep() {
            SagaStepDefinition<OrderPaymentCompletedSagaContext> step = definition.getSteps().get(1);

            assertThat(step.stepName()).isEqualTo("DEDUCT_ORDER_POINT");
            assertThat(step.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_ORDER_POINT);
            assertThat(step.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("세 번째 스텝은 ACCRUE_SHARED_POINT 이다")
        void shouldHaveSharedPointAsThirdStep() {
            SagaStepDefinition<OrderPaymentCompletedSagaContext> step = definition.getSteps().get(2);

            assertThat(step.stepName()).isEqualTo("ACCRUE_SHARED_POINT");
            assertThat(step.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_SHARED_POINT);
            assertThat(step.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("네 번째 스텝은 ACCRUE_PRODUCT_POINT 이다")
        void shouldHaveProductPointAsFourthStep() {
            SagaStepDefinition<OrderPaymentCompletedSagaContext> step = definition.getSteps().get(3);

            assertThat(step.stepName()).isEqualTo("ACCRUE_PRODUCT_POINT");
            assertThat(step.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_PRODUCT_POINT);
            assertThat(step.hasCompensation()).isTrue();
        }

        @Test
        @DisplayName("다섯 번째 스텝은 DELETE_CART 이며 보상이 없다")
        void shouldHaveCartAsLastStepWithoutCompensation() {
            SagaStepDefinition<OrderPaymentCompletedSagaContext> step = definition.getSteps().get(4);

            assertThat(step.stepName()).isEqualTo("DELETE_CART");
            assertThat(step.topic()).isEqualTo(KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_CART);
            assertThat(step.hasCompensation()).isFalse();
        }
    }

    @Nested
    @DisplayName("action 메시지 생성")
    class ActionMessages {

        private OrderPaymentCompletedSagaContext context;

        @BeforeEach
        void setUp() {
            context = OrderPaymentCompletedSagaContext.builder()
                    .orderId(1L)
                    .buyerId(100L)
                    .totalAmount(60000L)
                    .pointAmount(10000L)
                    .totalAccumulatedPoint(500L)
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .zipCode("06236")
                    .address("서울특별시 강남구 테헤란로 100")
                    .addressDetail("101호")
                    .deliveryRequestMessage("부재시 경비실")
                    .orderProducts(List.of(
                            new OrderProductItem(10L, 1000L, null,
                                    UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 2, 25000L),
                            new OrderProductItem(20L, 2000L, null, null, 1, 10000L)
                    ))
                    .build();
        }

        @Test
        @DisplayName("풀필먼트 출고 등록 액션 메시지에 orderId, buyerId, 수취인, 주소, 상품이 포함된다")
        void shouldBuildFulfillmentAction() {
            String actionJson = definition.getSteps().get(0).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"recipientName\"");
            assertThat(actionJson).contains("\"recipientPhoneNumber\"");
            assertThat(actionJson).contains("\"zipCode\"");
            assertThat(actionJson).contains("\"address\"");
            assertThat(actionJson).contains("\"addressDetail\"");
            assertThat(actionJson).contains("\"deliveryRequestMessage\"");
            assertThat(actionJson).contains("\"orderProducts\"");
            assertThat(actionJson).contains("\"productId\"");
        }

        @Test
        @DisplayName("주문 포인트 차감 액션 메시지에 orderId, buyerId, pointAmount가 포함된다")
        void shouldBuildOrderPointAction() {
            String actionJson = definition.getSteps().get(1).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"pointAmount\"");
            assertThat(actionJson).contains("10000");
        }

        @Test
        @DisplayName("공유 적립 예정 포인트 액션 메시지에 orderId, buyerId, totalAmount, sharerKeys가 포함된다")
        void shouldBuildSharedPointAction() {
            String actionJson = definition.getSteps().get(2).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"totalAmount\"");
            assertThat(actionJson).contains("\"sharerKeys\"");
            assertThat(actionJson).contains("550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("상품 적립 예정 포인트 액션 메시지에 orderId, buyerId, totalAmount, totalAccumulatedPoint가 포함된다")
        void shouldBuildProductPointAction() {
            String actionJson = definition.getSteps().get(3).action().apply(context);

            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"totalAmount\"");
            assertThat(actionJson).contains("\"totalAccumulatedPoint\"");
            assertThat(actionJson).contains("500");
        }

        @Test
        @DisplayName("장바구니 삭제 액션 메시지에 buyerId, orderId, pricePolicyIds가 포함된다")
        void shouldBuildCartAction() {
            String actionJson = definition.getSteps().get(4).action().apply(context);

            assertThat(actionJson).contains("\"buyerId\"");
            assertThat(actionJson).contains("\"orderId\"");
            assertThat(actionJson).contains("\"pricePolicyIds\"");
            assertThat(actionJson).contains("10");
            assertThat(actionJson).contains("20");
        }
    }

    @Nested
    @DisplayName("compensation 메시지 생성")
    class CompensationMessages {

        private OrderPaymentCompletedSagaContext context;

        @BeforeEach
        void setUp() {
            context = OrderPaymentCompletedSagaContext.builder()
                    .orderId(1L)
                    .buyerId(100L)
                    .totalAmount(60000L)
                    .pointAmount(10000L)
                    .totalAccumulatedPoint(500L)
                    .orderProducts(List.of(
                            new OrderProductItem(10L, 1000L, null,
                                    UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), 2, 25000L)
                    ))
                    .build();
        }

        @Test
        @DisplayName("풀필먼트 출고 취소 보상 메시지에 orderId가 포함된다")
        void shouldBuildFulfillmentCompensation() {
            String compensationJson = definition.getSteps().get(0).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("1");
        }

        @Test
        @DisplayName("주문 포인트 환원 보상 메시지에 orderId, buyerId, pointAmount가 포함된다")
        void shouldBuildOrderPointCompensation() {
            String compensationJson = definition.getSteps().get(1).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"buyerId\"");
            assertThat(compensationJson).contains("\"pointAmount\"");
        }

        @Test
        @DisplayName("공유 적립 예정 포인트 보상 메시지에 orderId, buyerId, sharerKeys가 포함된다")
        void shouldBuildSharedPointCompensation() {
            String compensationJson = definition.getSteps().get(2).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"buyerId\"");
            assertThat(compensationJson).contains("\"sharerKeys\"");
        }

        @Test
        @DisplayName("상품 적립 예정 포인트 보상 메시지에 orderId, buyerId가 포함된다")
        void shouldBuildProductPointCompensation() {
            String compensationJson = definition.getSteps().get(3).compensation().apply(context);

            assertThat(compensationJson).contains("\"orderId\"");
            assertThat(compensationJson).contains("\"buyerId\"");
        }

        @Test
        @DisplayName("장바구니 삭제 스텝은 보상이 null 이다")
        void shouldReturnNullCompensationForCart() {
            SagaStepDefinition<OrderPaymentCompletedSagaContext> cartStep = definition.getSteps().get(4);

            assertThat(cartStep.compensation()).isNull();
            assertThat(cartStep.hasCompensation()).isFalse();
        }
    }
}
