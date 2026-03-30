package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRegisteredFulfillmentConsumerTest {
    @InjectMocks
    private ProductRegisteredFulfillmentConsumer consumer;

    @Mock
    private RegisterFulfillmentGoodsUseCase registerFulfillmentGoodsUseCase;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @Mock
    private FulfillmentAuthProperties fasstoAuthProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long productId, String productName, String godType
    ) {
        ProductRegisteredEvent event = new ProductRegisteredEvent(
                productId, 100L, 1L, productName, godType
        );
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.registered", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.product.registered", 0, 0L, "key-1", envelope);
    }

    private FulfillmentAccessToken buildValidAccessToken() {
        return FulfillmentAccessToken.of("valid-token", "20261231235959");
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Fulfillment 상품 등록 UseCase를 호출하고 acknowledge한다")
    void handleEvent_success_registersFulfillmentGoodsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, "테스트 상품", "1");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterFulfillmentGoodsCommand> captor = ArgumentCaptor.forClass(RegisterFulfillmentGoodsCommand.class);
        verify(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(captor.capture());

        RegisterFulfillmentGoodsCommand command = captor.getValue();
        assertThat(command.customerCode()).isEqualTo("CUST001");
        assertThat(command.accessToken()).isEqualTo("valid-token");
        assertThat(command.goods()).hasSize(1);
        assertThat(command.goods().get(0).cstGodCd()).isEqualTo("10");
        assertThat(command.goods().get(0).godNm()).isEqualTo("테스트 상품");
        assertThat(command.goods().get(0).godType()).isEqualTo("1");
        assertThat(command.goods().get(0).giftDiv()).isEqualTo("01");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트의 godType이 있으면 해당 값으로 상품을 등록한다")
    void handleEvent_withCustomGodType_usesProvidedGodType() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(20L, "커스텀 상품", "2");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterFulfillmentGoodsCommand> captor = ArgumentCaptor.forClass(RegisterFulfillmentGoodsCommand.class);
        verify(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(captor.capture());

        assertThat(captor.getValue().goods().get(0).godType()).isEqualTo("2");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트의 godType이 null이면 기본값 '1'로 등록한다")
    void handleEvent_nullGodType_usesDefault() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(30L, "기본 타입 상품", null);
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        ArgumentCaptor<RegisterFulfillmentGoodsCommand> captor = ArgumentCaptor.forClass(RegisterFulfillmentGoodsCommand.class);
        verify(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(captor.capture());

        assertThat(captor.getValue().goods().get(0).godType()).isEqualTo("1");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 상품 등록을 호출하지 않고 acknowledge한다")
    void handleEvent_nullProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "테스트", "1");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, registerFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productName이 null이면 상품 등록을 호출하지 않고 acknowledge한다")
    void handleEvent_nullProductName_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, "1");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, registerFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Fulfillment 액세스 토큰이 null이면 FulfillmentAccessTokenIssuanceFailedException을 던진다")
    void handleEvent_nullAccessToken_throwsException() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> consumer.handleProductRegisteredEvent(record, acknowledgment))
                .isInstanceOf(FulfillmentAccessTokenIssuanceFailedException.class);

        verifyNoInteractions(registerFulfillmentGoodsUseCase);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("RegisterFulfillmentGoodsFailedException 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleEvent_registerFulfillmentGoodsFailed_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new RegisterFulfillmentGoodsFailedException(new IOException("Fulfillment API 오류")))
                .when(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(any(RegisterFulfillmentGoodsCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleProductRegisteredEvent(record, acknowledgment))
                .isInstanceOf(RegisterFulfillmentGoodsFailedException.class);

        verify(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(any(RegisterFulfillmentGoodsCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("FulfillmentGoodsAlreadyRegisteredException 발생 시 warn 로그 후 acknowledge한다")
    void handleEvent_alreadyRegistered_warnsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new FulfillmentGoodsAlreadyRegisteredException(1L))
                .when(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(any(RegisterFulfillmentGoodsCommand.class));

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verify(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(any(RegisterFulfillmentGoodsCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "product.product.registered", 0, 0L, "1", null
        );

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, registerFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ProductRegisteredEvent event = new ProductRegisteredEvent(1L, 100L, 1L, "테스트", "1");
        EventEnvelope<ProductRegisteredEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "product.product.registered", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, registerFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 0이면 상품 등록을 호출하지 않고 acknowledge한다")
    void handleEvent_zeroProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "테스트", "1");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, registerFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 음수이면 상품 등록을 호출하지 않고 acknowledge한다")
    void handleEvent_negativeProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "테스트", "1");

        // when
        consumer.handleProductRegisteredEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, registerFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new RuntimeException("네트워크 오류"))
                .when(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(any(RegisterFulfillmentGoodsCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleProductRegisteredEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("네트워크 오류");

        verify(registerFulfillmentGoodsUseCase).registerGoodsIdempotent(any(RegisterFulfillmentGoodsCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
