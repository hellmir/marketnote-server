package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.*;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowBrandCodeException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowGoodsCodeException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowPhoneNoException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowTrIdException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowUserIdException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
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
import java.util.function.Supplier;

@Component
@Slf4j
public class GiftishowApiClient {

    private static final String OP_PRODUCT_LIST = GiftishowOperationTargetTypeResolver.OP_PRODUCT_LIST;
    private static final String OP_PRODUCT_DETAIL = GiftishowOperationTargetTypeResolver.OP_PRODUCT_DETAIL;
    private static final String OP_BRAND_LIST = GiftishowOperationTargetTypeResolver.OP_BRAND_LIST;
    private static final String OP_BRAND_DETAIL = GiftishowOperationTargetTypeResolver.OP_BRAND_DETAIL;
    private static final String OP_COUPON_SEND = GiftishowOperationTargetTypeResolver.OP_COUPON_SEND;
    private static final String OP_COUPON_DETAIL = GiftishowOperationTargetTypeResolver.OP_COUPON_DETAIL;
    private static final String OP_COUPON_CANCEL = GiftishowOperationTargetTypeResolver.OP_COUPON_CANCEL;
    private static final String OP_COUPON_RESEND = GiftishowOperationTargetTypeResolver.OP_COUPON_RESEND;
    private static final String OP_BIZMONEY_BALANCE = GiftishowOperationTargetTypeResolver.OP_BIZMONEY_BALANCE;
    private static final String OP_COUPON_SEND_FAIL_CANCEL =
            GiftishowOperationTargetTypeResolver.OP_COUPON_SEND_FAIL_CANCEL;

    private static final String DEV_YN_PRODUCTION = "N";
    private static final String API_CODE_PRODUCT_LIST = "0101";
    private static final String API_CODE_PRODUCT_DETAIL = "0111";
    private static final String API_CODE_BRAND_LIST = "0102";
    private static final String API_CODE_BRAND_DETAIL = "0112";
    private static final String API_CODE_COUPON_DETAIL = "0201";
    private static final String API_CODE_COUPON_CANCEL = "0202";
    private static final String API_CODE_COUPON_RESEND = "0203";
    private static final String API_CODE_COUPON_SEND = "0204";
    private static final String API_CODE_COUPON_SEND_FAIL_CANCEL = "0205";
    private static final String API_CODE_BIZMONEY_BALANCE = "0301";

    private static final String TAG_TR_ID = "tr_id";
    private static final String TAG_GOODS_CODE = "goods_code";
    private static final String TAG_BRAND_CODE = "brand_code";
    private static final String TAG_USER_ID = "user_id";

    private final GiftishowApiProperties properties;
    private final RestClient giftishowRestClient;
    private final ObjectMapper objectMapper;
    private final ExternalCallLogger externalCallLogger;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private final GiftishowOperationTargetTypeResolver operationTargetTypeResolver;

    public GiftishowApiClient(
            GiftishowApiProperties properties,
            RestClient giftishowRestClient,
            ObjectMapper objectMapper,
            ExternalCallLogger externalCallLogger,
            VendorCommunicationRecorder vendorCommunicationRecorder,
            VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator,
            GiftishowOperationTargetTypeResolver operationTargetTypeResolver
    ) {
        this.properties = properties;
        this.giftishowRestClient = giftishowRestClient;
        this.objectMapper = objectMapper;
        this.externalCallLogger = externalCallLogger;
        this.vendorCommunicationRecorder = vendorCommunicationRecorder;
        this.vendorCommunicationPayloadGenerator = vendorCommunicationPayloadGenerator;
        this.operationTargetTypeResolver = operationTargetTypeResolver;
    }

    /**
     * 0101 - 상품 리스트 조회 (POST)
     */
    public GiftishowApiResponse<GiftishowProductListResponse> getProductList(int start, int size) {
        MultiValueMap<String, String> params = createAuthParams();
        params.add("api_code", API_CODE_PRODUCT_LIST);
        params.add("start", String.valueOf(start));
        params.add("size", String.valueOf(size));

        log.info("기프티쇼 상품 리스트 조회 요청: start={}, size={}", start, size);

        Map<String, String> tags = mutableTags();
        String path = properties.getApi().getProductListPath();

        return recordVendorCall(
                path, OP_PRODUCT_LIST, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_PRODUCT_DETAIL);

        log.info("기프티쇼 상품 상세 조회 요청: goodsCode={}", goodsCode);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_GOODS_CODE, goodsCode);
        String path = properties.getApi().getProductDetailPath();

