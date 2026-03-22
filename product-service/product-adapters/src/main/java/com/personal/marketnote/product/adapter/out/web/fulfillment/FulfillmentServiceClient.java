package com.personal.marketnote.product.adapter.out.web.fulfillment;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.web.fulfillment.request.RegisterFasstoGoodsItemRequest;
import com.personal.marketnote.product.adapter.out.web.fulfillment.request.UpdateFasstoGoodsItemRequest;
import com.personal.marketnote.product.adapter.out.web.fulfillment.response.*;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.in.result.fulfillment.*;
import com.personal.marketnote.product.port.out.fulfillment.*;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
@RequiredArgsConstructor
@Slf4j
public class FulfillmentServiceClient implements
        RegisterFulfillmentVendorGoodsPort,
        GetFulfillmentVendorGoodsPort,
        GetFulfillmentVendorGoodsElementsPort,
        UpdateFulfillmentVendorGoodsPort {
    private static final ProductServiceCommunicationSenderType REQUEST_SENDER =
            ProductServiceCommunicationSenderType.PRODUCT;
    private static final ProductServiceCommunicationSenderType RESPONSE_SENDER =
            ProductServiceCommunicationSenderType.FULFILLMENT;

    @Value("${fulfillment-service.base-url}")
    private String fulfillmentServiceBaseUrl;

    @Value("${fulfillment-service.fassto.customer-code}")
    private String fulfillmentVendorCustomerCode;

    private final RestTemplate restTemplate;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @Override
    public void registerFulfillmentVendorGoods(RegisterFulfillmentVendorGoodsCommand command) {
        String fulfillmentVendorAccessToken = requestFulfillmentVendorAccessToken();
        if (FormatValidator.hasNoValue(fulfillmentVendorCustomerCode) || FormatValidator.hasNoValue(fulfillmentVendorAccessToken)) {
            throw new FulfillmentServiceRequestFailedException(new IOException());
        }

        URI uri = UriComponentsBuilder
                .fromUriString(fulfillmentServiceBaseUrl)
                .path("/api/v1/vendors/fassto/goods/{customerCode}")
                .buildAndExpand(fulfillmentVendorCustomerCode)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath());
        headers.add("accessToken", fulfillmentVendorAccessToken);

        List<RegisterFasstoGoodsItemRequest> payload = List.of(RegisterFasstoGoodsItemRequest.from(command));
        HttpEntity<List<RegisterFasstoGoodsItemRequest>> httpEntity = new HttpEntity<>(payload, headers);

        sendRequest(uri, httpEntity, command);
    }

    @Override
    public GetFulfillmentVendorGoodsResult getFulfillmentVendorGoods(String godNm) {
        String fulfillmentVendorAccessToken = requestFulfillmentVendorAccessToken();
        if (FormatValidator.hasNoValue(fulfillmentVendorCustomerCode)
                || FormatValidator.hasNoValue(fulfillmentVendorAccessToken)
                || FormatValidator.hasNoValue(godNm)) {
            throw new FulfillmentServiceRequestFailedException(new IOException());
        }

        URI uri = UriComponentsBuilder
                .fromUriString(fulfillmentServiceBaseUrl)
                .path("/api/v1/vendors/fassto/goods/detail/{customerCode}")
                .queryParam("godNm", godNm)
                .buildAndExpand(fulfillmentVendorCustomerCode)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath());
        headers.add("accessToken", fulfillmentVendorAccessToken);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        GetFulfillmentVendorGoodsResult result = requestGoodsList(uri, httpEntity);
        if (FormatValidator.hasNoValue(result)) {
            throw new FulfillmentServiceRequestFailedException(new IOException());
        }

        return result;
    }

    @Override
    public GetFulfillmentVendorGoodsElementsResult getFulfillmentVendorGoodsElements() {
        String fulfillmentVendorAccessToken = requestFulfillmentVendorAccessToken();
        if (FormatValidator.hasNoValue(fulfillmentVendorCustomerCode) || FormatValidator.hasNoValue(fulfillmentVendorAccessToken)) {
            throw new FulfillmentServiceRequestFailedException(new IOException());
        }

        URI uri = UriComponentsBuilder
                .fromUriString(fulfillmentServiceBaseUrl)
                .path("/api/v1/vendors/fassto/goods/element/{customerCode}")
                .buildAndExpand(fulfillmentVendorCustomerCode)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath());
        headers.add("accessToken", fulfillmentVendorAccessToken);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        GetFulfillmentVendorGoodsElementsResult result = requestGoodsElements(uri, httpEntity);
        if (FormatValidator.hasNoValue(result)) {
            throw new FulfillmentServiceRequestFailedException(new IOException());
        }

        return result;
    }

    @Override
    public void updateFulfillmentVendorGoods(UpdateFulfillmentVendorGoodsCommand command) {
        String fulfillmentVendorAccessToken = requestFulfillmentVendorAccessToken();
        if (FormatValidator.hasNoValue(fulfillmentVendorCustomerCode) || FormatValidator.hasNoValue(fulfillmentVendorAccessToken)) {
            throw new FulfillmentServiceRequestFailedException(new IOException());
        }

        URI uri = UriComponentsBuilder
                .fromUriString(fulfillmentServiceBaseUrl)
                .path("/api/v1/vendors/fassto/goods/{customerCode}")
                .buildAndExpand(fulfillmentVendorCustomerCode)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "PUT", uri.getPath());
        headers.add("accessToken", fulfillmentVendorAccessToken);

        List<UpdateFasstoGoodsItemRequest> payload = List.of(UpdateFasstoGoodsItemRequest.from(command));
        HttpEntity<List<UpdateFasstoGoodsItemRequest>> httpEntity = new HttpEntity<>(payload, headers);

        sendUpdateRequest(uri, httpEntity, command);
    }

    private String requestFulfillmentVendorAccessToken() {
        URI uri = UriComponentsBuilder
                .fromUriString(fulfillmentServiceBaseUrl)
                .path("/api/v1/vendors/fassto/auth")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath());
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<FasstoAuthTokenResponse>> responseEntity =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.POST,
                                httpEntity,
                                new ParameterizedTypeReference<BaseResponse<FasstoAuthTokenResponse>>() {
                                }
                        );

                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                BaseResponse<FasstoAuthTokenResponse> response = responseEntity.getBody();
                if (FormatValidator.hasNoValue(response)
                        || FormatValidator.hasNoValue(response.getContent())
                        || FormatValidator.hasNoValue(response.getContent().tokenInfo())) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                String accessToken = response.getContent().tokenInfo().accessToken();
                if (FormatValidator.hasNoValue(accessToken)) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                return accessToken;
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.POST,
                        uri,
                        null,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_AUTH,
                        fulfillmentVendorCustomerCode,
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_AUTH,
                        fulfillmentVendorCustomerCode,
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to request fassto access token with error: {}", error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private void sendRequest(
            URI uri,
            HttpEntity<List<RegisterFasstoGoodsItemRequest>> httpEntity,
            RegisterFulfillmentVendorGoodsCommand command
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        List<RegisterFasstoGoodsItemRequest> requestBody = httpEntity.getBody();
        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<RegisterFasstoGoodsResponse>> responseEntity =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.POST,
                                httpEntity,
                                new ParameterizedTypeReference<BaseResponse<RegisterFasstoGoodsResponse>>() {
                                }
                        );

                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                BaseResponse<RegisterFasstoGoodsResponse> response = responseEntity.getBody();
                if (FormatValidator.hasNoValue(response) || FormatValidator.hasNoValue(response.getContent())) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                RegisterFasstoGoodsResponse content = response.getContent();
                if (!content.isSuccess()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                return;
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.POST,
                        uri,
                        requestBody,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        command.cstGodCd(),
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        command.cstGodCd(),
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to register fassto goods: {} with error: {}", command.cstGodCd(), error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private void sendUpdateRequest(
            URI uri,
            HttpEntity<List<UpdateFasstoGoodsItemRequest>> httpEntity,
            UpdateFulfillmentVendorGoodsCommand command
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        List<UpdateFasstoGoodsItemRequest> requestBody = httpEntity.getBody();
        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<UpdateFasstoGoodsResponse>> responseEntity =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.PUT,
                                httpEntity,
                                new ParameterizedTypeReference<BaseResponse<UpdateFasstoGoodsResponse>>() {
                                }
                        );

                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                BaseResponse<UpdateFasstoGoodsResponse> response = responseEntity.getBody();
                if (FormatValidator.hasNoValue(response) || FormatValidator.hasNoValue(response.getContent())) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                UpdateFasstoGoodsResponse content = response.getContent();
                if (!content.isSuccess()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                return;
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.PUT,
                        uri,
                        requestBody,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        command.cstGodCd(),
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        command.cstGodCd(),
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to update fassto goods: {} with error: {}", command.cstGodCd(), error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private GetFulfillmentVendorGoodsResult requestGoodsList(
            URI uri,
            HttpEntity<Void> httpEntity
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<GetFasstoGoodsResponse>> responseEntity =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                httpEntity,
                                new ParameterizedTypeReference<BaseResponse<GetFasstoGoodsResponse>>() {
                                }
                        );

                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                BaseResponse<GetFasstoGoodsResponse> response = responseEntity.getBody();
                if (FormatValidator.hasNoValue(response) || FormatValidator.hasNoValue(response.getContent())) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                GetFasstoGoodsResponse content = response.getContent();
                if (!content.isSuccess()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                return mapFulfillmentGoodsResult(content);
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.GET,
                        uri,
                        null,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        fulfillmentVendorCustomerCode,
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        fulfillmentVendorCustomerCode,
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to get fassto goods list: {} with error: {}", fulfillmentVendorCustomerCode, error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private GetFulfillmentVendorGoodsElementsResult requestGoodsElements(
            URI uri,
            HttpEntity<Void> httpEntity
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<GetFasstoGoodsElementsResponse>> responseEntity =
                        restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                httpEntity,
                                new ParameterizedTypeReference<BaseResponse<GetFasstoGoodsElementsResponse>>() {
                                }
                        );

                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                BaseResponse<GetFasstoGoodsElementsResponse> response = responseEntity.getBody();
                if (FormatValidator.hasNoValue(response) || FormatValidator.hasNoValue(response.getContent())) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                GetFasstoGoodsElementsResponse content = response.getContent();
                if (!content.isSuccess()) {
                    throw new FulfillmentServiceRequestFailedException(new IOException());
                }

                return mapFulfillmentGoodsElementsResult(content);
            } catch (Exception e) {
                String exception = e.getClass().getSimpleName();
                JsonNode requestPayloadJson = serviceCommunicationPayloadGenerator.buildRequestPayloadJson(
                        HttpMethod.GET,
                        uri,
                        null,
                        attempt
                );
                String requestPayload = requestPayloadJson.toString();
                JsonNode responsePayloadJson = serviceCommunicationPayloadGenerator.buildErrorPayloadJson(
                        exception,
                        e.getMessage(),
                        attempt
                );
                String responsePayload = responsePayloadJson.toString();
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS_ELEMENT,
                        fulfillmentVendorCustomerCode,
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS_ELEMENT,
                        fulfillmentVendorCustomerCode,
                        ProductServiceCommunicationType.RESPONSE,
                        responsePayload,
                        responsePayloadJson,
                        exception
                );
                log.warn(e.getMessage(), e);
                if (i == INTER_SERVER_MAX_REQUEST_COUNT - 1) {
                    error = e;
                }

                try {
                    // 대상 서비스 장애 시 요청 트래픽 폭주를 방지하기 위해 jitter 설정
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                // exponential backoff 적용
                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to get fassto goods elements: {} with error: {}", fulfillmentVendorCustomerCode, error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private GetFulfillmentVendorGoodsElementsResult mapFulfillmentGoodsElementsResult(
            GetFasstoGoodsElementsResponse response
    ) {
        List<FulfillmentVendorGoodsElementInfoResult> elements = response.elements().stream()
                .map(this::mapFulfillmentGoodsElementInfo)
                .toList();
        return GetFulfillmentVendorGoodsElementsResult.of(response.dataCount(), elements);
    }

    private FulfillmentVendorGoodsElementInfoResult mapFulfillmentGoodsElementInfo(FasstoGoodsElementResponse item) {
        List<FulfillmentVendorGoodsElementItemResult> elementItems = FormatValidator.hasValue(item.elementList())
                ? item.elementList().stream()
                .map(this::mapFulfillmentGoodsElementItem)
                .toList()
                : List.of();

        return FulfillmentVendorGoodsElementInfoResult.of(
                item.godCd(),
                item.cstGodCd(),
                item.godNm(),
                item.useYn(),
                elementItems
        );
    }

    private FulfillmentVendorGoodsElementItemResult mapFulfillmentGoodsElementItem(
            FasstoGoodsElementItemResponse item
    ) {
        return FulfillmentVendorGoodsElementItemResult.of(
                item.godCd(),
                item.cstGodCd(),
                item.godBarcd(),
                item.godNm(),
                item.godType(),
                item.godTypeNm(),
                item.qty()
        );
    }

    private GetFulfillmentVendorGoodsResult mapFulfillmentGoodsResult(GetFasstoGoodsResponse response) {
        List<FulfillmentVendorGoodsInfoResult> goods = response.goods().stream()
                .map(this::mapFulfillmentGoodsItem)
                .toList();
        return GetFulfillmentVendorGoodsResult.of(response.dataCount(), goods);
    }

    private FulfillmentVendorGoodsInfoResult mapFulfillmentGoodsItem(FasstoGoodsItemResponse item) {
        return FulfillmentVendorGoodsInfoResult.of(
                item.godCd(),
                item.godType(),
                item.godNm(),
                item.godTypeNm(),
                item.invGodNmUseYn(),
                item.cstGodCd(),
                item.godOptCd1(),
                item.godOptCd2(),
                item.cstCd(),
                item.cstNm(),
                item.supCd(),
                item.supNm(),
                item.cateCd(),
                item.cateNm(),
                item.seasonCd(),
                item.genderCd(),
                item.godPr(),
                item.inPr(),
                item.salPr(),
                item.dealTemp(),
                item.pickFac(),
                item.giftDiv(),
                item.giftDivNm(),
                item.godWidth(),
                item.godLength(),
                item.godHeight(),
                item.makeYr(),
                item.godBulk(),
                item.godWeight(),
                item.godSideSum(),
                item.godVolume(),
                item.godBarcd(),
                item.boxWidth(),
                item.boxLength(),
                item.boxHeight(),
                item.boxBulk(),
                item.boxWeight(),
                item.inBoxBarcd(),
                item.inBoxLength(),
                item.inBoxHeight(),
                item.inBoxBulk(),
                item.inBoxWidth(),
                item.inBoxWeight(),
                item.inBoxSideSum(),
                item.boxInCnt(),
                item.inBoxInCnt(),
                item.pltInCnt(),
                item.origin(),
                item.distTermMgtYn(),
                item.useTermDay(),
                item.outCanDay(),
                item.inCanDay(),
                item.boxDiv(),
                item.bufGodYn(),
                item.loadingDirection(),
                item.firstInDt(),
                item.useYn(),
                item.feeYn(),
                item.saleUnitQty(),
                item.cstOneDayDeliveryYn(),
                item.safetyStock()
        );
    }

    private void recordCommunication(
            ProductServiceCommunicationTargetType targetType,
            String targetId,
            ProductServiceCommunicationType communicationType,
            String payload,
            JsonNode payloadJson,
            String exception
    ) {
        if (FormatValidator.hasNoValue(exception)) {
            return;
        }

        ProductServiceCommunicationSenderType sender =
                communicationType == ProductServiceCommunicationType.REQUEST ? REQUEST_SENDER : RESPONSE_SENDER;
        serviceCommunicationRecorder.record(
                targetType,
                communicationType,
                sender,
                targetId,
                payload,
                payloadJson,
                exception
        );
    }
}
