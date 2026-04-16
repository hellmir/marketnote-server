package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.out.quickpayment.*;
import com.personal.marketnote.commerce.utility.VendorCommunicationRecorder;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class KcpQuickPaymentAdapter implements RegisterQuickPaymentTransactionPort, IssueBatchKeyPort {
    private static final CommerceVendorName VENDOR_NAME = CommerceVendorName.NHN_KCP;
    private static final String GOOD_MNY = "0";
    private static final String PAY_METHOD = "AUTH";
    private static final String GOOD_NAME = "빠른결제 카드 등록";
    private static final String BATCH_KEY_TRAN_CD = "00300001";
    private static final String MASKED = "***MASKED***";
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "kcp_cert_info", "enc_data", "enc_info", "batch_key"
    );

    private final KcpProperties kcpProperties;
    private final KcpApiClient kcpApiClient;
    private final KcpCertificateLoader kcpCertificateLoader;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;

    @Override
    public RegisterQuickPaymentTransactionPortResult registerTransaction(
            RegisterQuickPaymentTransactionPortCommand command
    ) {
        KcpTradeRegisterRequest request = KcpTradeRegisterRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .ordrIdxx(command.transactionId())
                .goodMny(GOOD_MNY)
                .payMethod(PAY_METHOD)
                .goodName(GOOD_NAME)
                .retUrl(kcpProperties.getRetUrl())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_TRADE_REGISTER,
                command.transactionId(), request);

        try {
            KcpTradeRegisterResponse response = kcpApiClient.registerTrade(request);
            recordResponse(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_TRADE_REGISTER,
                    command.transactionId(), response);

            boolean isSuccess = response.isSuccess();
            if (!isSuccess) {
                log.error("빠른결제 거래등록 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return RegisterQuickPaymentTransactionPortResult.builder()
                    .success(isSuccess)
                    .resultCode(response.resCd())
                    .resultMessage(response.resMsg())
                    .approvalKey(response.approvalKey())
                    .payUrl(response.payUrl())
                    .traceNo(response.traceNo())
                    .rawResponse(kcpApiClient.toJsonNode(response).toString())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_TRADE_REGISTER,
                    command.transactionId(), e);
            throw e;
        }
    }

    @Override
    public IssueBatchKeyPortResult issueBatchKey(IssueBatchKeyPortCommand command) {
        String certInfo = kcpCertificateLoader.loadCertInfo();

        KcpBatchKeyIssuanceRequest request = KcpBatchKeyIssuanceRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .kcpCertInfo(certInfo)
                .encData(command.encData())
                .encInfo(command.encInfo())
                .tranCd(BATCH_KEY_TRAN_CD)
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_ISSUANCE,
                null, request);

        try {
            KcpBatchKeyIssuanceResponse response = kcpApiClient.issueBatchKey(request);
            recordResponse(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_ISSUANCE,
                    null, response);

            boolean isSuccess = response.isSuccess();
            if (!isSuccess) {
                log.error("빠른결제 배치키 발급 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return IssueBatchKeyPortResult.builder()
                    .success(isSuccess)
                    .resultCode(response.resCd())
                    .resultMessage(response.resMsg())
                    .batchKey(response.batchKey())
                    .cardCode(response.cardCd())
                    .cardName(response.cardName())
                    .cardBinType01(response.cardBinType01())
                    .cardBinType02(response.cardBinType02())
                    .rawResponse(kcpApiClient.toJsonNode(response).toString())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_ISSUANCE,
                    null, e);
            throw e;
        }
    }

    private void recordRequest(CommerceVendorCommunicationTargetType targetType,
                               String targetId, Object request) {
        JsonNode payloadJson = maskSensitiveFields(kcpApiClient.toJsonNode(request));
        vendorCommunicationRecorder.record(
                targetType,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                targetId,
                VENDOR_NAME,
                payloadJson.toString(),
                payloadJson
        );
    }

    private JsonNode maskSensitiveFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            SENSITIVE_FIELDS.forEach(field -> {
                if (objectNode.has(field)) {
                    objectNode.put(field, MASKED);
                }
            });
        }
        return node;
    }

    private void recordResponse(CommerceVendorCommunicationTargetType targetType,
                                String targetId, Object response) {
        JsonNode payloadJson = maskSensitiveFields(kcpApiClient.toJsonNode(response));
        vendorCommunicationRecorder.record(
                targetType,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                targetId,
                VENDOR_NAME,
                payloadJson.toString(),
                payloadJson
        );
    }

    private void recordError(CommerceVendorCommunicationTargetType targetType,
                             String targetId, Exception e) {
        JsonNode errorJson = kcpApiClient.toJsonNode(
                Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage())
        );
        vendorCommunicationRecorder.record(
                targetType,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                targetId,
                VENDOR_NAME,
                errorJson.toString(),
                errorJson,
                e.getClass().getSimpleName()
        );
    }
}
