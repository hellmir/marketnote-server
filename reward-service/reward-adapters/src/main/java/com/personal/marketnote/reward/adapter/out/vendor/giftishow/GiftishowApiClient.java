package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.*;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GiftishowApiClient {

    private final GiftishowApiProperties properties;
    private final RestClient giftishowRestClient;
    private final ObjectMapper objectMapper;

    public GiftishowApiClient(
            GiftishowApiProperties properties,
            RestClient giftishowRestClient,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.giftishowRestClient = giftishowRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 0101 - 상품 리스트 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowProductListResponse> getProductList(int start, int size) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("start", String.valueOf(start));
        params.add("size", String.valueOf(size));

        log.info("기프티쇼 상품 리스트 조회 요청: start={}, size={}", start, size);

        return executePost(
                properties.getApi().getProductListPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0111 - 상품 상세 조회 (GET)
     */
    public GiftishowApiResponse<GiftishowProductDetailResponse> getProductDetail(String goodsCode) {
        log.info("기프티쇼 상품 상세 조회 요청: goodsCode={}", goodsCode);

        return executeGet(
                properties.getApi().getProductDetailPath(),
                goodsCode,
                "goods_code",
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0102 - 브랜드 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowBrandListResponse> getBrandList() {
        MultiValueMap<String, String> params = createAuthParams();

        log.info("기프티쇼 브랜드 조회 요청");

        return executePost(
                properties.getApi().getBrandListPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0112 - 브랜드 상세 조회 (GET)
     */
    public GiftishowApiResponse<GiftishowBrandDetailResponse> getBrandDetail(String brandCode) {
        log.info("기프티쇼 브랜드 상세 조회 요청: brandCode={}", brandCode);

        return executeGet(
                properties.getApi().getBrandDetailPath(),
                brandCode,
                "brand_code",
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0204 - 쿠폰 발송 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponSendResponse> sendCoupon(
            String trId,
            String goodsCode,
            String phoneNo,
            String userId,
            String mmsMsg,
            String mmsTitle
    ) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("goods_code", goodsCode);
        params.add("phone_no", phoneNo);
        params.add("user_id", userId);
        params.add("gubun", properties.getGubun());
        params.add("callback_no", properties.getCallbackNo());
        params.add("mms_msg", mmsMsg);
        params.add("mms_title", mmsTitle);

        log.info("기프티쇼 쿠폰 발송 요청: trId={}, goodsCode={}, userId={}", trId, goodsCode, userId);

        return executePost(
                properties.getApi().getCouponSendPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0201 - 쿠폰 상세 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponDetailResponse> getCouponDetail(String trId) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);

        log.info("기프티쇼 쿠폰 상세 조회 요청: trId={}", trId);

        return executePost(
                properties.getApi().getCouponDetailPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0202 - 쿠폰 취소 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponCancelResponse> cancelCoupon(String trId, String userId) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("user_id", userId);

        log.info("기프티쇼 쿠폰 취소 요청: trId={}, userId={}", trId, userId);

        return executePost(
                properties.getApi().getCouponCancelPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0203 - 쿠폰 재전송 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponResendResponse> resendCoupon(String trId, String phoneNo) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("phone_no", phoneNo);

        log.info("기프티쇼 쿠폰 재전송 요청: trId={}", trId);

        return executePost(
                properties.getApi().getCouponResendPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0301 - 비즈머니 잔액 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowBizMoneyResponse> getBizMoneyBalance(String userId) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("user_id", userId);

        log.info("기프티쇼 비즈머니 잔액 조회 요청: userId={}", userId);

        return executePost(
                properties.getApi().getBizMoneyBalancePath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    /**
     * 0205 - 발송실패 취소 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponCancelResponse> cancelSendFailedCoupon(String trId, String userId) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("user_id", userId);

        log.info("기프티쇼 발송실패 취소 요청: trId={}, userId={}", trId, userId);

        return executePost(
                properties.getApi().getCouponSendFailCancelPath(),
                params,
                new TypeReference<>() {
                }
        );
    }

    private MultiValueMap<String, String> createAuthParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("api_code", properties.getApiCode());
        params.add("auth_code", properties.getAuthCode());
        params.add("auth_token", properties.getAuthToken());
        params.add("dev_yn", properties.getDevYn());
        return params;
    }

    private <T> GiftishowApiResponse<T> executePost(
            String path,
            MultiValueMap<String, String> params,
            TypeReference<GiftishowApiResponse<T>> typeReference
    ) {
        ResponseEntity<String> rawResponse = giftishowRestClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .toEntity(String.class);

        return parseResponse(rawResponse, path, typeReference);
    }

    private <T> GiftishowApiResponse<T> executeGet(
            String path,
            String paramValue,
            String paramName,
            TypeReference<GiftishowApiResponse<T>> typeReference
    ) {
        ResponseEntity<String> rawResponse = giftishowRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("api_code", properties.getApiCode())
                        .queryParam("auth_code", properties.getAuthCode())
                        .queryParam("auth_token", properties.getAuthToken())
                        .queryParam("dev_yn", properties.getDevYn())
                        .queryParam(paramName, paramValue)
                        .build())
                .retrieve()
                .toEntity(String.class);

        return parseResponse(rawResponse, path, typeReference);
    }

    private <T> GiftishowApiResponse<T> parseResponse(
            ResponseEntity<String> rawResponse,
            String path,
            TypeReference<GiftishowApiResponse<T>> typeReference
    ) {
        String responseBody = rawResponse.getBody();
        if (isSensitivePath(path)) {
            log.debug("기프티쇼 API 응답: path={}, status={} (body 로깅 생략 — 민감 데이터 포함)",
                    path, rawResponse.getStatusCode());
        } else {
            log.debug("기프티쇼 API 응답: path={}, status={}, body={}",
                    path, rawResponse.getStatusCode(), responseBody);
        }

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new GiftishowCommunicationException(
                    "기프티쇼 API 응답 본문이 비어 있습니다. path=" + path
            );
        }

        try {
            return objectMapper.readValue(responseBody, typeReference);
        } catch (Exception e) {
            log.error("기프티쇼 API 응답 파싱 실패: path={}, error={}", path, e.getMessage());
            throw new GiftishowCommunicationException(
                    "기프티쇼 API 응답 파싱 실패. path=" + path, e
            );
        }
    }

    private boolean isSensitivePath(String path) {
        return path.equals(properties.getApi().getCouponSendPath());
    }
}
