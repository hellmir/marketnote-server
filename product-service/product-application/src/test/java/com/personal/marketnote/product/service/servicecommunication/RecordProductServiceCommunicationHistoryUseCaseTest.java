package com.personal.marketnote.product.service.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.product.domain.servicecommunication.*;
import com.personal.marketnote.product.port.in.command.servicecommunication.ProductServiceCommunicationHistoryCommand;
import com.personal.marketnote.product.port.out.servicecommunication.SaveProductServiceCommunicationHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordProductServiceCommunicationHistoryUseCaseTest {

    @Mock
    private SaveProductServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @InjectMocks
    private RecordProductServiceCommunicationHistoryService recordService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 모든 필드가 정확히 매핑되어 저장된다")
    void record_withAllFields_savesWithAllFieldsMapped() {
        // given
        JsonNode payloadJson = objectMapper.createObjectNode().put("key", "value");
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                "target-123",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                "NullPointerException",
                "payload text",
                payloadJson
        );
        ProductServiceCommunicationHistory savedHistory = buildHistory(
                1L,
                ProductServiceCommunicationTargetType.INVENTORY,
                "target-123",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                "NullPointerException",
                "payload text",
                payloadJson,
                LocalDateTime.of(2026, 2, 2, 12, 0, 0)
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        ProductServiceCommunicationHistory result = recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.INVENTORY);
        assertThat(captured.getTargetId()).isEqualTo("target-123");
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("NullPointerException");
        assertThat(captured.getPayload()).isEqualTo("payload text");
        assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(result).isSameAs(savedHistory);
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 선택 필드가 null이면 null로 저장된다")
    void record_withRequiredFieldsOnly_savesWithNullOptionalFields() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE,
                null,
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.COMMUNITY,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE);
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.COMMUNITY);
        assertThat(captured.getTargetId()).isNull();
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 REQUEST 통신 타입이 정확히 매핑된다")
    void record_requestCommunicationType_mapsCorrectly() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                null,
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 RESPONSE 통신 타입이 정확히 매핑된다")
    void record_responseCommunicationType_mapsCorrectly() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                null,
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.COMMERCE,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getCommunicationType()).isEqualTo(ProductServiceCommunicationType.RESPONSE);
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 각 발신자 타입이 정확히 매핑된다")
    void record_eachSenderType_mapsCorrectly() {
        for (ProductServiceCommunicationSenderType senderType : ProductServiceCommunicationSenderType.values()) {
            // given
            ProductServiceCommunicationHistoryCommand command = buildCommand(
                    ProductServiceCommunicationTargetType.INVENTORY,
                    null,
                    ProductServiceCommunicationType.REQUEST,
                    senderType,
                    null,
                    null,
                    null
            );
            when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                    .thenReturn(buildMinimalHistory());

            // when
            recordService.record(command);

            // then
            ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                    ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
            verify(saveServiceCommunicationHistoryPort, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getSender()).isEqualTo(senderType);
            clearInvocations(saveServiceCommunicationHistoryPort);
        }
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 각 대상 타입이 정확히 매핑된다")
    void record_eachTargetType_mapsCorrectly() {
        for (ProductServiceCommunicationTargetType targetType : ProductServiceCommunicationTargetType.values()) {
            // given
            ProductServiceCommunicationHistoryCommand command = buildCommand(
                    targetType,
                    null,
                    ProductServiceCommunicationType.REQUEST,
                    ProductServiceCommunicationSenderType.PRODUCT,
                    null,
                    null,
                    null
            );
            when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                    .thenReturn(buildMinimalHistory());

            // when
            recordService.record(command);

            // then
            ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                    ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
            verify(saveServiceCommunicationHistoryPort, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getTargetType()).isEqualTo(targetType);
            clearInvocations(saveServiceCommunicationHistoryPort);
        }
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 payload만 있고 payloadJson이 null이면 정상 저장된다")
    void record_withPayloadOnly_savesCorrectly() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.PRODUCT_IMAGE,
                null,
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.FILE,
                null,
                "text payload only",
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayload()).isEqualTo("text payload only");
        assertThat(captured.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 payloadJson만 있고 payload가 null이면 정상 저장된다")
    void record_withPayloadJsonOnly_savesCorrectly() {
        // given
        JsonNode json = objectMapper.createObjectNode().put("status", 500);
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                null,
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.FULFILLMENT,
                null,
                null,
                json
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isEqualTo(json);
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 targetId가 존재하면 정확히 매핑된다")
    void record_withTargetId_mapsTargetIdCorrectly() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.FULFILLMENT_GOODS_ELEMENT,
                "element-456",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getTargetId()).isEqualTo("element-456");
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 exception이 존재하면 정확히 매핑된다")
    void record_withException_mapsExceptionCorrectly() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.FULFILLMENT_AUTH,
                null,
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                "HttpClientErrorException",
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getException()).isEqualTo("HttpClientErrorException");
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 SavePort가 정확히 한 번 호출된다")
    void record_callsSavePortExactlyOnce() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                null,
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        verify(saveServiceCommunicationHistoryPort, times(1)).save(any(ProductServiceCommunicationHistory.class));
        verifyNoMoreInteractions(saveServiceCommunicationHistoryPort);
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 SavePort의 반환값이 그대로 반환된다")
    void record_returnsExactResultFromPort() {
        // given
        JsonNode payloadJson = objectMapper.createObjectNode().put("method", "GET");
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                "inv-789",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                "GET /api/inventory",
                payloadJson
        );
        ProductServiceCommunicationHistory expectedResult = buildHistory(
                100L,
                ProductServiceCommunicationTargetType.INVENTORY,
                "inv-789",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                "GET /api/inventory",
                payloadJson,
                LocalDateTime.of(2026, 2, 3, 15, 30, 0)
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(expectedResult);

        // when
        ProductServiceCommunicationHistory result = recordService.record(command);

        // then
        assertThat(result).isSameAs(expectedResult);
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 15, 30, 0));
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 저장 전 도메인 객체에는 id와 createdAt이 null이다")
    void record_historyBeforeSave_hasNoIdAndCreatedAt() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                null,
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("상품 서비스 통신 기록 저장 시 복합 JSON payload가 정확히 매핑된다")
    void record_withComplexJsonPayload_mapsCorrectly() {
        // given
        ObjectNode body = objectMapper.createObjectNode();
        body.put("productId", 100);
        body.put("quantity", 5);
        ObjectNode complexJson = objectMapper.createObjectNode();
        complexJson.put("method", "POST");
        complexJson.put("url", "http://api.example.com/inventory");
        complexJson.put("attempt", 3);
        complexJson.set("body", body);

        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                "100",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                "{\"method\":\"POST\",\"url\":\"http://api.example.com/inventory\"}",
                complexJson
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayloadJson()).isEqualTo(complexJson);
        assertThat(captured.getPayloadJson().get("method").asText()).isEqualTo("POST");
        assertThat(captured.getPayloadJson().get("attempt").asInt()).isEqualTo(3);
        assertThat(captured.getPayloadJson().get("body").get("productId").asInt()).isEqualTo(100);
        assertThat(captured.getPayloadJson().get("body").get("quantity").asInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("풀필먼트 인증 실패 응답 기록이 정확히 저장된다")
    void record_fulfillmentAuthFailureResponse_savesCorrectly() {
        // given
        ObjectNode errorPayload = objectMapper.createObjectNode();
        errorPayload.put("error", "Unauthorized");
        errorPayload.put("message", "Invalid token");
        errorPayload.put("attempt", 1);

        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.FULFILLMENT_AUTH,
                null,
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.FULFILLMENT,
                "HttpClientErrorException",
                "401 Unauthorized",
                errorPayload
        );
        ProductServiceCommunicationHistory savedHistory = buildHistory(
                50L,
                ProductServiceCommunicationTargetType.FULFILLMENT_AUTH,
                null,
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.FULFILLMENT,
                "HttpClientErrorException",
                "401 Unauthorized",
                errorPayload,
                LocalDateTime.of(2026, 2, 2, 10, 0, 0)
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        ProductServiceCommunicationHistory result = recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.FULFILLMENT_AUTH);
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.FULFILLMENT);
        assertThat(captured.getException()).isEqualTo("HttpClientErrorException");
        assertThat(captured.getPayload()).isEqualTo("401 Unauthorized");
        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 2, 10, 0, 0));
    }

    @Test
    @DisplayName("커뮤니티 서비스 리뷰 집계 요청 기록이 정확히 저장된다")
    void record_communityReviewAggregateRequest_savesCorrectly() {
        // given
        ObjectNode requestPayload = objectMapper.createObjectNode();
        requestPayload.put("method", "GET");
        requestPayload.put("url", "http://community/api/reviews/aggregate");
        requestPayload.put("attempt", 2);

        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE,
                "product-100",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                "GET /api/reviews/aggregate?productId=100",
                requestPayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE);
        assertThat(captured.getTargetId()).isEqualTo("product-100");
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isNull();
    }

    @Test
    @DisplayName("커머스 서비스 재고 조회 실패 기록이 정확히 저장된다")
    void record_commerceInventoryFailure_savesCorrectly() {
        // given
        ObjectNode errorPayload = objectMapper.createObjectNode();
        errorPayload.put("error", "RestClientException");
        errorPayload.put("message", "Connection refused");
        errorPayload.put("attempt", 3);

        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.INVENTORY,
                "product-200",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                "RestClientException",
                "POST /api/inventory/stocks",
                errorPayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.INVENTORY);
        assertThat(captured.getTargetId()).isEqualTo("product-200");
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("RestClientException");
        assertThat(captured.getPayload()).isEqualTo("POST /api/inventory/stocks");
    }

    @Test
    @DisplayName("파일 서비스 상품 이미지 조회 실패 기록이 정확히 저장된다")
    void record_fileServiceImageFailure_savesCorrectly() {
        // given
        ObjectNode responsePayload = objectMapper.createObjectNode();
        responsePayload.put("status", 500);
        responsePayload.put("message", "Internal Server Error");

        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.PRODUCT_IMAGE,
                "image-300",
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.FILE,
                "HttpServerErrorException",
                "500 Internal Server Error",
                responsePayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.PRODUCT_IMAGE);
        assertThat(captured.getTargetId()).isEqualTo("image-300");
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.FILE);
        assertThat(captured.getException()).isEqualTo("HttpServerErrorException");
    }

    @Test
    @DisplayName("풀필먼트 상품 등록 요청 기록이 정확히 저장된다")
    void record_fulfillmentGoodsRegistrationRequest_savesCorrectly() {
        // given
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("goodsName", "테스트 상품");
        requestBody.put("goodsCode", "GOODS-001");
        ObjectNode requestPayload = objectMapper.createObjectNode();
        requestPayload.put("method", "POST");
        requestPayload.put("url", "http://fulfillment/api/goods");
        requestPayload.put("attempt", 1);
        requestPayload.set("body", requestBody);

        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                "goods-500",
                ProductServiceCommunicationType.REQUEST,
                ProductServiceCommunicationSenderType.PRODUCT,
                null,
                "POST /api/goods",
                requestPayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.FULFILLMENT_GOODS);
        assertThat(captured.getTargetId()).isEqualTo("goods-500");
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayloadJson().get("body").get("goodsName").asText()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("풀필먼트 모음상품 조회 실패 기록이 정확히 저장된다")
    void record_fulfillmentGoodsElementFailure_savesCorrectly() {
        // given
        ProductServiceCommunicationHistoryCommand command = buildCommand(
                ProductServiceCommunicationTargetType.FULFILLMENT_GOODS_ELEMENT,
                "element-600",
                ProductServiceCommunicationType.RESPONSE,
                ProductServiceCommunicationSenderType.FULFILLMENT,
                "ResourceAccessException",
                "I/O error on GET request",
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(ProductServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<ProductServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(ProductServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        ProductServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.FULFILLMENT_GOODS_ELEMENT);
        assertThat(captured.getTargetId()).isEqualTo("element-600");
        assertThat(captured.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(ProductServiceCommunicationSenderType.FULFILLMENT);
        assertThat(captured.getException()).isEqualTo("ResourceAccessException");
        assertThat(captured.getPayload()).isEqualTo("I/O error on GET request");
        assertThat(captured.getPayloadJson()).isNull();
    }

    // --- Helper Methods ---

    private ProductServiceCommunicationHistoryCommand buildCommand(
            ProductServiceCommunicationTargetType targetType,
            String targetId,
            ProductServiceCommunicationType communicationType,
            ProductServiceCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson
    ) {
        return ProductServiceCommunicationHistoryCommand.builder()
                .targetType(targetType)
                .targetId(targetId)
                .communicationType(communicationType)
                .sender(sender)
                .exception(exception)
                .payload(payload)
                .payloadJson(payloadJson)
                .build();
    }

    private ProductServiceCommunicationHistory buildHistory(
            Long id,
            ProductServiceCommunicationTargetType targetType,
            String targetId,
            ProductServiceCommunicationType communicationType,
            ProductServiceCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson,
            LocalDateTime createdAt
    ) {
        return ProductServiceCommunicationHistory.from(
                ProductServiceCommunicationHistorySnapshotState.builder()
                        .id(id)
                        .targetType(targetType)
                        .targetId(targetId)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(payloadJson)
                        .createdAt(createdAt)
                        .build()
        );
    }

    private ProductServiceCommunicationHistory buildMinimalHistory() {
        return ProductServiceCommunicationHistory.from(
                ProductServiceCommunicationHistorySnapshotState.builder()
                        .id(1L)
                        .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                        .communicationType(ProductServiceCommunicationType.REQUEST)
                        .sender(ProductServiceCommunicationSenderType.PRODUCT)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}
