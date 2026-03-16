package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.fulfillment.configuration.FasstoAuthProperties;
import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;
import com.personal.marketnote.fulfillment.exception.FasstoAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFasstoGoodsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFasstoAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFasstoGoodsUseCase;
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
    private UpdateFasstoGoodsUseCase updateFasstoGoodsUseCase;

    @Mock
    private RequestFasstoAuthUseCase requestFasstoAuthUseCase;

    @Mock
    private FasstoAuthProperties fasstoAuthProperties;

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

    private FasstoAccessToken buildValidAccessToken() {
        return FasstoAccessToken.of("valid-token", "20261231235959");
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 Fassto 상품 수정 UseCase를 호출하고 acknowledge한다")
    void handleEvent_success_updatesFasstoGoodsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(10L, "테스트 상품", "1", "01");
        when(requestFasstoAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<UpdateFasstoGoodsCommand> captor = ArgumentCaptor.forClass(UpdateFasstoGoodsCommand.class);
        verify(updateFasstoGoodsUseCase).updateGoods(captor.capture());

        UpdateFasstoGoodsCommand command = captor.getValue();
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
    @DisplayName("모든 옵션 필드가 포함된 이벤트 수신 시 전체 필드가 매핑된다")
    void handleEvent_allFields_mapsAllFieldsCorrectly() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecordWithAllFields();
        when(requestFasstoAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<UpdateFasstoGoodsCommand> captor = ArgumentCaptor.forClass(UpdateFasstoGoodsCommand.class);
        verify(updateFasstoGoodsUseCase).updateGoods(captor.capture());

        UpdateFasstoGoodsCommand command = captor.getValue();
        assertThat(command.goods().get(0).cstGodCd()).isEqualTo("10");
        assertThat(command.goods().get(0).godNm()).isEqualTo("테스트 상품");
        assertThat(command.goods().get(0).godType()).isEqualTo("2");
        assertThat(command.goods().get(0).giftDiv()).isEqualTo("99");
        assertThat(command.goods().get(0).godOptCd1()).isEqualTo("opt1");
        assertThat(command.goods().get(0).godOptCd2()).isEqualTo("opt2");
        assertThat(command.goods().get(0).invGodNmUseYn()).isEqualTo("Y");
        assertThat(command.goods().get(0).invGodNm()).isEqualTo("상품명");
        assertThat(command.goods().get(0).supCd()).isEqualTo("SUP");
        assertThat(command.goods().get(0).cateCd()).isEqualTo("CATE");
        assertThat(command.goods().get(0).origin()).isEqualTo("KR");
        assertThat(command.goods().get(0).cstGodImgUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(command.goods().get(0).externalGodImgUrl()).isEqualTo("https://example.com/external.jpg");

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
        verifyNoInteractions(requestFasstoAuthUseCase, updateFasstoGoodsUseCase);
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
        verifyNoInteractions(requestFasstoAuthUseCase, updateFasstoGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Fassto 액세스 토큰이 null이면 FasstoAccessTokenIssuanceFailedException을 던진다")
    void handleEvent_nullAccessToken_throwsException() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1", "01");
        when(requestFasstoAuthUseCase.requestAccessToken()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> consumer.handleProductUpdatedEvent(record, acknowledgment))
                .isInstanceOf(FasstoAccessTokenIssuanceFailedException.class);

        verifyNoInteractions(updateFasstoGoodsUseCase);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("UpdateFasstoGoodsFailedException 발생 시 warn 로그 후 acknowledge한다")
    void handleEvent_updateFasstoGoodsFailed_warnsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1", "01");
        when(requestFasstoAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new UpdateFasstoGoodsFailedException(new IOException("Fassto API 오류")))
                .when(updateFasstoGoodsUseCase).updateGoods(any(UpdateFasstoGoodsCommand.class));

        // when
        consumer.handleProductUpdatedEvent(record, acknowledgment);

        // then
        verify(updateFasstoGoodsUseCase).updateGoods(any(UpdateFasstoGoodsCommand.class));
        verify(acknowledgment).acknowledge();
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
        verifyNoInteractions(requestFasstoAuthUseCase, updateFasstoGoodsUseCase);
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
        verifyNoInteractions(requestFasstoAuthUseCase, updateFasstoGoodsUseCase);
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
        verifyNoInteractions(requestFasstoAuthUseCase, updateFasstoGoodsUseCase);
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
        verifyNoInteractions(requestFasstoAuthUseCase, updateFasstoGoodsUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleEvent_unexpectedException_propagatesForRetry() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, "테스트", "1", "01");
        when(requestFasstoAuthUseCase.requestAccessToken()).thenReturn(buildValidAccessToken());
        when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
        doThrow(new RuntimeException("네트워크 오류"))
                .when(updateFasstoGoodsUseCase).updateGoods(any(UpdateFasstoGoodsCommand.class));

        // when & then
        assertThatThrownBy(() -> consumer.handleProductUpdatedEvent(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("네트워크 오류");

        verify(updateFasstoGoodsUseCase).updateGoods(any(UpdateFasstoGoodsCommand.class));
        verify(acknowledgment, never()).acknowledge();
    }
}
