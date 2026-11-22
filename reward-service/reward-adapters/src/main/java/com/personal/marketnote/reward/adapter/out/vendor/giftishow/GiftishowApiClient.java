package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.*;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowBrandCodeException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowGoodsCodeException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowPhoneNoException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowTrIdException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowUserIdException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class GiftishowApiClient {

    private static final String OP_PRODUCT_LIST = "product.list";
    private static final String OP_PRODUCT_DETAIL = "product.detail";
    private static final String OP_BRAND_LIST = "brand.list";
    private static final String OP_BRAND_DETAIL = "brand.detail";
    private static final String OP_COUPON_SEND = "coupon.send";
    private static final String OP_COUPON_DETAIL = "coupon.detail";
    private static final String OP_COUPON_CANCEL = "coupon.cancel";
    private static final String OP_COUPON_RESEND = "coupon.resend";
    private static final String OP_BIZMONEY_BALANCE = "bizmoney.balance";
    private static final String OP_COUPON_SEND_FAIL_CANCEL = "coupon.sendFailCancel";

    private static final String TAG_TR_ID = "tr_id";
    private static final String TAG_GOODS_CODE = "goods_code";
    private static final String TAG_BRAND_CODE = "brand_code";
    private static final String TAG_USER_ID = "user_id";

    private final GiftishowApiProperties properties;
    private final RestClient giftishowRestClient;
    private final ObjectMapper objectMapper;
    private final ExternalCallLogger externalCallLogger;

    public GiftishowApiClient(
            GiftishowApiProperties properties,
            RestClient giftishowRestClient,
            ObjectMapper objectMapper,
            ExternalCallLogger externalCallLogger
    ) {
        this.properties = properties;
        this.giftishowRestClient = giftishowRestClient;
        this.objectMapper = objectMapper;
        this.externalCallLogger = externalCallLogger;
    }

    /**
     * 0101 - 상품 리스트 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowProductListResponse> getProductList(int start, int size) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("start", String.valueOf(start));
        params.add("size", String.valueOf(size));

        log.info("기프티쇼 상품 리스트 조회 요청: start={}, size={}", start, size);

        Map<String, String> tags = mutableTags();

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_PRODUCT_LIST, tags,
                () -> executePost(
                        properties.getApi().getProductListPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0111 - 상품 상세 조회 (POST + URL path variable)
     */
    public GiftishowApiResponse<GiftishowProductDetailResponse> getProductDetail(String goodsCode) {
        if (FormatValidator.hasNoValue(goodsCode)) {
            throw new InvalidGiftishowGoodsCodeException();
        }
        MultiValueMap<String, String> params = createAuthParams();

        log.info("기프티쇼 상품 상세 조회 요청: goodsCode={}", goodsCode);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_GOODS_CODE, goodsCode);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_PRODUCT_DETAIL, tags,
                () -> executePostWithPathVariable(
                        properties.getApi().getProductDetailPath(),
                        goodsCode,
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0102 - 브랜드 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowBrandListResponse> getBrandList() {
        MultiValueMap<String, String> params = createAuthParams();

        log.info("기프티쇼 브랜드 조회 요청");

        Map<String, String> tags = mutableTags();

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_BRAND_LIST, tags,
                () -> executePost(
                        properties.getApi().getBrandListPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0112 - 브랜드 상세 조회 (POST + URL path variable)
     */
    public GiftishowApiResponse<GiftishowBrandDetailResponse> getBrandDetail(String brandCode) {
        if (FormatValidator.hasNoValue(brandCode)) {
            throw new InvalidGiftishowBrandCodeException();
        }
        MultiValueMap<String, String> params = createAuthParams();

        log.info("기프티쇼 브랜드 상세 조회 요청: brandCode={}", brandCode);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_BRAND_CODE, brandCode);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_BRAND_DETAIL, tags,
                () -> executePostWithPathVariable(
                        properties.getApi().getBrandDetailPath(),
                        brandCode,
                        params,
                        new TypeReference<>() {
                        }
                )
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
        if (FormatValidator.hasNoValue(trId)) {
            throw new InvalidGiftishowTrIdException();
        }
        if (FormatValidator.hasNoValue(goodsCode)) {
            throw new InvalidGiftishowGoodsCodeException();
        }
        if (FormatValidator.hasNoValue(phoneNo)) {
            throw new InvalidGiftishowPhoneNoException();
        }
        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidGiftishowUserIdException();
        }
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

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        putIfPresent(tags, TAG_GOODS_CODE, goodsCode);
        putIfPresent(tags, TAG_USER_ID, userId);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_COUPON_SEND, tags,
                () -> executePost(
                        properties.getApi().getCouponSendPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0201 - 쿠폰 상세 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponDetailResponse> getCouponDetail(String trId) {
        if (FormatValidator.hasNoValue(trId)) {
            throw new InvalidGiftishowTrIdException();
        }
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);

        log.info("기프티쇼 쿠폰 상세 조회 요청: trId={}", trId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_COUPON_DETAIL, tags,
                () -> executePost(
                        properties.getApi().getCouponDetailPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0202 - 쿠폰 취소 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponCancelResponse> cancelCoupon(String trId, String userId) {
        if (FormatValidator.hasNoValue(trId)) {
            throw new InvalidGiftishowTrIdException();
        }
        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidGiftishowUserIdException();
        }
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("user_id", userId);

        log.info("기프티쇼 쿠폰 취소 요청: trId={}, userId={}", trId, userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        putIfPresent(tags, TAG_USER_ID, userId);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_COUPON_CANCEL, tags,
                () -> executePost(
                        properties.getApi().getCouponCancelPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0203 - 쿠폰 재전송 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponResendResponse> resendCoupon(String trId, String phoneNo) {
        if (FormatValidator.hasNoValue(trId)) {
            throw new InvalidGiftishowTrIdException();
        }
        if (FormatValidator.hasNoValue(phoneNo)) {
            throw new InvalidGiftishowPhoneNoException();
        }
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("phone_no", phoneNo);

        log.info("기프티쇼 쿠폰 재전송 요청: trId={}", trId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_COUPON_RESEND, tags,
                () -> executePost(
                        properties.getApi().getCouponResendPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0301 - 비즈머니 잔액 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowBizMoneyResponse> getBizMoneyBalance(String userId) {
        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidGiftishowUserIdException();
        }
        MultiValueMap<String, String> params = createAuthParams();
        params.add("user_id", userId);

        log.info("기프티쇼 비즈머니 잔액 조회 요청: userId={}", userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_USER_ID, userId);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_BIZMONEY_BALANCE, tags,
                () -> executePost(
                        properties.getApi().getBizMoneyBalancePath(),
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0205 - 발송실패 취소 (POST)
     */
    public GiftishowApiResponse<GiftishowCouponCancelResponse> cancelSendFailedCoupon(String trId, String userId) {
        if (FormatValidator.hasNoValue(trId)) {
            throw new InvalidGiftishowTrIdException();
        }
        if (FormatValidator.hasNoValue(userId)) {
            throw new InvalidGiftishowUserIdException();
        }
        MultiValueMap<String, String> params = createAuthParams();
        params.add("tr_id", trId);
        params.add("user_id", userId);

        log.info("기프티쇼 발송실패 취소 요청: trId={}, userId={}", trId, userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        putIfPresent(tags, TAG_USER_ID, userId);

        return externalCallLogger.record(
                ExternalSystem.GIFTISHOW, OP_COUPON_SEND_FAIL_CANCEL, tags,
                () -> executePost(
                        properties.getApi().getCouponSendFailCancelPath(),
                        params,
                        new TypeReference<>() {
                        }
                )
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
        ResponseEntity<String> rawResponse;
        try {
            rawResponse = giftishowRestClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException re) {
            throw wrapHttpFailure(path, re);
        }

        return parseResponse(rawResponse, path, typeReference);
    }

    private <T> GiftishowApiResponse<T> executePostWithPathVariable(
            String basePath,
            String pathVariable,
            MultiValueMap<String, String> params,
            TypeReference<GiftishowApiResponse<T>> typeReference
    ) {
        ResponseEntity<String> rawResponse;
        try {
            rawResponse = giftishowRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(basePath)
                            .pathSegment(pathVariable)
                            .build())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException re) {
            throw wrapHttpFailure(basePath, re);
        }

        return parseResponse(rawResponse, basePath, typeReference);
    }

    private GiftishowCommunicationException wrapHttpFailure(String path, RestClientResponseException re) {
        int statusCode = re.getStatusCode().value();
        if (isSensitivePath(path)) {
            log.error(
                    "기프티쇼 API HTTP 응답 실패: path={}, status={}, exception={} (민감 데이터 포함 응답 — 메시지/스택 로깅 생략)",
                    path, statusCode, re.getClass().getSimpleName()
            );
            return new GiftishowCommunicationException(
                    "기프티쇼 API HTTP 응답 실패. path=" + path + ", status=" + statusCode
            );
        }
        log.error("기프티쇼 API HTTP 응답 실패: path={}, status={}", path, statusCode, re);
        return new GiftishowCommunicationException(
                "기프티쇼 API HTTP 응답 실패. path=" + path + ", status=" + statusCode, re
        );
    }

    private <T> GiftishowApiResponse<T> parseResponse(
            ResponseEntity<String> rawResponse,
            String path,
            TypeReference<GiftishowApiResponse<T>> typeReference
    ) {
        String responseBody = rawResponse.getBody();
        logApiResponse(path, rawResponse, responseBody);

        if (FormatValidator.hasNoValue(responseBody)) {
            throw new GiftishowCommunicationException(
                    "기프티쇼 API 응답 본문이 비어 있습니다. path=" + path
            );
        }

        try {
            return objectMapper.readValue(responseBody, typeReference);
        } catch (JsonProcessingException e) {
            logParseFailure(path, e);
            if (isSensitivePath(path)) {
                throw new GiftishowCommunicationException(
                        "기프티쇼 API 응답 파싱 실패. path=" + path
                );
            }
            throw new GiftishowCommunicationException(
                    "기프티쇼 API 응답 파싱 실패. path=" + path, e
            );
        }
    }

    private void logParseFailure(String path, JsonProcessingException e) {
        if (isSensitivePath(path)) {
            log.error(
                    "기프티쇼 API 응답 파싱 실패: path={}, exception={} (민감 데이터 포함 응답 — 메시지/스택 로깅 생략)",
                    path, e.getClass().getSimpleName()
            );
            return;
        }
        log.error("기프티쇼 API 응답 파싱 실패: path={}", path, e);
    }

    private void logApiResponse(String path, ResponseEntity<String> rawResponse, String responseBody) {
        if (isSensitivePath(path)) {
            log.debug("기프티쇼 API 응답: path={}, status={} (body 로깅 생략 — 민감 데이터 포함)",
                    path, rawResponse.getStatusCode());
            return;
        }
        log.debug("기프티쇼 API 응답: path={}, status={}, body={}",
                path, rawResponse.getStatusCode(), responseBody);
    }

    private boolean isSensitivePath(String path) {
        return properties.getApi().getSensitivePaths().contains(path);
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
