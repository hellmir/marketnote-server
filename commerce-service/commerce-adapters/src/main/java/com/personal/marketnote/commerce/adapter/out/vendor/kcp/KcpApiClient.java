package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.*;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class KcpApiClient {
    private final KcpProperties kcpProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KcpApiClient(
            KcpProperties kcpProperties,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.kcpProperties = kcpProperties;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public KcpTradeRegisterResponse registerTrade(KcpTradeRegisterRequest request) {
        String url = kcpProperties.getApi().getTradeRegisterUrl();

        log.info("KCP 거래등록 요청: url={}, site_cd={}, ordr_idxx={}, good_mny={}, pay_method={}, good_name={}, Ret_URL={}",
                url, request.siteCd(), request.ordrIdxx(), request.goodMny(),
                request.payMethod(), request.goodName(), request.retUrl());

        ResponseEntity<String> rawResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        String responseBody = rawResponse.getBody();
        log.debug("KCP 거래등록 응답 Content-Type={}, body={}", rawResponse.getHeaders().getContentType(), responseBody);

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new KcpCommunicationException("KCP 거래등록 응답 본문이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(responseBody, KcpTradeRegisterResponse.class);
        } catch (Exception e) {
            log.error("KCP 거래등록 응답 파싱 실패: {}", e.getMessage());
            throw new KcpCommunicationException("KCP 거래등록 응답 파싱 실패", e);
        }
    }

    public KcpPaymentApprovalResponse approvePayment(KcpPaymentApprovalRequest request) {
        String url = kcpProperties.getApi().getPaymentApprovalUrl();

        log.info("KCP 결제승인 요청: url={}, ordrNo={}", url, request.ordrNo());

        ResponseEntity<String> rawResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        String responseBody = rawResponse.getBody();
        log.debug("KCP 결제승인 응답 Content-Type={}, body={}", rawResponse.getHeaders().getContentType(), responseBody);

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new KcpCommunicationException("KCP 결제승인 응답 본문이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(responseBody, KcpPaymentApprovalResponse.class);
        } catch (Exception e) {
            log.error("KCP 결제승인 응답 파싱 실패: {}", e.getMessage());
            throw new KcpCommunicationException("KCP 결제승인 응답 파싱 실패", e);
        }
    }

    public KcpPaymentCancelResponse cancelPayment(KcpPaymentCancelRequest request) {
        String url = kcpProperties.getApi().getPaymentCancelUrl();

        log.info("KCP 결제취소 요청: url={}, tno={}", url, request.tno());

        ResponseEntity<String> rawResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        String responseBody = rawResponse.getBody();
        log.debug("KCP 결제취소 응답 Content-Type={}, body={}", rawResponse.getHeaders().getContentType(), responseBody);

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new KcpCommunicationException("KCP 결제취소 응답 본문이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(responseBody, KcpPaymentCancelResponse.class);
        } catch (Exception e) {
            log.error("KCP 결제취소 응답 파싱 실패: {}", e.getMessage());
            throw new KcpCommunicationException("KCP 결제취소 응답 파싱 실패", e);
        }
    }

    public KcpBatchKeyIssuanceResponse issueBatchKey(KcpBatchKeyIssuanceRequest request) {
        String url = kcpProperties.getApi().getBatchKeyIssuanceUrl();

        log.info("KCP 배치키 발급 요청: url={}, site_cd={}", url, request.siteCd());

        ResponseEntity<String> rawResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class);

        String responseBody = rawResponse.getBody();
        log.debug("KCP 배치키 발급 응답 Content-Type={}, body={}", rawResponse.getHeaders().getContentType(), responseBody);

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new KcpCommunicationException("KCP 배치키 발급 응답 본문이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(responseBody, KcpBatchKeyIssuanceResponse.class);
        } catch (Exception e) {
            log.error("KCP 배치키 발급 응답 파싱 실패: {}", e.getMessage());
            throw new KcpCommunicationException("KCP 배치키 발급 응답 파싱 실패", e);
        }
    }

    public JsonNode toJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }
}
