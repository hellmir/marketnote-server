package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentGoodsUseCase;
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
class ProductUpdatedFulfillmentConsumerTest {
    @InjectMocks
    private ProductUpdatedFulfillmentConsumer consumer;

    @Mock
    private UpdateFulfillmentGoodsUseCase updateFulfillmentGoodsUseCase;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @Mock
    private FulfillmentAuthProperties fasstoAuthProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long productId, String productName, String godType, String giftDiv
    ) {
        ProductUpdatedEvent event = new ProductUpdatedEvent(
                productId, productName, godType, giftDiv,
                null, null, null, null, null, null, null, null, null, // godOptCd1 ~ makeYr (9)
                null, null, null, null, null, null, null, null, null, // godPr ~ distTermMgtYn (9)
                null, null, null, null, null, null, null, null, null, // useTermDay ~ useYn (9)
                null, null, null, null                                // safetyStock ~ externalGodImgUrl (4)
        );
        EventEnvelope<ProductUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.updated", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.product.updated", 0, 0L, "key-1", envelope);
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecordWithAllFields() {
        ProductUpdatedEvent event = new ProductUpdatedEvent(
                10L, "테스트 상품", "2", "99",
                "opt1", "opt2", "Y", "상품명", "SUP", "CATE", "2024", "M", "2024",
                "12000", "9000", "10000", "ROOM", "A1", "BARCODE", "2.5", "KR", "Y",
                "30", "7", "7", "BOX", "N", "UP", "COTTON", "Y", "10",
                "N", "1", "https://example.com/image.jpg", "https://example.com/external.jpg"
        );
        EventEnvelope<ProductUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "product.product.updated", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        return new ConsumerRecord<>("product.product.updated", 0, 0L, "key-10", envelope);
    }

    private FulfillmentAccessToken buildValidAccessToken() {
        return FulfillmentAccessToken.of("valid-token", "20261231235959");
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Fulfillment 상품 수정 UseCase를 호출하고 acknowledge한다")
    void handleEvent_success_updatesFulfillmentGoodsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, "테스트 상품", "1", "01");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<UpdateFulfillmentGoodsCommand> captor = ArgumentCaptor.forClass(UpdateFulfillmentGoodsCommand.class);
        verify(updateFulfillmentGoodsUseCase).updateGoods(captor.capture());

        UpdateFulfillmentGoodsCommand command = captor.getValue();
        assertThat(command.customerCode()).isEqualTo("CUST001");
        assertThat(command.accessToken()).isEqualTo("valid-token");
        assertThat(command.goods()).hasSize(1);
        assertThat(command.goods().get(0).productCode()).isEqualTo("10");
        assertThat(command.goods().get(0).productName()).isEqualTo("테스트 상품");
        assertThat(command.goods().get(0).productType()).isEqualTo("1");
        assertThat(command.goods().get(0).giftDivision()).isEqualTo("01");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("모든 옵션 필드가 포함된 이벤트 수신 시 전체 필드가 매핑된다")
    void handleEvent_allFields_mapsAllFieldsCorrectly() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithAllFields();
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<UpdateFulfillmentGoodsCommand> captor = ArgumentCaptor.forClass(UpdateFulfillmentGoodsCommand.class);
        verify(updateFulfillmentGoodsUseCase).updateGoods(captor.capture());

        UpdateFulfillmentGoodsCommand command = captor.getValue();
        assertThat(command.goods().get(0).productCode()).isEqualTo("10");
        assertThat(command.goods().get(0).productName()).isEqualTo("테스트 상품");
        assertThat(command.goods().get(0).productType()).isEqualTo("2");
        assertThat(command.goods().get(0).giftDivision()).isEqualTo("99");
        assertThat(command.goods().get(0).productOptionCode1()).isEqualTo("opt1");
        assertThat(command.goods().get(0).productOptionCode2()).isEqualTo("opt2");
        assertThat(command.goods().get(0).inventoryProductNameUseYn()).isEqualTo("Y");
        assertThat(command.goods().get(0).inventoryProductName()).isEqualTo("상품명");
        assertThat(command.goods().get(0).supplierCode()).isEqualTo("SUP");
        assertThat(command.goods().get(0).categoryCode()).isEqualTo("CATE");
        assertThat(command.goods().get(0).origin()).isEqualTo("KR");
        assertThat(command.goods().get(0).customerProductImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(command.goods().get(0).externalProductImageUrl()).isEqualTo("https://example.com/external.jpg");

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 null이면 상품 수정을 호출하지 않고 acknowledge한다")
    void handleEvent_nullProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, "테스트", "1", "01");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, updateFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productName이 null이면 상품 수정을 호출하지 않고 acknowledge한다")
    void handleEvent_nullProductName_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null, "1", "01");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, updateFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Fulfillment 액세스 토큰이 null이면 FulfillmentAccessTokenIssuanceFailedException을 던진다")
    void handleEvent_nullAccessToken_throwsException() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1", "01");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> consumer.handleProductUpdatedEvent(record, acknowledgment))
                .isInstanceOf(FulfillmentAccessTokenIssuanceFailedException.class);

        verifyNoInteractions(updateFulfillmentGoodsUseCase);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("UpdateFulfillmentGoodsFailedException 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleEvent_updateFulfillmentGoodsFailed_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1", "01");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new UpdateFulfillmentGoodsFailedException(new IOException("Fulfillment API 오류")))
                .when(updateFulfillmentGoodsUseCase).updateGoods(any(UpdateFulfillmentGoodsCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleProductUpdatedEvent(record, acknowledgment))
                .isInstanceOf(UpdateFulfillmentGoodsFailedException.class);

        verify(updateFulfillmentGoodsUseCase).updateGoods(any(UpdateFulfillmentGoodsCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "product.product.updated", 0, 0L, "1", null
        );

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, updateFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ProductUpdatedEvent event = new ProductUpdatedEvent(
                1L, "테스트", "1", "01",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );
        EventEnvelope<ProductUpdatedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "product-service",
                LocalDateTime.of(2026, 2, 27, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "product.product.updated", 0, 0L, "key-1", envelope
        );

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, updateFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 0이면 상품 수정을 호출하지 않고 acknowledge한다")
    void handleEvent_zeroProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, "테스트", "1", "01");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, updateFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("productId가 음수이면 상품 수정을 호출하지 않고 acknowledge한다")
    void handleEvent_negativeProductId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, "테스트", "1", "01");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(requestFulfillmentAuthUseCase, updateFulfillmentGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1", "01");
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new RuntimeException("네트워크 오류"))
                .when(updateFulfillmentGoodsUseCase).updateGoods(any(UpdateFulfillmentGoodsCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleProductUpdatedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("네트워크 오류");

        verify(updateFulfillmentGoodsUseCase).updateGoods(any(UpdateFulfillmentGoodsCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
