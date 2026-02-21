package com.personal.marketnote.commerce.service.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.commerce.domain.servicecommunication.*;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.out.servicecommunication.SaveCommerceServiceCommunicationHistoryPort;
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
class RecordCommerceServiceCommunicationHistoryUseCaseTest {

    @Mock
    private SaveCommerceServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @InjectMocks
    private RecordCommerceServiceCommunicationHistoryService recordService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 모든 필드가 정확히 매핑되어 저장된다")
    void record_withAllFields_savesWithAllFieldsMapped() {
        // given
        JsonNode payloadJson = objectMapper.createObjectNode().put("key", "value");
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "target-123",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                "NullPointerException",
                "payload text",
                payloadJson
        );
        CommerceServiceCommunicationHistory savedHistory = buildHistory(
                1L,
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "target-123",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                "NullPointerException",
                "payload text",
                payloadJson,
                LocalDateTime.of(2026, 2, 3, 12, 0, 0)
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        CommerceServiceCommunicationHistory result = recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(captured.getTargetId()).isEqualTo("target-123");
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.COMMERCE);
        assertThat(captured.getException()).isEqualTo("NullPointerException");
        assertThat(captured.getPayload()).isEqualTo("payload text");
        assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(result).isSameAs(savedHistory);
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 선택 필드가 null이면 null로 저장된다")
    void record_withRequiredFieldsOnly_savesWithNullOptionalFields() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.USER_POINT,
                null,
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.USER,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.USER_POINT);
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.USER);
        assertThat(captured.getTargetId()).isNull();
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 REQUEST 통신 타입이 정확히 매핑된다")
    void record_requestCommunicationType_mapsCorrectly() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                null,
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.REQUEST);
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 RESPONSE 통신 타입이 정확히 매핑된다")
    void record_responseCommunicationType_mapsCorrectly() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                null,
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.PRODUCT,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.RESPONSE);
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 각 발신자 타입이 정확히 매핑된다")
    void record_eachSenderType_mapsCorrectly() {
        for (CommerceServiceCommunicationSenderType senderType : CommerceServiceCommunicationSenderType.values()) {
            // given
            CommerceServiceCommunicationHistoryCommand command = buildCommand(
                    CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                    null,
                    CommerceServiceCommunicationType.REQUEST,
                    senderType,
                    null,
                    null,
                    null
            );
            when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                    .thenReturn(buildMinimalHistory());

            // when
            recordService.record(command);

            // then
            ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                    ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
            verify(saveServiceCommunicationHistoryPort, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getSender()).isEqualTo(senderType);
            clearInvocations(saveServiceCommunicationHistoryPort);
        }
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 각 대상 타입이 정확히 매핑된다")
    void record_eachTargetType_mapsCorrectly() {
        for (CommerceServiceCommunicationTargetType targetType : CommerceServiceCommunicationTargetType.values()) {
            // given
            CommerceServiceCommunicationHistoryCommand command = buildCommand(
                    targetType,
                    null,
                    CommerceServiceCommunicationType.REQUEST,
                    CommerceServiceCommunicationSenderType.COMMERCE,
                    null,
                    null,
                    null
            );
            when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                    .thenReturn(buildMinimalHistory());

            // when
            recordService.record(command);

            // then
            ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                    ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
            verify(saveServiceCommunicationHistoryPort, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getTargetType()).isEqualTo(targetType);
            clearInvocations(saveServiceCommunicationHistoryPort);
        }
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 payload만 있고 payloadJson이 null이면 정상 저장된다")
    void record_withPayloadOnly_savesCorrectly() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.CART_PRODUCT,
                null,
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.PRODUCT,
                null,
                "text payload only",
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayload()).isEqualTo("text payload only");
        assertThat(captured.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 payloadJson만 있고 payload가 null이면 정상 저장된다")
    void record_withPayloadJsonOnly_savesCorrectly() {
        // given
        JsonNode json = objectMapper.createObjectNode().put("status", 500);
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.USER_POINT,
                null,
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.USER,
                null,
                null,
                json
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isEqualTo(json);
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 targetId가 존재하면 정확히 매핑된다")
    void record_withTargetId_mapsTargetIdCorrectly() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.CART_PRODUCT,
                "cart-456",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getTargetId()).isEqualTo("cart-456");
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 exception이 존재하면 정확히 매핑된다")
    void record_withException_mapsExceptionCorrectly() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                null,
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                "HttpClientErrorException",
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getException()).isEqualTo("HttpClientErrorException");
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 SavePort가 정확히 한 번 호출된다")
    void record_callsSavePortExactlyOnce() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                null,
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        verify(saveServiceCommunicationHistoryPort, times(1)).save(any(CommerceServiceCommunicationHistory.class));
        verifyNoMoreInteractions(saveServiceCommunicationHistoryPort);
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 SavePort의 반환값이 그대로 반환된다")
    void record_returnsExactResultFromPort() {
        // given
        JsonNode payloadJson = objectMapper.createObjectNode().put("method", "GET");
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "product-789",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                "GET /api/products/info",
                payloadJson
        );
        CommerceServiceCommunicationHistory expectedResult = buildHistory(
                100L,
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "product-789",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                "GET /api/products/info",
                payloadJson,
                LocalDateTime.of(2026, 2, 3, 15, 30, 0)
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(expectedResult);

        // when
        CommerceServiceCommunicationHistory result = recordService.record(command);

        // then
        assertThat(result).isSameAs(expectedResult);
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 15, 30, 0));
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 저장 전 도메인 객체에는 id와 createdAt이 null이다")
    void record_historyBeforeSave_hasNoIdAndCreatedAt() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                null,
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                null,
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("커머스 서비스 통신 기록 저장 시 복합 JSON payload가 정확히 매핑된다")
    void record_withComplexJsonPayload_mapsCorrectly() {
        // given
        ObjectNode body = objectMapper.createObjectNode();
        body.put("productId", 100);
        body.put("quantity", 5);
        ObjectNode complexJson = objectMapper.createObjectNode();
        complexJson.put("method", "POST");
        complexJson.put("url", "http://api.example.com/products/info");
        complexJson.put("attempt", 3);
        complexJson.set("body", body);

        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "100",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                "{\"method\":\"POST\",\"url\":\"http://api.example.com/products/info\"}",
                complexJson
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayloadJson()).isEqualTo(complexJson);
        assertThat(captured.getPayloadJson().get("method").asText()).isEqualTo("POST");
        assertThat(captured.getPayloadJson().get("attempt").asInt()).isEqualTo(3);
        assertThat(captured.getPayloadJson().get("body").get("productId").asInt()).isEqualTo(100);
        assertThat(captured.getPayloadJson().get("body").get("quantity").asInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 서비스 상품 정보 조회 실패 응답 기록이 정확히 저장된다")
    void record_productInfoFailureResponse_savesCorrectly() {
        // given
        ObjectNode errorPayload = objectMapper.createObjectNode();
        errorPayload.put("error", "Not Found");
        errorPayload.put("message", "상품을 찾을 수 없습니다");
        errorPayload.put("attempt", 1);

        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "product-100",
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.PRODUCT,
                "HttpClientErrorException",
                "404 Not Found",
                errorPayload
        );
        CommerceServiceCommunicationHistory savedHistory = buildHistory(
                50L,
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "product-100",
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.PRODUCT,
                "HttpClientErrorException",
                "404 Not Found",
                errorPayload,
                LocalDateTime.of(2026, 2, 3, 10, 0, 0)
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        CommerceServiceCommunicationHistory result = recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(captured.getTargetId()).isEqualTo("product-100");
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("HttpClientErrorException");
        assertThat(captured.getPayload()).isEqualTo("404 Not Found");
        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 10, 0, 0));
    }

    @Test
    @DisplayName("유저 서비스 포인트 차감 요청 기록이 정확히 저장된다")
    void record_userPointDeductionRequest_savesCorrectly() {
        // given
        ObjectNode requestPayload = objectMapper.createObjectNode();
        requestPayload.put("method", "POST");
        requestPayload.put("url", "http://user/api/points/deduct");
        requestPayload.put("attempt", 1);

        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.USER_POINT,
                "user-200",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                "POST /api/points/deduct",
                requestPayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.USER_POINT);
        assertThat(captured.getTargetId()).isEqualTo("user-200");
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.COMMERCE);
        assertThat(captured.getException()).isNull();
    }

    @Test
    @DisplayName("유저 서비스 포인트 조회 실패 응답 기록이 정확히 저장된다")
    void record_userPointQueryFailureResponse_savesCorrectly() {
        // given
        ObjectNode errorPayload = objectMapper.createObjectNode();
        errorPayload.put("error", "RestClientException");
        errorPayload.put("message", "Connection refused");
        errorPayload.put("attempt", 3);

        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.USER_POINT,
                "user-300",
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.USER,
                "RestClientException",
                "GET /api/points",
                errorPayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.USER_POINT);
        assertThat(captured.getTargetId()).isEqualTo("user-300");
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.USER);
        assertThat(captured.getException()).isEqualTo("RestClientException");
        assertThat(captured.getPayload()).isEqualTo("GET /api/points");
    }

    @Test
    @DisplayName("장바구니 상품 조회 실패 응답 기록이 정확히 저장된다")
    void record_cartProductQueryFailureResponse_savesCorrectly() {
        // given
        ObjectNode responsePayload = objectMapper.createObjectNode();
        responsePayload.put("status", 500);
        responsePayload.put("message", "Internal Server Error");

        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.CART_PRODUCT,
                "cart-400",
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.PRODUCT,
                "HttpServerErrorException",
                "500 Internal Server Error",
                responsePayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.CART_PRODUCT);
        assertThat(captured.getTargetId()).isEqualTo("cart-400");
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("HttpServerErrorException");
    }

    @Test
    @DisplayName("상품 정보 동기화 요청 기록이 정확히 저장된다")
    void record_productInfoSyncRequest_savesCorrectly() {
        // given
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("productName", "테스트 상품");
        requestBody.put("price", 15000);
        ObjectNode requestPayload = objectMapper.createObjectNode();
        requestPayload.put("method", "POST");
        requestPayload.put("url", "http://product/api/products/info");
        requestPayload.put("attempt", 1);
        requestPayload.set("body", requestBody);

        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.PRODUCT_INFO,
                "product-500",
                CommerceServiceCommunicationType.REQUEST,
                CommerceServiceCommunicationSenderType.COMMERCE,
                null,
                "POST /api/products/info",
                requestPayload
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(captured.getTargetId()).isEqualTo("product-500");
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.COMMERCE);
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayloadJson().get("body").get("productName").asText()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("장바구니 상품 가격 업데이트 실패 기록이 정확히 저장된다")
    void record_cartProductPriceUpdateFailure_savesCorrectly() {
        // given
        CommerceServiceCommunicationHistoryCommand command = buildCommand(
                CommerceServiceCommunicationTargetType.CART_PRODUCT,
                "cart-600",
                CommerceServiceCommunicationType.RESPONSE,
                CommerceServiceCommunicationSenderType.PRODUCT,
                "ResourceAccessException",
                "I/O error on PATCH request",
                null
        );
        when(saveServiceCommunicationHistoryPort.save(any(CommerceServiceCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceServiceCommunicationHistory.class);
        verify(saveServiceCommunicationHistoryPort).save(captor.capture());

        CommerceServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.CART_PRODUCT);
        assertThat(captured.getTargetId()).isEqualTo("cart-600");
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("ResourceAccessException");
        assertThat(captured.getPayload()).isEqualTo("I/O error on PATCH request");
        assertThat(captured.getPayloadJson()).isNull();
    }

    // --- Helper Methods ---

    private CommerceServiceCommunicationHistoryCommand buildCommand(
            CommerceServiceCommunicationTargetType targetType,
            String targetId,
            CommerceServiceCommunicationType communicationType,
            CommerceServiceCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson
    ) {
        return CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(targetType)
                .targetId(targetId)
                .communicationType(communicationType)
                .sender(sender)
                .exception(exception)
                .payload(payload)
                .payloadJson(payloadJson)
                .build();
    }

    private CommerceServiceCommunicationHistory buildHistory(
            Long id,
            CommerceServiceCommunicationTargetType targetType,
            String targetId,
            CommerceServiceCommunicationType communicationType,
            CommerceServiceCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson,
            LocalDateTime createdAt
    ) {
        return CommerceServiceCommunicationHistory.from(
                CommerceServiceCommunicationHistorySnapshotState.builder()
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

    private CommerceServiceCommunicationHistory buildMinimalHistory() {
        return CommerceServiceCommunicationHistory.from(
                CommerceServiceCommunicationHistorySnapshotState.builder()
                        .id(1L)
                        .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                        .communicationType(CommerceServiceCommunicationType.REQUEST)
                        .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }
}
