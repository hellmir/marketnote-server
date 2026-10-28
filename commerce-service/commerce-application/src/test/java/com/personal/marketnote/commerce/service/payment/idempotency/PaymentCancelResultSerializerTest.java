package com.personal.marketnote.commerce.service.payment.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.service.payment.PaymentCancelContext;
import com.personal.marketnote.common.utility.metrics.CounterRegistryHelper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PaymentCancelResultSerializer 테스트")
class PaymentCancelResultSerializerTest {

    private static final String SERIALIZER_FAILURE_METRIC = "payment.cancel.idempotency.serializer.failure";

    private ObjectMapper objectMapper;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Nested
    @DisplayName("정상 직렬화")
    class HappyPath {

        @Test
        @DisplayName("직렬화에 성공하면 주요 필드가 포함된 JSON 을 반환한다")
        void shouldReturnJsonContainingExpectedFieldsOnSuccess() {
            // given
            PaymentCancelResultSerializer serializer =
                    new PaymentCancelResultSerializer(objectMapper, new CounterRegistryHelper(meterRegistry));
            String orderKey = UUID.randomUUID().toString();
            PaymentCancelContext context = createContext(orderKey, 10000L, 8000L, 2000L, false);

            // when
            String json = serializer.serialize(context);

            // then
            assertThat(json).contains("\"orderKey\":\"" + orderKey + "\"");
            assertThat(json).contains("\"cancelAmount\":8000");
            assertThat(json).contains("\"originalCancelAmount\":10000");
            assertThat(json).contains("\"isFullCancel\":false");
            assertThat(json).contains("\"shippingFeeDeduction\":2000");
            assertThat(json).contains("\"completedAt\":");
            assertThat(counterValue()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("전체 취소 시 cancelId 가 null 인 JSON 을 반환한다")
        void shouldReturnJsonWithNullCancelIdOnFullCancel() {
            // given
            PaymentCancelResultSerializer serializer =
                    new PaymentCancelResultSerializer(objectMapper, new CounterRegistryHelper(meterRegistry));
            String orderKey = UUID.randomUUID().toString();
            PaymentCancelContext context = createContext(orderKey, 50000L, 50000L, 0L, true);

            // when
            String json = serializer.serialize(context);

            // then
            assertThat(json).contains("\"isFullCancel\":true");
            assertThat(json).contains("\"cancelId\":null");
        }
    }

    @Nested
    @DisplayName("직렬화 실패")
    class SerializationFailure {

        @Test
        @DisplayName("JsonProcessingException 발생 시 \"{}\" fallback 을 반환하고 카운터를 1 증가시킨다")
        void shouldReturnFallbackAndIncrementCounterOnFailure() throws JsonProcessingException {
            // given
            ObjectMapper failingMapper = mock(ObjectMapper.class);
            when(failingMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("boom") {});
            PaymentCancelResultSerializer serializer =
                    new PaymentCancelResultSerializer(failingMapper, new CounterRegistryHelper(meterRegistry));
            PaymentCancelContext context = createContext(
                    UUID.randomUUID().toString(), 10000L, 8000L, 2000L, false
            );

            // when
            String json = serializer.serialize(context);

            // then
            assertThat(json).isEqualTo("{}");
            assertThat(counterValue()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("직렬화 실패가 두 번 발생하면 카운터는 2가 된다")
        void shouldAccumulateCounterAcrossMultipleFailures() throws JsonProcessingException {
            // given
            ObjectMapper failingMapper = mock(ObjectMapper.class);
            when(failingMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("boom") {});
            PaymentCancelResultSerializer serializer =
                    new PaymentCancelResultSerializer(failingMapper, new CounterRegistryHelper(meterRegistry));
            PaymentCancelContext context = createContext(
                    UUID.randomUUID().toString(), 10000L, 8000L, 2000L, false
            );

            // when
            serializer.serialize(context);
            serializer.serialize(context);

            // then
            assertThat(counterValue()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("MeterRegistry 가 null 인 경우에도 NPE 없이 fallback 을 반환한다")
        void shouldNotThrowWhenMeterRegistryIsNull() throws JsonProcessingException {
            // given
            ObjectMapper failingMapper = mock(ObjectMapper.class);
            when(failingMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("boom") {});
            PaymentCancelResultSerializer serializer =
                    new PaymentCancelResultSerializer(failingMapper, null);
            PaymentCancelContext context = createContext(
                    UUID.randomUUID().toString(), 10000L, 8000L, 2000L, false
            );

            // when
            String json = serializer.serialize(context);

            // then
            assertThat(json).isEqualTo("{}");
        }
    }

    private double counterValue() {
        Counter counter = meterRegistry.find(SERIALIZER_FAILURE_METRIC).counter();
        if (counter == null) {
            return 0.0;
        }
        return counter.count();
    }

    private PaymentCancelContext createContext(
            String orderKey,
            Long originalCancelAmount,
            Long adjustedCancelAmount,
            Long shippingFeeDeduction,
            boolean isFullCancel
    ) {
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .buyerId(100L)
                .orderKey(orderKey)
                .cancelType(
                        isFullCancel
                                ? CancelPaymentCommand.CancelType.FULL
                                : CancelPaymentCommand.CancelType.PARTIAL
                )
                .cancelAmount(originalCancelAmount)
                .cancelReason("고객 요청")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
        String cancelId = isFullCancel ? null : UUID.randomUUID().toString();
        return PaymentCancelContext.of(
                null, null, null,
                originalCancelAmount, adjustedCancelAmount,
                originalCancelAmount - adjustedCancelAmount,
                0L, shippingFeeDeduction,
                isFullCancel, "고객 요청", "고객 요청",
                cancelId, "tno_123",
                isFullCancel ? "STSC" : "STPC", command
        );
    }
}
