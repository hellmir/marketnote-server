package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.domain.payment.PaymentMethod;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
import com.personal.marketnote.commerce.utility.VendorCommunicationRecorder;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class KcpPaymentVendorAdapter implements PaymentVendorPort {
    private static final CommerceVendorName VENDOR_NAME = CommerceVendorName.NHN_KCP;
    private static final String MASKED = "***MASKED***";
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "kcp_cert_info", "kcp_sign_data", "enc_data", "enc_info"
    );

    private final KcpProperties kcpProperties;
    private final KcpApiClient kcpApiClient;
    private final KcpCertificateLoader kcpCertificateLoader;
    private final KcpSignatureGenerator kcpSignatureGenerator;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;

    @Override
    public String getVendorSiteCd() {
        return kcpProperties.getSiteCd();
    }

    @Override
    public TradeRegisterVendorResult registerTrade(TradeRegisterVendorCommand command) {
        String mobilePayMethod = PaymentMethod.valueOf(command.payMethod()).getMobileCode();

        KcpTradeRegisterRequest request = KcpTradeRegisterRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .ordrIdxx(command.orderKey())
                .goodMny(command.goodMny())
                .payMethod(mobilePayMethod)
                .goodName(command.goodName())
                .retUrl(kcpProperties.getRetUrl())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.TRADE_REGISTER, command.orderKey(), request);

        try {
            KcpTradeRegisterResponse response = kcpApiClient.registerTrade(request);
            recordResponse(CommerceVendorCommunicationTargetType.TRADE_REGISTER, command.orderKey(), response);

            if (!response.isSuccess()) {
                log.error("KCP 거래등록 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return TradeRegisterVendorResult.builder()
                    .resCd(response.resCd())
                    .resMsg(response.resMsg())
                    .approvalKey(response.approvalKey())
                    .payUrl(response.payUrl())
                    .traceNo(response.traceNo())
                    .rawResponse(kcpApiClient.toJsonNode(response).toString())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.TRADE_REGISTER, command.orderKey(), e);
            throw e;
        }
    }

    private void recordRequest(CommerceVendorCommunicationTargetType targetType, String targetId, Object request) {
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

    private void recordResponse(CommerceVendorCommunicationTargetType targetType, String targetId, Object response) {
        JsonNode payloadJson = kcpApiClient.toJsonNode(response);
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

    private void recordError(CommerceVendorCommunicationTargetType targetType, String targetId, Exception e) {
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
