package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPort;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortResult;
import com.personal.marketnote.commerce.utility.VendorCommunicationRecorder;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class KcpQuickPaymentAdapter implements RegisterQuickPaymentTransactionPort {
    private static final CommerceVendorName VENDOR_NAME = CommerceVendorName.NHN_KCP;
    private static final CommerceVendorCommunicationTargetType TARGET_TYPE =
            CommerceVendorCommunicationTargetType.QUICK_PAYMENT_TRADE_REGISTER;
    private static final String GOOD_MNY = "0";
    private static final String PAY_METHOD = "AUTH";
    private static final String GOOD_NAME = "빠른결제 카드 등록";

    private final KcpProperties kcpProperties;
    private final KcpApiClient kcpApiClient;
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

        recordRequest(command.transactionId(), request);

        try {
            KcpTradeRegisterResponse response = kcpApiClient.registerTrade(request);
            recordResponse(command.transactionId(), response);

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
            recordError(command.transactionId(), e);
            throw e;
        }
    }

    private void recordRequest(String targetId, Object request) {
        JsonNode payloadJson = kcpApiClient.toJsonNode(request);
        vendorCommunicationRecorder.record(
                TARGET_TYPE,
                CommerceVendorCommunicationType.REQUEST,
                CommerceVendorCommunicationSenderType.SERVER,
                targetId,
                VENDOR_NAME,
                payloadJson.toString(),
                payloadJson
        );
    }

    private void recordResponse(String targetId, Object response) {
        JsonNode payloadJson = kcpApiClient.toJsonNode(response);
        vendorCommunicationRecorder.record(
                TARGET_TYPE,
                CommerceVendorCommunicationType.RESPONSE,
                CommerceVendorCommunicationSenderType.VENDOR,
                targetId,
                VENDOR_NAME,
                payloadJson.toString(),
                payloadJson
        );
    }

    private void recordError(String targetId, Exception e) {
        JsonNode errorJson = kcpApiClient.toJsonNode(
                Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage())
        );
        vendorCommunicationRecorder.record(
                TARGET_TYPE,
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
