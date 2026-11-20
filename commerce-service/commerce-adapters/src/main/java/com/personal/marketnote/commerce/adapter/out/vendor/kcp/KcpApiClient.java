package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.*;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class KcpApiClient {

    private static final String OP_TRADE_REGISTER = "trade.register";
    private static final String OP_PAYMENT_APPROVE = "payment.approve";
    private static final String OP_PAYMENT_CANCEL = "payment.cancel";
    private static final String OP_BATCH_KEY_ISSUE = "batch.key.issue";
    private static final String OP_BATCH_PAYMENT_APPROVE = "batch.payment.approve";
    private static final String OP_BATCH_KEY_DELETE = "batch.key.delete";

    private static final String TAG_SITE_CD = "siteCd";
    private static final String TAG_ORDR_IDXX = "ordrIdxx";
    private static final String TAG_ORDR_NO = "ordrNo";
    private static final String TAG_TNO = "tno";
    private static final String TAG_RESPONSE_CODE = "responseCode";

    private final KcpProperties kcpProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ExternalCallLogger externalCallLogger;

    public KcpApiClient(
            KcpProperties kcpProperties,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            ExternalCallLogger externalCallLogger
    ) {
        this.kcpProperties = kcpProperties;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.externalCallLogger = externalCallLogger;
    }

    public KcpTradeRegisterResponse registerTrade(KcpTradeRegisterRequest request) {
        String url = kcpProperties.getApi().getTradeRegisterUrl();

        log.info("KCP 거래등록 요청: url={}, site_cd={}, ordr_idxx={}, good_mny={}, pay_method={}, good_name={}, Ret_URL={}",
                url, request.siteCd(), request.ordrIdxx(), request.goodMny(),
                request.payMethod(), request.goodName(), request.retUrl()
        );

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_SITE_CD, request.siteCd());
        putIfPresent(tags, TAG_ORDR_IDXX, request.ordrIdxx());

        return externalCallLogger.record(
                ExternalSystem.KCP, OP_TRADE_REGISTER, tags,
                () -> {
                    KcpTradeRegisterResponse response = postAndParse(
                            url, request, KcpTradeRegisterResponse.class, "KCP 거래등록"
                    );
                    putIfPresent(tags, TAG_RESPONSE_CODE, response.resCd());
                    return response;
                }
        );
    }

    public KcpPaymentApprovalResponse approvePayment(KcpPaymentApprovalRequest request) {
        String url = kcpProperties.getApi().getPaymentApprovalUrl();

        log.info("KCP 결제승인 요청: url={}, ordrNo={}", url, request.ordrNo());

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_SITE_CD, request.siteCd());
        putIfPresent(tags, TAG_ORDR_NO, request.ordrNo());

        return externalCallLogger.record(
                ExternalSystem.KCP, OP_PAYMENT_APPROVE, tags,
                () -> {
                    KcpPaymentApprovalResponse response = postAndParse(
                            url, request, KcpPaymentApprovalResponse.class, "KCP 결제승인"
                    );
                    putIfPresent(tags, TAG_RESPONSE_CODE, response.resCd());
                    return response;
                }
        );
    }

    public KcpPaymentCancelResponse cancelPayment(KcpPaymentCancelRequest request) {
        String url = kcpProperties.getApi().getPaymentCancelUrl();

        log.info("KCP 결제취소 요청: url={}, tno={}", url, request.tno());

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_SITE_CD, request.siteCd());
        putIfPresent(tags, TAG_TNO, request.tno());

        return externalCallLogger.record(
                ExternalSystem.KCP, OP_PAYMENT_CANCEL, tags,
                () -> {
                    KcpPaymentCancelResponse response = postAndParse(
                            url, request, KcpPaymentCancelResponse.class, "KCP 결제취소"
                    );
                    putIfPresent(tags, TAG_RESPONSE_CODE, response.resCd());
                    return response;
                }
        );
    }

    public KcpBatchKeyIssuanceResponse issueBatchKey(KcpBatchKeyIssuanceRequest request) {
        String url = kcpProperties.getApi().getBatchKeyIssuanceUrl();

        log.info("KCP 배치키 발급 요청: url={}, site_cd={}", url, request.siteCd());

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_SITE_CD, request.siteCd());

        return externalCallLogger.record(
                ExternalSystem.KCP, OP_BATCH_KEY_ISSUE, tags,
                () -> {
                    KcpBatchKeyIssuanceResponse response = postAndParse(
                            url, request, KcpBatchKeyIssuanceResponse.class, "KCP 배치키 발급"
                    );
                    putIfPresent(tags, TAG_RESPONSE_CODE, response.resCd());
                    return response;
                }
        );
    }

    public KcpBatchPaymentApprovalResponse approveBatchPayment(KcpBatchPaymentApprovalRequest request) {
        String url = kcpProperties.getApi().getBatchPaymentApprovalUrl();

        log.info("KCP 배치 결제승인 요청: url={}, site_cd={}, ordr_idxx={}",
                url, request.siteCd(), request.ordrIdxx()
        );

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_SITE_CD, request.siteCd());
        putIfPresent(tags, TAG_ORDR_IDXX, request.ordrIdxx());

        return externalCallLogger.record(
                ExternalSystem.KCP, OP_BATCH_PAYMENT_APPROVE, tags,
                () -> {
                    KcpBatchPaymentApprovalResponse response = postAndParse(
                            url, request, KcpBatchPaymentApprovalResponse.class, "KCP 배치 결제승인"
                    );
                    putIfPresent(tags, TAG_RESPONSE_CODE, response.resCd());
                    return response;
                }
        );
    }

    public KcpBatchKeyDeletionResponse deleteBatchKey(KcpBatchKeyDeletionRequest request) {
        String url = kcpProperties.getApi().getBatchPaymentApprovalUrl();

        log.info("KCP 배치키 삭제 요청: url={}, site_cd={}", url, request.siteCd());

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_SITE_CD, request.siteCd());

        return externalCallLogger.record(
                ExternalSystem.KCP, OP_BATCH_KEY_DELETE, tags,
                () -> {
                    KcpBatchKeyDeletionResponse response = postAndParse(
                            url, request, KcpBatchKeyDeletionResponse.class, "KCP 배치키 삭제"
                    );
                    putIfPresent(tags, TAG_RESPONSE_CODE, response.resCd());
                    return response;
                }
        );
    }

    public JsonNode toJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }

    private <T> T postAndParse(String url, Object request, Class<T> responseType, String operationLabel) {
        ResponseEntity<String> rawResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        String responseBody = rawResponse.getBody();
        log.debug("{} 응답 Content-Type={}, body={}",
                operationLabel, rawResponse.getHeaders().getContentType(), responseBody
        );

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new KcpCommunicationException(operationLabel + " 응답 본문이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (Exception e) {
            log.error("KCP 응답 파싱 실패. operationLabel={}", operationLabel, e);
            throw new KcpCommunicationException(operationLabel + " 응답 파싱 실패", e);
        }
    }

    private Map<String, String> mutableTags() {
        return new HashMap<>();
    }

    private void putIfPresent(Map<String, String> tags, String key, String value) {
        if (FormatValidator.hasNoValue(value)) {
            return;
        }
        tags.put(key, value);
    }
}