        return recordVendorCall(
                path, OP_PRODUCT_DETAIL, tags, params,
                () -> executePostWithPathVariable(
                        path,
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
        params.add("api_code", API_CODE_BRAND_LIST);

        log.info("기프티쇼 브랜드 조회 요청");

        Map<String, String> tags = mutableTags();
        String path = properties.getApi().getBrandListPath();

        return recordVendorCall(
                path, OP_BRAND_LIST, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_BRAND_DETAIL);

        log.info("기프티쇼 브랜드 상세 조회 요청: brandCode={}", brandCode);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_BRAND_CODE, brandCode);
        String path = properties.getApi().getBrandDetailPath();

        return recordVendorCall(
                path, OP_BRAND_DETAIL, tags, params,
                () -> executePostWithPathVariable(
                        path,
                        brandCode,
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    /**
     * 0204 - 쿠폰 발송 (POST)
     *
     * <p>규격서 P.26~27(v1.04)에 따라 옵션 파라미터 {@code template_id}(카드 아이디),
     * {@code banner_id}(배너 아이디), {@code order_no}(주문번호)는 값이 존재할 때만 송신한다.
     * {@code template_id}/{@code banner_id}는 properties에서 주입(미설정 시 기본 이미지 발송),
     * {@code order_no}는 호출자가 전달(없으면 송신 생략).</p>
     */
    public GiftishowApiResponse<GiftishowCouponSendResponse> sendCoupon(
            String trId,
            String goodsCode,
            String phoneNo,
            String userId,
            String mmsMsg,
            String mmsTitle,
            String orderNo
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
        params.add("api_code", API_CODE_COUPON_SEND);
        params.add("tr_id", trId);
        params.add("goods_code", goodsCode);
        params.add("phone_no", phoneNo);
        params.add("user_id", userId);
        params.add("gubun", properties.getGubun());
        params.add("callback_no", properties.getCallbackNo());
        params.add("mms_msg", mmsMsg);
        params.add("mms_title", mmsTitle);
        if (FormatValidator.hasValue(properties.getTemplateId())) {
            params.add("template_id", properties.getTemplateId());
        }
        if (FormatValidator.hasValue(properties.getBannerId())) {
            params.add("banner_id", properties.getBannerId());
        }
        if (FormatValidator.hasValue(orderNo)) {
            params.add("order_no", orderNo);
        }

        log.info("기프티쇼 쿠폰 발송 요청: trId={}, goodsCode={}, userId={}", trId, goodsCode, userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        putIfPresent(tags, TAG_GOODS_CODE, goodsCode);
        putIfPresent(tags, TAG_USER_ID, userId);
        String path = properties.getApi().getCouponSendPath();

        return recordVendorCall(
                path, OP_COUPON_SEND, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_COUPON_DETAIL);
        params.add("tr_id", trId);

        log.info("기프티쇼 쿠폰 상세 조회 요청: trId={}", trId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        String path = properties.getApi().getCouponDetailPath();

        return recordVendorCall(
                path, OP_COUPON_DETAIL, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_COUPON_CANCEL);
        params.add("tr_id", trId);
        params.add("user_id", userId);

        log.info("기프티쇼 쿠폰 취소 요청: trId={}, userId={}", trId, userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        putIfPresent(tags, TAG_USER_ID, userId);
        String path = properties.getApi().getCouponCancelPath();

        return recordVendorCall(
                path, OP_COUPON_CANCEL, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_COUPON_RESEND);
        params.add("tr_id", trId);
        params.add("phone_no", phoneNo);

        log.info("기프티쇼 쿠폰 재전송 요청: trId={}", trId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        String path = properties.getApi().getCouponResendPath();

        return recordVendorCall(
                path, OP_COUPON_RESEND, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_BIZMONEY_BALANCE);
        params.add("user_id", userId);

        log.info("기프티쇼 비즈머니 잔액 조회 요청: userId={}", userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_USER_ID, userId);
        String path = properties.getApi().getBizMoneyBalancePath();

        return recordVendorCall(
                path, OP_BIZMONEY_BALANCE, tags, params,
                () -> executePost(
                        path,
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
        params.add("api_code", API_CODE_COUPON_SEND_FAIL_CANCEL);
        params.add("tr_id", trId);
        params.add("user_id", userId);

        log.info("기프티쇼 발송실패 취소 요청: trId={}, userId={}", trId, userId);

        Map<String, String> tags = mutableTags();
        putIfPresent(tags, TAG_TR_ID, trId);
        putIfPresent(tags, TAG_USER_ID, userId);
        String path = properties.getApi().getCouponSendFailCancelPath();

        return recordVendorCall(
                path, OP_COUPON_SEND_FAIL_CANCEL, tags, params,
                () -> executePost(
                        path,
                        params,
                        new TypeReference<>() {
                        }
                )
        );
    }

    private MultiValueMap<String, String> createAuthParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("custom_auth_code", properties.getAuthCode());
        params.add("custom_auth_token", properties.getAuthToken());
        params.add("dev_yn", DEV_YN_PRODUCTION);
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

    private <T> T recordCall(
            String path, String operation, Map<String, String> tags, Supplier<T> call
    ) {
        if (isSensitivePath(path)) {
            return externalCallLogger.recordSensitive(ExternalSystem.GIFTISHOW, operation, tags, call);
        }
        return externalCallLogger.record(ExternalSystem.GIFTISHOW, operation, tags, call);
    }

    /**
     * 외부 호출 전후로 vendor communication history(REQUEST/RESPONSE) 2건을 영속하면서 외부 호출을 위임한다.
     *
     * <p>sensitive path(쿠폰 발송/조회/재전송)의 경우 응답 payload는
     * {@code {"masked":true,"operation":...,"status":...}} 비식별화 토큰만 영속한다 — 핀번호/폰번호 raw 영속 차단.
     * record 실패는 try-catch로 격리하여 쿠폰 발송 실패로 전이되지 않도록 한다.</p>
     */
    private <T extends GiftishowApiResponse<?>> T recordVendorCall(
            String path,
            String operation,
            Map<String, String> tags,
            MultiValueMap<String, String> params,
            Supplier<T> call
    ) {
        RewardVendorCommunicationTargetType targetType = operationTargetTypeResolver.resolve(operation);
        String targetId = resolveTargetId(tags);
        boolean sensitive = isSensitivePath(path);

        recordRequestSafely(targetType, vendorOperationName(operation), targetId, params, sensitive);

        try {
            T response = recordCall(path, operation, tags, call);
            recordResponseSafely(targetType, operation, targetId, response, null, sensitive);
            return response;
        } catch (RuntimeException re) {
            recordResponseSafely(targetType, operation, targetId, null, re, sensitive);
            throw re;
        }
    }

    private void recordRequestSafely(
            RewardVendorCommunicationTargetType targetType,
            String operation,
            String targetId,
            MultiValueMap<String, String> params,
            boolean sensitive
    ) {
        try {
            JsonNode payloadJson = sensitive
                    ? buildMaskedRequestPayloadJson(operation)
                    : buildRequestPayloadJson(operation, params);
            String payload = payloadJson.toString();

            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.REQUEST,
                    RewardVendorCommunicationSenderType.SERVER,
                    targetId,
                    RewardVendorName.GIFTISHOW,
                    payload,
                    payloadJson
            );
        } catch (RuntimeException re) {
            log.warn("Giftishow REQUEST 이력 영속 실패 (격리 — 외부 호출 흐름 영향 없음): operation={}, exception={}",
                    operation, re.getClass().getSimpleName(), re);
        }
    }

    private void recordResponseSafely(
            RewardVendorCommunicationTargetType targetType,
            String operation,
            String targetId,
            GiftishowApiResponse<?> response,
            RuntimeException exception,
            boolean sensitive
    ) {
        try {
            String exceptionName = FormatValidator.hasValue(exception)
                    ? exception.getClass().getSimpleName()
                    : resolveResponseException(response);

            JsonNode payloadJson = sensitive
                    ? buildMaskedResponsePayloadJson(operation, response, exception)
                    : buildResponsePayloadJson(operation, response, exception);
            String payload = payloadJson.toString();

            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.RESPONSE,
                    RewardVendorCommunicationSenderType.VENDOR,
                    targetId,
                    RewardVendorName.GIFTISHOW,
                    payload,
                    payloadJson,
                    exceptionName
            );
        } catch (RuntimeException re) {
            log.warn("Giftishow RESPONSE 이력 영속 실패 (격리 — 외부 호출 흐름 영향 없음): operation={}, exception={}",
                    operation, re.getClass().getSimpleName(), re);
        }
    }

    private JsonNode buildRequestPayloadJson(String operation, MultiValueMap<String, String> params) {
        ObjectNode node = (ObjectNode) vendorCommunicationPayloadGenerator.buildPayloadJson(null);
        node.put("operation", operation);
        ObjectNode paramsNode = node.putObject("params");
        for (Map.Entry<String, java.util.List<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = maskParamValue(key, entry.getValue());
            paramsNode.put(key, value);
        }
        return node;
    }

    private JsonNode buildMaskedRequestPayloadJson(String operation) {
        ObjectNode node = (ObjectNode) vendorCommunicationPayloadGenerator.buildPayloadJson(null);
        node.put("masked", true);
        node.put("operation", operation);
        return node;
    }

    private JsonNode buildResponsePayloadJson(
            String operation, GiftishowApiResponse<?> response, RuntimeException exception
    ) {
        ObjectNode node = (ObjectNode) vendorCommunicationPayloadGenerator.buildPayloadJson(null);
        node.put("operation", operation);
        if (FormatValidator.hasValue(response)) {
            node.put("code", response.code());
            node.put("message", response.message());
            if (FormatValidator.hasValue(response.result())) {
                node.set("result", objectMapper.valueToTree(response.result()));
            }
            return node;
        }
        if (FormatValidator.hasValue(exception)) {
            node.put("error", exception.getClass().getSimpleName());
            node.put("message", exception.getMessage());
        }
        return node;
    }

    private JsonNode buildMaskedResponsePayloadJson(
            String operation, GiftishowApiResponse<?> response, RuntimeException exception
    ) {
        ObjectNode node = (ObjectNode) vendorCommunicationPayloadGenerator.buildPayloadJson(null);
        node.put("masked", true);
        node.put("operation", operation);
        if (FormatValidator.hasValue(response)) {
            node.put("status", response.code());
            return node;
        }
        if (FormatValidator.hasValue(exception)) {
            node.put("status", exception.getClass().getSimpleName());
        }
        return node;
    }

    private String resolveResponseException(GiftishowApiResponse<?> response) {
        if (FormatValidator.hasNoValue(response)) {
            return "NO_RESPONSE";
        }
        if (response.isSuccess()) {
            return null;
        }
        return "CODE_" + response.code();
    }

    private String resolveTargetId(Map<String, String> tags) {
        if (tags.containsKey(TAG_TR_ID)) {
            return tags.get(TAG_TR_ID);
        }
        if (tags.containsKey(TAG_GOODS_CODE)) {
            return tags.get(TAG_GOODS_CODE);
        }
        if (tags.containsKey(TAG_BRAND_CODE)) {
            return tags.get(TAG_BRAND_CODE);
        }
        if (tags.containsKey(TAG_USER_ID)) {
            return tags.get(TAG_USER_ID);
        }
        return null;
    }

    private String vendorOperationName(String operation) {
        return operation;
    }

    private String maskParamValue(String key, java.util.List<String> values) {
        if (FormatValidator.hasNoValue(values) || values.isEmpty()) {
            return null;
        }
        String first = values.getFirst();
        if (isAuthParam(key)) {
            return mask(first);
        }
        if (isSensitiveParam(key)) {
            return mask(first);
        }
        return first;
    }

    private boolean isAuthParam(String key) {
        return "custom_auth_code".equals(key) || "custom_auth_token".equals(key);
    }

    private boolean isSensitiveParam(String key) {
        return "phone_no".equals(key);
    }

    private String mask(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }
        int visible = Math.min(4, value.length());
        int maskedLength = value.length() - visible;
        if (maskedLength <= 0) {
            return value;
        }
        return "*".repeat(maskedLength) + value.substring(value.length() - visible);
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
