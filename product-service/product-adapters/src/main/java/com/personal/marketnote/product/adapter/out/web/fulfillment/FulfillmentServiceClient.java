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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.personal.marketnote.common.utility.ApiConstant.*;

@ServiceAdapter
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

    private final RestClient restClient;
    private final String fulfillmentServiceBaseUrl;
    private final String fulfillmentVendorCustomerCode;
    private final HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;
    private final ServiceCommunicationRecorder serviceCommunicationRecorder;
    private final ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    public FulfillmentServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${fulfillment-service.base-url}") String fulfillmentServiceBaseUrl,
            @Value("${fulfillment-service.fassto.customer-code}") String fulfillmentVendorCustomerCode,
            HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder,
            ServiceCommunicationRecorder serviceCommunicationRecorder,
            ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator
    ) {
        this.restClient = restClientBuilder.build();
        this.fulfillmentServiceBaseUrl = fulfillmentServiceBaseUrl;
        this.fulfillmentVendorCustomerCode = fulfillmentVendorCustomerCode;
        this.hmacServiceAuthHeaderBuilder = hmacServiceAuthHeaderBuilder;
        this.serviceCommunicationRecorder = serviceCommunicationRecorder;
        this.serviceCommunicationPayloadGenerator = serviceCommunicationPayloadGenerator;
    }

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

        List<RegisterFasstoGoodsItemRequest> payload = List.of(RegisterFasstoGoodsItemRequest.from(command));
        sendRegisterRequest(uri, payload, fulfillmentVendorAccessToken, command);
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

        GetFulfillmentVendorGoodsResult result = requestGoodsList(uri, fulfillmentVendorAccessToken);
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

        GetFulfillmentVendorGoodsElementsResult result = requestGoodsElements(uri, fulfillmentVendorAccessToken);
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

        List<UpdateFasstoGoodsItemRequest> payload = List.of(UpdateFasstoGoodsItemRequest.from(command));
        sendUpdateRequest(uri, payload, fulfillmentVendorAccessToken, command);
    }

    private String requestFulfillmentVendorAccessToken() {
        URI uri = UriComponentsBuilder
                .fromUriString(fulfillmentServiceBaseUrl)
                .path("/api/v1/vendors/fassto/auth")
                .build()
                .toUri();

        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<FasstoAuthTokenResponse>> responseEntity =
                        restClient.post()
                                .uri(uri)
                                .headers(headers -> hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath()))
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

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
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to request fassto access token with error: {}", error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private void sendRegisterRequest(
            URI uri,
            List<RegisterFasstoGoodsItemRequest> requestBody,
            String accessToken,
            RegisterFulfillmentVendorGoodsCommand command
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<RegisterFasstoGoodsResponse>> responseEntity =
                        restClient.post()
                                .uri(uri)
                                .headers(headers -> {
                                    hmacServiceAuthHeaderBuilder.applyHeaders(headers, "POST", uri.getPath());
                                    headers.add("accessToken", accessToken);
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(requestBody)
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

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
                        command.customerGoodsCode(),
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        command.customerGoodsCode(),
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
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to register fassto goods: {} with error: {}", command.customerGoodsCode(), error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private void sendUpdateRequest(
            URI uri,
            List<UpdateFasstoGoodsItemRequest> requestBody,
            String accessToken,
            UpdateFulfillmentVendorGoodsCommand command
    ) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<UpdateFasstoGoodsResponse>> responseEntity =
                        restClient.put()
                                .uri(uri)
                                .headers(headers -> {
                                    hmacServiceAuthHeaderBuilder.applyHeaders(headers, "PUT", uri.getPath());
                                    headers.add("accessToken", accessToken);
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(requestBody)
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

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
                        command.customerGoodsCode(),
                        ProductServiceCommunicationType.REQUEST,
                        requestPayload,
                        requestPayloadJson,
                        exception
                );
                recordCommunication(
                        ProductServiceCommunicationTargetType.FULFILLMENT_GOODS,
                        command.customerGoodsCode(),
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
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to update fassto goods: {} with error: {}", command.customerGoodsCode(), error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private GetFulfillmentVendorGoodsResult requestGoodsList(URI uri, String accessToken) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<GetFasstoGoodsResponse>> responseEntity =
                        restClient.get()
                                .uri(uri)
                                .headers(headers -> {
                                    hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath());
                                    headers.add("accessToken", accessToken);
                                })
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

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
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }

                sleepMillis = sleepMillis * INTER_SERVER_DEFAULT_EXPONENTIAL_BACKOFF_VALUE;
            }
        }

        log.error("Failed to get fassto goods list: {} with error: {}", fulfillmentVendorCustomerCode, error.getMessage(), error);
        throw new FulfillmentServiceRequestFailedException(new IOException());
    }

    private GetFulfillmentVendorGoodsElementsResult requestGoodsElements(URI uri, String accessToken) {
        long sleepMillis = INTER_SERVER_DEFAULT_RETRIAL_PENDING_MILLI_SECOND;
        Exception error = new Exception();

        for (int i = 0; i < INTER_SERVER_MAX_REQUEST_COUNT; i++) {
            int attempt = i + 1;
            try {
                ResponseEntity<BaseResponse<GetFasstoGoodsElementsResponse>> responseEntity =
                        restClient.get()
                                .uri(uri)
                                .headers(headers -> {
                                    hmacServiceAuthHeaderBuilder.applyHeaders(headers, "GET", uri.getPath());
                                    headers.add("accessToken", accessToken);
                                })
                                .retrieve()
                                .toEntity(new ParameterizedTypeReference<>() {
                                });

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
                    long jitteredSleepMillis = ThreadLocalRandom.current()
                            .nextLong(Math.max(1L, sleepMillis) + 1);
                    Thread.sleep(jitteredSleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }

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

        return FulfillmentVendorGoodsElementInfoResult.builder()
                .goodsCode(item.godCd())
                .customerGoodsCode(item.cstGodCd())
                .goodsName(item.godNm())
                .enabled(item.useYn())
                .elementList(elementItems)
                .build();
    }

    private FulfillmentVendorGoodsElementItemResult mapFulfillmentGoodsElementItem(
            FasstoGoodsElementItemResponse item
    ) {
        return FulfillmentVendorGoodsElementItemResult.builder()
                .goodsCode(item.godCd())
                .customerGoodsCode(item.cstGodCd())
                .goodsBarcode(item.godBarcd())
                .goodsName(item.godNm())
                .goodsType(item.godType())
                .goodsTypeName(item.godTypeNm())
                .quantity(item.qty())
                .build();
    }

    private GetFulfillmentVendorGoodsResult mapFulfillmentGoodsResult(GetFasstoGoodsResponse response) {
        List<FulfillmentVendorGoodsInfoResult> goods = response.goods().stream()
                .map(this::mapFulfillmentGoodsItem)
                .toList();
        return GetFulfillmentVendorGoodsResult.of(response.dataCount(), goods);
    }

    private FulfillmentVendorGoodsInfoResult mapFulfillmentGoodsItem(FasstoGoodsItemResponse item) {
        return FulfillmentVendorGoodsInfoResult.builder()
                .goodsCode(item.godCd())
                .goodsType(item.godType())
                .goodsName(item.godNm())
                .goodsTypeName(item.godTypeNm())
                .invoiceGoodsNameEnabled(item.invGodNmUseYn())
                .customerGoodsCode(item.cstGodCd())
                .goodsOptionCode1(item.godOptCd1())
                .goodsOptionCode2(item.godOptCd2())
                .customerCode(item.cstCd())
                .customerName(item.cstNm())
                .supplierCode(item.supCd())
                .supplierName(item.supNm())
                .categoryCode(item.cateCd())
                .categoryName(item.cateNm())
                .seasonCode(item.seasonCd())
                .genderCode(item.genderCd())
                .unitPrice(item.godPr())
                .supplyPrice(item.inPr())
                .salePrice(item.salPr())
                .handlingTemperature(item.dealTemp())
                .pickingFacility(item.pickFac())
                .giftDivision(item.giftDiv())
                .giftDivisionName(item.giftDivNm())
                .goodsWidth(item.godWidth())
                .goodsLength(item.godLength())
                .goodsHeight(item.godHeight())
                .manufactureYear(item.makeYr())
                .goodsBulk(item.godBulk())
                .goodsWeight(item.godWeight())
                .goodsSideSum(item.godSideSum())
                .goodsVolume(item.godVolume())
                .goodsBarcode(item.godBarcd())
                .boxWidth(item.boxWidth())
                .boxLength(item.boxLength())
                .boxHeight(item.boxHeight())
                .boxBulk(item.boxBulk())
                .boxWeight(item.boxWeight())
                .innerBoxBarcode(item.inBoxBarcd())
                .innerBoxLength(item.inBoxLength())
                .innerBoxHeight(item.inBoxHeight())
                .innerBoxBulk(item.inBoxBulk())
                .innerBoxWidth(item.inBoxWidth())
                .innerBoxWeight(item.inBoxWeight())
                .innerBoxSideSum(item.inBoxSideSum())
                .boxInnerCount(item.boxInCnt())
                .innerBoxInnerCount(item.inBoxInCnt())
                .palletInnerCount(item.pltInCnt())
                .origin(item.origin())
                .expirationDateManagementEnabled(item.distTermMgtYn())
                .shelfLifeDays(item.useTermDay())
                .outboundAvailableDays(item.outCanDay())
                .inboundAvailableDays(item.inCanDay())
                .outboundBoxType(item.boxDiv())
                .cushioningEnabled(item.bufGodYn())
                .loadingDirection(item.loadingDirection())
                .firstInboundDate(item.firstInDt())
                .enabled(item.useYn())
                .feeApplied(item.feeYn())
                .saleUnitQuantity(item.saleUnitQty())
                .oneDayDeliveryEnabled(item.cstOneDayDeliveryYn())
                .safetyStock(item.safetyStock())
                .build();
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
