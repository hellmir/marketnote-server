package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.*;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.port.out.quickpayment.*;
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
public class KcpQuickPaymentAdapter implements RegisterQuickPaymentTransactionPort, IssueBatchKeyPort, ApproveQuickPaymentPort, DeleteBatchKeyPort {
    private static final CommerceVendorName VENDOR_NAME = CommerceVendorName.NHN_KCP;
    private static final String GOOD_MNY = "0";
    private static final String PAY_METHOD = "AUTH";
    private static final String GOOD_NAME = "빠른결제 카드 등록";
    private static final String BATCH_KEY_TRAN_CD = "00300001";
    private static final String MASKED = "***MASKED***";
    private static final String BATCH_PAYMENT_PAY_METHOD = "CARD";
    private static final String BATCH_PAYMENT_QUOTA = "00";
    private static final String BATCH_PAYMENT_CURRENCY = "410";
    private static final String BATCH_PAYMENT_CARD_TX_TYPE = "11511000";
    private static final String BATCH_PAYMENT_MEDIA_TYPE = "MC02";
    private static final String BATCH_KEY_DELETE_PAY_METHOD = "BATCH";
    private static final String BATCH_KEY_DELETE_TX_TYPE = "10005010";
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "kcp_cert_info", "enc_data", "enc_info", "batch_key", "bt_batch_key"
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

        recordRequest(
                CommerceVendorCommunicationTargetType.QUICK_PAYMENT_TRADE_REGISTER, command.transactionId(), request
        );

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
            recordResponse(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_ISSUANCE, null, response);

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

    @Override
    public ApproveQuickPaymentPortResult approvePayment(ApproveQuickPaymentPortCommand command) {
        String certInfo = kcpCertificateLoader.loadCertInfo();

        KcpBatchPaymentApprovalRequest request = KcpBatchPaymentApprovalRequest.builder()
                .kcpCertInfo(certInfo)
                .siteCd(kcpProperties.getSiteCd())
                .payMethod(BATCH_PAYMENT_PAY_METHOD)
                .amount(command.amount())
                .cardMny(command.amount())
                .quota(BATCH_PAYMENT_QUOTA)
                .currency(BATCH_PAYMENT_CURRENCY)
                .ordrIdxx(command.orderKey())
                .goodName(command.goodName())
                .cardTxType(BATCH_PAYMENT_CARD_TX_TYPE)
                .btBatchKey(command.batchKey())
                .btGroupId(command.groupId())
                .mediaType(BATCH_PAYMENT_MEDIA_TYPE)
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_APPROVAL, command.orderKey(), request);

        return executeBatchApprovalWithRetry(request, command.orderKey());
    }

    @Override
    public DeleteBatchKeyPortResult deleteBatchKey(DeleteBatchKeyPortCommand command) {
        String certInfo = kcpCertificateLoader.loadCertInfo();

        KcpBatchKeyDeletionRequest request = KcpBatchKeyDeletionRequest.builder()
                .kcpCertInfo(certInfo)
                .siteCd(kcpProperties.getSiteCd())
                .payMethod(BATCH_KEY_DELETE_PAY_METHOD)
                .txType(BATCH_KEY_DELETE_TX_TYPE)
                .groupId(command.groupId())
                .batchKey(command.batchKey())
                .build();

        recordRequest(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_DELETION,
                null, request);

        try {
            KcpBatchKeyDeletionResponse response = kcpApiClient.deleteBatchKey(request);
            recordResponse(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_DELETION,
                    null, response);

            boolean isSuccess = response.isSuccess();
            if (!isSuccess) {
                log.error("빠른결제 배치키 삭제 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
            }

            return DeleteBatchKeyPortResult.builder()
                    .success(isSuccess)
                    .resultCode(response.resCd())
                    .resultMessage(response.resMsg())
                    .build();
        } catch (KcpCommunicationException e) {
            recordError(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_KEY_DELETION,
                    null, e);
            throw e;
        }
    }

    private ApproveQuickPaymentPortResult executeBatchApprovalWithRetry(
            KcpBatchPaymentApprovalRequest request, String orderKey
    ) {
        KcpProperties.Retry retryConfig = kcpProperties.getRetry();
        long sleepMillis = retryConfig.getInitialDelayMs();
        int maxAttempts = retryConfig.getMaxAttempts();
        int readTimeoutAttemptCount = 0;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                KcpBatchPaymentApprovalResponse response = kcpApiClient.approveBatchPayment(request);
                recordResponse(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_APPROVAL, orderKey, response);

                return buildBatchApprovalResult(response);
            } catch (Exception e) {
                lastException = e;
                recordError(CommerceVendorCommunicationTargetType.QUICK_PAYMENT_BATCH_APPROVAL, orderKey, e);

                if (isConnectionFailure(e)) {
                    log.warn("빠른결제 배치 결제승인 연결 실패 - orderKey: {}, attempt: {}/{}, error: {}",
                            orderKey, attempt, maxAttempts, e.getMessage());
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
                    log.warn("빠른결제 배치 결제승인 읽기 타임아웃 - orderKey: {}, attempt: {}/{}, readTimeoutCount: {}/{}",
                            orderKey, attempt, maxAttempts, readTimeoutAttemptCount, retryConfig.getReadTimeoutMaxAttempts());
                    if (readTimeoutAttemptCount < retryConfig.getReadTimeoutMaxAttempts() && attempt < maxAttempts) {
                        sleep(sleepMillis);
                        sleepMillis *= retryConfig.getBackoffMultiplier();
                        continue;
                    }

                    throw new KcpCommunicationException(
                            "빠른결제 배치 결제승인 읽기 타임아웃 초과: " + e.getMessage(), e
                    );
                }

                throw e;
            }
        }

        throw new KcpCommunicationException("빠른결제 배치 결제승인 재시도 소진", lastException);
    }

    private ApproveQuickPaymentPortResult buildBatchApprovalResult(KcpBatchPaymentApprovalResponse response) {
        boolean isSuccess = response.isSuccess();
        if (!isSuccess) {
            log.error("빠른결제 배치 결제승인 실패: resCd={}, resMsg={}", response.resCd(), response.resMsg());
        }

        return ApproveQuickPaymentPortResult.builder()
                .success(isSuccess)
                .resultCode(response.resCd())
                .resultMessage(response.resMsg())
                .transactionId(response.tno())
                .amount(response.amount())
                .payMethod(response.payMethod())
                .cardCode(response.cardCd())
                .cardName(response.cardName())
                .cardNumber(response.cardNo())
                .approvalNumber(response.appNo())
                .approvalTime(response.appTime())
                .installmentMonths(response.quota())
                .cardAmount(response.cardMny())
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
