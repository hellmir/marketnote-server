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
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.*;
import com.personal.marketnote.commerce.utility.VendorCommunicationRecorder;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
    public String getVendorKey() {
        return "NHN_KCP";
    }

    @Override
    public String getShopCode() {
        return kcpProperties.getSiteCd();
    }

    @Override
    public TradeRegisterVendorResult registerTrade(TradeRegisterVendorCommand command) {
        String mobilePayMethod = PaymentMethod.valueOf(command.payMethod()).getMobileCode();

        KcpTradeRegisterRequest request = KcpTradeRegisterRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .ordrIdxx(command.orderKey())
                .goodMny(command.orderAmount())
                .payMethod(mobilePayMethod)
                .goodName(command.goodName())
                .retUrl(kcpProperties.getRetUrl())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.TRADE_REGISTER, command.orderKey(), request);

        try {
            KcpTradeRegisterResponse response = kcpApiClient.registerTrade(request);
            recordResponse(CommerceVendorCommunicationTargetType.TRADE_REGISTER, command.orderKey(), response);

            boolean isSuccess = response.isSuccess();
            if (!isSuccess) {
                log.error("KCP 거래등록 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return TradeRegisterVendorResult.builder()
                    .success(isSuccess)
                    .resultCode(response.resCd())
                    .resultMessage(response.resMsg())
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
                .ordrMony(command.orderAmount())
                .ordrNo(command.orderNumber())
                .payType(command.payType())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL, command.orderNumber(), request);

        return executeApprovalWithRetry(request, command.orderNumber());
    }

    private PaymentApprovalVendorResult executeApprovalWithRetry(
            KcpPaymentApprovalRequest request, String orderNo
    ) {
        KcpProperties.Retry retryConfig = kcpProperties.getRetry();
        long sleepMillis = retryConfig.getInitialDelayMs();
        int maxAttempts = retryConfig.getMaxAttempts();
        int readTimeoutAttemptCount = 0;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                KcpPaymentApprovalResponse response = kcpApiClient.approvePayment(request);
                recordResponse(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL, orderNo, response);
                return buildApprovalResult(response);
            } catch (Exception e) {
                lastException = e;
                recordError(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL, orderNo, e);

                if (isConnectionFailure(e)) {
                    log.warn("KCP 결제승인 연결 실패 - orderNo: {}, attempt: {}/{}, error: {}",
                            orderNo, attempt, maxAttempts, e.getMessage());
                    if (attempt < maxAttempts) {
                        sleep(sleepMillis);
                        sleepMillis *= retryConfig.getBackoffMultiplier();
                        continue;
                    }
                    throw new PaymentVendorConnectionFailedException(
                            "KCP 연결 실패: " + e.getMessage(), e
                    );
                }

                if (isReadTimeout(e)) {
                    readTimeoutAttemptCount++;
                    log.warn("KCP 결제승인 읽기 타임아웃 - orderNo: {}, attempt: {}/{}, readTimeoutCount: {}/{}",
                            orderNo, attempt, maxAttempts, readTimeoutAttemptCount, retryConfig.getReadTimeoutMaxAttempts());
                    if (readTimeoutAttemptCount < retryConfig.getReadTimeoutMaxAttempts() && attempt < maxAttempts) {
                        sleep(sleepMillis);
                        sleepMillis *= retryConfig.getBackoffMultiplier();
                        continue;
                    }
                    throw new KcpCommunicationException(
                            "KCP 결제승인 읽기 타임아웃 초과: " + e.getMessage(), e
                    );
                }

                throw e;
            }
        }

        throw new KcpCommunicationException("KCP 결제승인 재시도 소진", lastException);
    }

    private PaymentApprovalVendorResult buildApprovalResult(KcpPaymentApprovalResponse response) {
        return PaymentApprovalVendorResult.builder()
                .success(response.isSuccess())
                .resultCode(response.resCd())
                .resultMessage(response.resMsg())
                .resultEnMessage(response.resEnMsg())
                .transactionId(response.tno())
                .amount(response.amount())
                .payMethod(response.payMethod())
                .cardCode(response.cardCd())
                .cardName(response.cardName())
                .cardNumber(response.cardNo())
                .approvalNumber(response.appNo())
                .approvalTime(response.appTime())
                .installmentInfo(response.noinf())
                .installmentType(response.noinfType())
                .installmentMonths(response.quota())
                .cardAmount(response.cardMny())
                .couponAmount(response.couponMny())
                .partialCancelYn(response.partcancYn())
                .cardBinType01(response.cardBinType01())
                .cardBinType02(response.cardBinType02())
                .rawResponse(kcpApiClient.toJsonNode(response).toString())
                .build();
    }

    private boolean isConnectionFailure(Throwable throwable) {
        return hasCause(throwable, ConnectException.class)
                || hasCause(throwable, UnknownHostException.class);
    }

    private boolean isReadTimeout(Throwable throwable) {
        return hasCause(throwable, SocketTimeoutException.class);
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (FormatValidator.hasValue(current)) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleep(long millis) {
        try {
            long jitteredSleepMillis = ThreadLocalRandom.current()
                    .nextLong(Math.max(1L, millis) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public PaymentCancelVendorResult cancelPayment(PaymentCancelVendorCommand command) {
        String certInfo = kcpCertificateLoader.loadCertInfo();
        String signData = kcpSignatureGenerator.generateSignData(
                kcpProperties.getSiteCd(),
                command.transactionId(),
                command.cancelType()
        );

        KcpPaymentCancelRequest request = KcpPaymentCancelRequest.builder()
                .siteCd(kcpProperties.getSiteCd())
                .tno(command.transactionId())
                .kcpCertInfo(certInfo)
                .kcpSignData(signData)
                .modType(command.cancelType())
                .modMny(String.valueOf(command.cancelAmount()))
                .remMny(String.valueOf(command.remainAmount()))
                .modDesc(command.cancelReason())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL, command.transactionId(), request);

        try {
            KcpPaymentCancelResponse response = kcpApiClient.cancelPayment(request);
            recordResponse(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL, command.transactionId(), response);

            boolean isSuccess = response.isSuccess();
            if (!isSuccess) {
                log.error("KCP 결제취소 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return PaymentCancelVendorResult.builder()
                    .success(isSuccess)
                    .resultCode(response.resCd())
                    .resultMessage(response.resMsg())
                    .amount(response.amount())
                    .rawResponse(kcpApiClient.toJsonNode(response).toString())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.PAYMENT_CANCEL, command.transactionId(), e);
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
