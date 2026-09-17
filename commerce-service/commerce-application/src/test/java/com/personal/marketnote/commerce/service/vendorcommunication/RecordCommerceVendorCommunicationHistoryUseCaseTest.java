package com.personal.marketnote.commerce.service.vendorcommunication;

import com.personal.marketnote.commerce.domain.vendorcommunication.*;
import com.personal.marketnote.commerce.port.in.command.vendorcommunication.CommerceVendorCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.out.vendorcommunication.SaveCommerceVendorCommunicationHistoryPort;
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
class RecordCommerceVendorCommunicationHistoryUseCaseTest {

    @Mock
    private SaveCommerceVendorCommunicationHistoryPort saveVendorCommunicationHistoryPort;

    @InjectMocks
    private RecordCommerceVendorCommunicationHistoryService recordService;

    @Test
    @DisplayName("벤더 통신 기록 저장 시 모든 필드가 정확히 매핑되어 저장된다")
    void record_withAllFields_savesWithAllFieldsMapped() {
        // given
        String payloadJson = "{\"key\":\"value\"}";
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                "trade-123",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                "NullPointerException",
                "payload text",
                payloadJson
        );
        CommerceVendorCommunicationHistory savedHistory = buildHistory(
                1L,
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                "trade-123",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                "NullPointerException",
                "payload text",
                payloadJson,
                LocalDateTime.of(2026, 2, 24, 12, 0, 0)
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        CommerceVendorCommunicationHistory result = recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL);
        assertThat(captured.getTargetId()).isEqualTo("trade-123");
        assertThat(captured.getVendorName()).isEqualTo(CommerceVendorName.NHN_KCP);
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceVendorCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(CommerceVendorCommunicationSenderType.SERVER);
        assertThat(captured.getException()).isEqualTo("NullPointerException");
        assertThat(captured.getPayload()).isEqualTo("payload text");
        assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);
        assertThat(result).isSameAs(savedHistory);
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 선택 필드가 null이면 null로 저장된다")
    void record_withRequiredFieldsOnly_savesWithNullOptionalFields() {
        // given
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.TRADE_REGISTER,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                null,
                null,
                null
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceVendorCommunicationTargetType.TRADE_REGISTER);
        assertThat(captured.getVendorName()).isEqualTo(CommerceVendorName.NHN_KCP);
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceVendorCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(CommerceVendorCommunicationSenderType.VENDOR);
        assertThat(captured.getTargetId()).isNull();
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 각 대상 타입이 정확히 매핑된다")
    void record_eachTargetType_mapsCorrectly() {
        for (CommerceVendorCommunicationTargetType targetType : CommerceVendorCommunicationTargetType.values()) {
            // given
            CommerceVendorCommunicationHistoryCommand command = buildCommand(
                    targetType,
                    null,
                    CommerceVendorName.NHN_KCP,
                    CommerceVendorCommunicationType.REQUEST,
                    CommerceVendorCommunicationSenderType.SERVER,
                    null,
                    null,
                    null
            );
            when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                    .thenReturn(buildMinimalHistory());

            // when
            recordService.record(command);

            // then
            ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                    ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
            verify(saveVendorCommunicationHistoryPort, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getTargetType()).isEqualTo(targetType);
            clearInvocations(saveVendorCommunicationHistoryPort);
        }
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 REQUEST 통신 타입이 정확히 매핑된다")
    void record_requestCommunicationType_mapsCorrectly() {
        // given
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                null,
                null
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getCommunicationType()).isEqualTo(CommerceVendorCommunicationType.REQUEST);
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 RESPONSE 통신 타입이 정확히 매핑된다")
    void record_responseCommunicationType_mapsCorrectly() {
        // given
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_CANCEL,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                null,
                null,
                null
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getCommunicationType()).isEqualTo(CommerceVendorCommunicationType.RESPONSE);
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 각 발신자 타입이 정확히 매핑된다")
    void record_eachSenderType_mapsCorrectly() {
        for (CommerceVendorCommunicationSenderType senderType : CommerceVendorCommunicationSenderType.values()) {
            // given
            CommerceVendorCommunicationHistoryCommand command = buildCommand(
                    CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                    null,
                    CommerceVendorName.NHN_KCP,
                    CommerceVendorCommunicationType.REQUEST,
                    senderType,
                    null,
                    null,
                    null
            );
            when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                    .thenReturn(buildMinimalHistory());

            // when
            recordService.record(command);

            // then
            ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                    ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
            verify(saveVendorCommunicationHistoryPort, atLeastOnce()).save(captor.capture());
            assertThat(captor.getValue().getSender()).isEqualTo(senderType);
            clearInvocations(saveVendorCommunicationHistoryPort);
        }
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 payload만 있고 payloadJson이 null이면 정상 저장된다")
    void record_withPayloadOnly_savesCorrectly() {
        // given
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                null,
                "text payload only",
                null
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayload()).isEqualTo("text payload only");
        assertThat(captured.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 payloadJson만 있고 payload가 null이면 정상 저장된다")
    void record_withPayloadJsonOnly_savesCorrectly() {
        // given
        String json = "{\"status\":500}";
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_CANCEL,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                null,
                null,
                json
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isEqualTo(json);
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 저장 전 도메인 객체에는 id와 createdAt이 null이다")
    void record_historyBeforeSave_hasNoIdAndCreatedAt() {
        // given
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.TRADE_REGISTER,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                null,
                null
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 SavePort가 정확히 한 번 호출된다")
    void record_callsSavePortExactlyOnce() {
        // given
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                null,
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                null,
                null
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        verify(saveVendorCommunicationHistoryPort, times(1)).save(any(CommerceVendorCommunicationHistory.class));
        verifyNoMoreInteractions(saveVendorCommunicationHistoryPort);
    }

    @Test
    @DisplayName("벤더 통신 기록 저장 시 SavePort의 반환값이 그대로 반환된다")
    void record_returnsExactResultFromPort() {
        // given
        String payloadJson = "{\"method\":\"POST\"}";
        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                "approval-789",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                "POST /api/payment/approval",
                payloadJson
        );
        CommerceVendorCommunicationHistory expectedResult = buildHistory(
                100L,
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                "approval-789",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                "POST /api/payment/approval",
                payloadJson,
                LocalDateTime.of(2026, 2, 24, 15, 30, 0)
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(expectedResult);

        // when
        CommerceVendorCommunicationHistory result = recordService.record(command);

        // then
        assertThat(result).isSameAs(expectedResult);
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 24, 15, 30, 0));
    }

    @Test
    @DisplayName("KCP 결제 승인 요청 기록이 정확히 저장된다")
    void record_kcpPaymentApprovalRequest_savesCorrectly() {
        // given
        String requestPayload = "{\"method\":\"POST\",\"url\":\"https://kcp.co.kr/api/payment\",\"attempt\":1,\"body\":{\"amount\":15000,\"ordNo\":\"ORD-001\"}}";

        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL,
                "ORD-001",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                "POST /api/payment",
                requestPayload
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL);
        assertThat(captured.getTargetId()).isEqualTo("ORD-001");
        assertThat(captured.getVendorName()).isEqualTo(CommerceVendorName.NHN_KCP);
        assertThat(captured.getCommunicationType()).isEqualTo(CommerceVendorCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(CommerceVendorCommunicationSenderType.SERVER);
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayloadJson()).contains("\"amount\":15000");
    }

    @Test
    @DisplayName("KCP 결제 취소 실패 응답 기록이 정확히 저장된다")
    void record_kcpPaymentCancelFailureResponse_savesCorrectly() {
        // given
        String errorPayload = "{\"error\":\"CANCEL_FAILED\",\"message\":\"이미 취소된 결제입니다\"}";

        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.PAYMENT_CANCEL,
                "ORD-002",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                "PaymentCancelException",
                "CANCEL_FAILED",
                errorPayload
        );
        CommerceVendorCommunicationHistory savedHistory = buildHistory(
                50L,
                CommerceVendorCommunicationTargetType.PAYMENT_CANCEL,
                "ORD-002",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                "PaymentCancelException",
                "CANCEL_FAILED",
                errorPayload,
                LocalDateTime.of(2026, 2, 24, 10, 0, 0)
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        CommerceVendorCommunicationHistory result = recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL);
        assertThat(captured.getTargetId()).isEqualTo("ORD-002");
        assertThat(captured.getSender()).isEqualTo(CommerceVendorCommunicationSenderType.VENDOR);
        assertThat(captured.getException()).isEqualTo("PaymentCancelException");
        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 24, 10, 0, 0));
    }

    @Test
    @DisplayName("KCP 거래 등록 요청 기록이 정확히 저장된다")
    void record_kcpTradeRegisterRequest_savesCorrectly() {
        // given
        String requestPayload = "{\"method\":\"POST\",\"url\":\"https://kcp.co.kr/api/trade/register\",\"attempt\":1}";

        CommerceVendorCommunicationHistoryCommand command = buildCommand(
                CommerceVendorCommunicationTargetType.TRADE_REGISTER,
                "TRADE-001",
                CommerceVendorName.NHN_KCP,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                null,
                "POST /api/trade/register",
                requestPayload
        );
        when(saveVendorCommunicationHistoryPort.save(any(CommerceVendorCommunicationHistory.class)))
                .thenReturn(buildMinimalHistory());

        // when
        recordService.record(command);

        // then
        ArgumentCaptor<CommerceVendorCommunicationHistory> captor =
                ArgumentCaptor.forClass(CommerceVendorCommunicationHistory.class);
        verify(saveVendorCommunicationHistoryPort).save(captor.capture());

        CommerceVendorCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommerceVendorCommunicationTargetType.TRADE_REGISTER);
        assertThat(captured.getTargetId()).isEqualTo("TRADE-001");
        assertThat(captured.getVendorName()).isEqualTo(CommerceVendorName.NHN_KCP);
        assertThat(captured.getSender()).isEqualTo(CommerceVendorCommunicationSenderType.SERVER);
        assertThat(captured.getException()).isNull();
    }

    // --- Helper Methods ---

    private CommerceVendorCommunicationHistoryCommand buildCommand(
            CommerceVendorCommunicationTargetType targetType,
            String targetId,
            CommerceVendorName vendorName,
            CommerceVendorCommunicationType communicationType,
            CommerceVendorCommunicationSenderType sender,
            String exception,
            String payload,
            String payloadJson
    ) {
        return CommerceVendorCommunicationHistoryCommand.builder()
                .targetType(targetType)
                .targetId(targetId)
                .vendorName(vendorName)
                .communicationType(communicationType)
                .sender(sender)
                .exception(exception)
                .payload(payload)
                .payloadJson(payloadJson)
                .build();
    }

    private CommerceVendorCommunicationHistory buildHistory(
            Long id,
            CommerceVendorCommunicationTargetType targetType,
            String targetId,
            CommerceVendorName vendorName,
            CommerceVendorCommunicationType communicationType,
            CommerceVendorCommunicationSenderType sender,
            String exception,
            String payload,
            String payloadJson,
            LocalDateTime createdAt
    ) {
        return CommerceVendorCommunicationHistory.from(
                CommerceVendorCommunicationHistorySnapshotState.builder()
                        .id(id)
                        .targetType(targetType)
                        .targetId(targetId)
                        .vendorName(vendorName)
                        .communicationType(communicationType)
                        .sender(sender)
                        .exception(exception)
                        .payload(payload)
                        .payloadJson(payloadJson)
                        .createdAt(createdAt)
                        .build()
        );
    }

    private CommerceVendorCommunicationHistory buildMinimalHistory() {
        return CommerceVendorCommunicationHistory.from(
                CommerceVendorCommunicationHistorySnapshotState.builder()
                        .id(1L)
                        .targetType(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL)
                        .vendorName(CommerceVendorName.NHN_KCP)
                        .communicationType(CommerceVendorCommunicationType.REQUEST)
                        .sender(CommerceVendorCommunicationSenderType.SERVER)
                        .createdAt(LocalDateTime.of(2026, 2, 24, 12, 0, 0))
                        .build()
        );
    }
}
