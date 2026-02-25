package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.*;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.domain.payment.PaymentMethod;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.*;
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
    private static final String PAYMENT_APPROVAL_TRAN_CD = "00100000";
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

    @Override
    public PaymentApprovalVendorResult approvePayment(PaymentApprovalVendorCommand command) {
        String certInfo = kcpCertificateLoader.loadCertInfo();

        KcpPaymentApprovalRequest request = KcpPaymentApprovalRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .encData(command.encData())
                .encInfo(command.encInfo())
                .tranCd(PAYMENT_APPROVAL_TRAN_CD)
                .kcpCertInfo(certInfo)
                .ordrMony(command.ordrMony())
                .ordrNo(command.ordrNo())
                .payType(command.payType())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL, command.ordrNo(), request);

        try {
            KcpPaymentApprovalResponse response = kcpApiClient.approvePayment(request);
            recordResponse(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL, command.ordrNo(), response);

            return PaymentApprovalVendorResult.builder()
                    .resCd(response.resCd())
                    .resMsg(response.resMsg())
                    .resEnMsg(response.resEnMsg())
                    .tno(response.tno())
                    .amount(response.amount())
                    .payMethod(response.payMethod())
                    .cardCd(response.cardCd())
                    .cardName(response.cardName())
                    .cardNo(response.cardNo())
                    .appNo(response.appNo())
                    .appTime(response.appTime())
                    .noinf(response.noinf())
                    .noinfType(response.noinfType())
                    .quota(response.quota())
                    .cardMny(response.cardMny())
                    .couponMny(response.couponMny())
                    .partcancYn(response.partcancYn())
                    .cardBinType01(response.cardBinType01())
                    .cardBinType02(response.cardBinType02())
                    .rawResponse(kcpApiClient.toJsonNode(response).toString())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL, command.ordrNo(), e);
            throw e;
        }
    }

    @Override
    public PaymentCancelVendorResult cancelPayment(PaymentCancelVendorCommand command) {
        String certInfo = kcpCertificateLoader.loadCertInfo();
        String signData = kcpSignatureGenerator.generateSignData(
                kcpProperties.getSiteCd(),
                command.tno(),
                command.modType()
        );

        KcpPaymentCancelRequest request = KcpPaymentCancelRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .tno(command.tno())
                .kcpCertInfo(certInfo)
                .kcpSignData(signData)
                .modType(command.modType())
                .modMny(String.valueOf(command.modMny()))
                .remMny(String.valueOf(command.remMny()))
                .modDesc(command.modDesc())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL, command.tno(), request);

        try {
            KcpPaymentCancelResponse response = kcpApiClient.cancelPayment(request);
            recordResponse(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL, command.tno(), response);

            if (!response.isSuccess()) {
                log.error("KCP 결제취소 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return PaymentCancelVendorResult.builder()
                    .resCd(response.resCd())
                    .resMsg(response.resMsg())
                    .amount(response.amount())
                    .rawResponse(kcpApiClient.toJsonNode(response).toString())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL, command.tno(), e);
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
