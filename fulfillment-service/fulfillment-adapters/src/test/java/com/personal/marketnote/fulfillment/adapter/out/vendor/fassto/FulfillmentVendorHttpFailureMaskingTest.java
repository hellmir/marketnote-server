package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client.FulfillmentAuthClient;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client.FulfillmentDeliveryClient;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client.FulfillmentReturnDeliveryClient;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client.FulfillmentSettlementClient;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client.FulfillmentStockClient;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentAuthRequestFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveriesFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentReturnGodDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentSettlementDailyCostsFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentStocksFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.utility.NoOpSleeper;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@DisplayName("Fulfillment Fassto vendor 클라이언트 sensitive path raw body 마스킹 테스트")
class FulfillmentVendorHttpFailureMaskingTest {

    private static final String FORBIDDEN_TOKEN = "01098765432";
    private static final String SENSITIVE_BODY =
            "{\"data\":{\"phoneNo\":\"" + FORBIDDEN_TOKEN + "\",\"orderNo\":\"ORD-LEAK\"}}";
    private static final String NON_SENSITIVE_ERROR_BODY =
            "{\"header\":{\"resultCode\":\"500\",\"resultMessage\":\"server error\"}}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FulfillmentAuthProperties properties;
    private VendorCommunicationRecorder vendorCommunicationRecorder;
    private VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;
    private VendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    private ExternalCallLogger externalCallLogger;

    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.RequestBodySpec requestBodySpec;
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    private RestClient.ResponseSpec responseSpec;

    private Logger authLogger;
    private Logger deliveryLogger;
    private Logger stockLogger;
    private Logger returnDeliveryLogger;
    private Logger settlementLogger;
    private ListAppender<ILoggingEvent> authAppender;
    private ListAppender<ILoggingEvent> deliveryAppender;
    private ListAppender<ILoggingEvent> stockAppender;
    private ListAppender<ILoggingEvent> returnDeliveryAppender;
    private ListAppender<ILoggingEvent> settlementAppender;

    @BeforeEach
    void setUp() {
        properties = new FulfillmentAuthProperties();
        properties.setBaseUrl("https://api.fassto.test");
        properties.setApiCd("API_CD");
        properties.setApiKey("API_KEY");
        properties.setCustomerCode("CUST");
        properties.setSensitivePaths(Set.of(
                properties.getConnectPath(),
                properties.getDeliveryListPath(),
                properties.getReturnGodDetailPath(),
                properties.getSettlementDailyCostPath()
                // stockListPath: non-sensitive (의도적으로 제외)
        ));

        vendorCommunicationRecorder = mock(VendorCommunicationRecorder.class);
        vendorCommunicationPayloadGenerator = mock(VendorCommunicationPayloadGenerator.class);
        vendorCommunicationFailureHandler = mock(VendorCommunicationFailureHandler.class);
        externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());

        lenient().when(vendorCommunicationPayloadGenerator.buildPayloadJson(any()))
                .thenReturn(objectMapper.createObjectNode());

        restClient = mock(RestClient.class);
        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(RestClient.RequestBodySpec.class);
        requestHeadersUriSpec = (RestClient.RequestHeadersUriSpec) mock(RestClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = (RestClient.RequestHeadersSpec) mock(RestClient.RequestHeadersSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        authLogger = (Logger) LoggerFactory.getLogger(FulfillmentAuthClient.class);
        deliveryLogger = (Logger) LoggerFactory.getLogger(FulfillmentDeliveryClient.class);
        stockLogger = (Logger) LoggerFactory.getLogger(FulfillmentStockClient.class);
        returnDeliveryLogger = (Logger) LoggerFactory.getLogger(FulfillmentReturnDeliveryClient.class);
        settlementLogger = (Logger) LoggerFactory.getLogger(FulfillmentSettlementClient.class);
        authAppender = attachAppender(authLogger);
        deliveryAppender = attachAppender(deliveryLogger);
        stockAppender = attachAppender(stockLogger);
        returnDeliveryAppender = attachAppender(returnDeliveryLogger);
        settlementAppender = attachAppender(settlementLogger);
    }

    @AfterEach
    void tearDown() {
        detachAppender(authLogger, authAppender);
        detachAppender(deliveryLogger, deliveryAppender);
        detachAppender(stockLogger, stockAppender);
        detachAppender(returnDeliveryLogger, returnDeliveryAppender);
        detachAppender(settlementLogger, settlementAppender);
    }

    @Test
    @DisplayName("Auth(sensitive) 401 응답 시 raw body가 예외 메시지/cause/log에 노출되지 않는다")
    void shouldMaskRawBodyWhenAuthSensitivePathReturns4xx() {
        // given
        RestClientResponseException unauthorized = HttpClientErrorException.create(
                UNAUTHORIZED, "Unauthorized", null,
                SENSITIVE_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        stubPostThrows(unauthorized);

        FulfillmentAuthClient client = newAuthClient();

        // when & then
        assertThatThrownBy(client::requestAccessToken)
                .isInstanceOf(FulfillmentAuthRequestFailedException.class)
                .hasMessageNotContaining(FORBIDDEN_TOKEN)
                .satisfies(thrown -> assertNoRawBodyInChain(thrown, FORBIDDEN_TOKEN));

        assertNoRawBodyInLogs(authAppender, FORBIDDEN_TOKEN);
        assertSensitiveLogShape(authAppender);
    }

    @Test
    @DisplayName("Delivery list(sensitive) 500 응답 시 raw body가 노출되지 않는다")
    void shouldMaskRawBodyWhenDeliverySensitivePathReturns5xx() {
        // given
        RestClientResponseException serverError = HttpServerErrorException.create(
                INTERNAL_SERVER_ERROR, "Internal Server Error", null,
                SENSITIVE_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        stubGetThrows(serverError);

        FulfillmentDeliveryClient client = newDeliveryClient();
        GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.builder()
                .customerCode("CUST")
                .accessToken("AT")
                .startDate("20260101")
                .endDate("20260131")
                .status("D03")
                .releaseType("Y")
                .build();

        // when & then
        assertThatThrownBy(() -> client.getDeliveries(command))
                .isInstanceOf(GetFulfillmentDeliveriesFailedException.class)
                .satisfies(thrown -> assertNoRawBodyInChain(thrown, FORBIDDEN_TOKEN));

        assertNoRawBodyInLogs(deliveryAppender, FORBIDDEN_TOKEN);
        assertSensitiveLogShape(deliveryAppender);
    }

    @Test
    @DisplayName("Stock list(non-sensitive) 500 응답 시 cause 보존, throwable 첨부 로그 유지")
    void shouldPreserveCauseWhenStockNonSensitivePathHttpFailure() {
        // given
        RestClientResponseException serverError = HttpServerErrorException.create(
                INTERNAL_SERVER_ERROR, "Internal Server Error", null,
                NON_SENSITIVE_ERROR_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        stubGetThrows(serverError);

        FulfillmentStockClient client = newStockClient();
        GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST", "AT");

        // when & then
        assertThatThrownBy(() -> client.getStocks(command))
                .isInstanceOf(GetFulfillmentStocksFailedException.class)
                .hasCauseInstanceOf(java.io.IOException.class);

        assertHasThrowableAttached(stockAppender);
    }

    @Test
    @DisplayName("Settlement(sensitive) 401 응답 시 raw body가 노출되지 않는다")
    void shouldMaskRawBodyWhenSettlementSensitivePathReturns4xx() {
        // given
        RestClientResponseException unauthorized = HttpClientErrorException.create(
                UNAUTHORIZED, "Unauthorized", null,
                SENSITIVE_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        stubGetThrows(unauthorized);

        FulfillmentSettlementClient client = newSettlementClient();
        GetFulfillmentSettlementDailyCostsCommand command =
                GetFulfillmentSettlementDailyCostsCommand.of("202601", "WH01", "CUST", "AT");

        // when & then
        assertThatThrownBy(() -> client.getDailyCosts(command))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class)
                .satisfies(thrown -> assertNoRawBodyInChain(thrown, FORBIDDEN_TOKEN));

        assertNoRawBodyInLogs(settlementAppender, FORBIDDEN_TOKEN);
        assertSensitiveLogShape(settlementAppender);
    }

    @Test
    @DisplayName("sensitive path 호출 실패 시 VendorCommunicationFailureHandler 로그에 raw body가 누출되지 않는다")
    void shouldNotLeakRawBodyThroughVendorFailureHandlerWhenSensitivePath() {
        // 핸들러 logger에 appender 부착
        Logger handlerLogger = (Logger) LoggerFactory.getLogger(VendorCommunicationFailureHandler.class);
        boolean prevAdditive = handlerLogger.isAdditive();
        Level prevLevel = handlerLogger.getLevel();
        handlerLogger.setAdditive(false);
        handlerLogger.setLevel(Level.ERROR);
        ListAppender<ILoggingEvent> handlerAppender = new ListAppender<>();
        handlerAppender.start();
        handlerLogger.addAppender(handlerAppender);

        try {
            // given - 실제 VendorCommunicationFailureHandler 인스턴스로 별도 client 빌드
            VendorCommunicationRecorder realRecorder = mock(VendorCommunicationRecorder.class);
            VendorCommunicationPayloadGenerator realGen = mock(VendorCommunicationPayloadGenerator.class);
            lenient().when(realGen.buildPayloadJson(any())).thenReturn(objectMapper.createObjectNode());
            VendorCommunicationFailureHandler realHandler = new VendorCommunicationFailureHandler(realRecorder, realGen);

            RestClientResponseException unauthorized = HttpClientErrorException.create(
                    UNAUTHORIZED, "Unauthorized", null,
                    SENSITIVE_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
            );
            stubPostThrows(unauthorized);

            VendorCommunicationPayloadGenerator clientGen = mock(VendorCommunicationPayloadGenerator.class);
            lenient().when(clientGen.buildPayloadJson(any())).thenReturn(objectMapper.createObjectNode());
            RestClient.Builder builder = mock(RestClient.Builder.class);
            when(builder.build()).thenReturn(restClient);
            FulfillmentAuthClient client = new FulfillmentAuthClient(
                    builder, objectMapper, properties,
                    mock(VendorCommunicationRecorder.class), clientGen,
                    realHandler, externalCallLogger, new NoOpSleeper()
            );

            // when
            assertThatThrownBy(client::requestAccessToken)
                    .isInstanceOf(FulfillmentAuthRequestFailedException.class);

            // then - 핸들러 로그에 raw body 토큰 미포함 검증
            assertThat(handlerAppender.list)
                    .filteredOn(event -> event.getLevel() == Level.ERROR)
                    .allSatisfy(event -> {
                        assertThat(event.getFormattedMessage()).doesNotContain(FORBIDDEN_TOKEN);
                        assertNoRawBodyInThrowableProxyChain(event.getThrowableProxy(), FORBIDDEN_TOKEN);
                    });
        } finally {
            handlerLogger.detachAppender(handlerAppender);
            handlerAppender.stop();
            handlerLogger.setAdditive(prevAdditive);
            handlerLogger.setLevel(prevLevel);
        }
    }

    @Test
    @DisplayName("ReturnGodDetail(sensitive) 500 응답 시 raw body가 노출되지 않는다")
    void shouldMaskRawBodyWhenReturnDeliverySensitivePathReturns5xx() {
        // given
        RestClientResponseException serverError = HttpServerErrorException.create(
                INTERNAL_SERVER_ERROR, "Internal Server Error", null,
                SENSITIVE_BODY.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        stubGetThrows(serverError);

        FulfillmentReturnDeliveryClient client = newReturnDeliveryClient();
        GetFulfillmentReturnGodDetailCommand command =
                GetFulfillmentReturnGodDetailCommand.builder()
                        .customerCode("CUST")
                        .accessToken("AT")
                        .startDate("20260101")
                        .endDate("20260131")
                        .build();

        // when & then
        assertThatThrownBy(() -> client.getReturnGodDetail(command))
                .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class)
                .satisfies(thrown -> assertNoRawBodyInChain(thrown, FORBIDDEN_TOKEN));

        assertNoRawBodyInLogs(returnDeliveryAppender, FORBIDDEN_TOKEN);
        assertSensitiveLogShape(returnDeliveryAppender);
    }

    private FulfillmentAuthClient newAuthClient() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        return new FulfillmentAuthClient(
                builder, objectMapper, properties,
                vendorCommunicationRecorder, vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler, externalCallLogger, new NoOpSleeper()
        );
    }

    private FulfillmentDeliveryClient newDeliveryClient() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        return new FulfillmentDeliveryClient(
                builder, objectMapper, properties,
                vendorCommunicationRecorder, vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler, externalCallLogger, new NoOpSleeper()
        );
    }

    private FulfillmentStockClient newStockClient() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        return new FulfillmentStockClient(
                builder, objectMapper, properties,
                vendorCommunicationRecorder, vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler, externalCallLogger, new NoOpSleeper()
        );
    }

    private FulfillmentReturnDeliveryClient newReturnDeliveryClient() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        return new FulfillmentReturnDeliveryClient(
                builder, objectMapper, properties,
                vendorCommunicationRecorder, vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler, externalCallLogger, new NoOpSleeper()
        );
    }

    private FulfillmentSettlementClient newSettlementClient() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);
        return new FulfillmentSettlementClient(
                builder, objectMapper, properties,
                vendorCommunicationRecorder, vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler, externalCallLogger, new NoOpSleeper()
        );
    }

    @SuppressWarnings("unchecked")
    private void stubPostThrows(Exception exception) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubGetThrows(Exception exception) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    private ListAppender<ILoggingEvent> attachAppender(Logger logger) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachAppender(Logger logger, ListAppender<ILoggingEvent> appender) {
        logger.detachAppender(appender);
        appender.stop();
    }

    private void assertNoRawBodyInLogs(ListAppender<ILoggingEvent> appender, String forbidden) {
        assertThat(appender.list).allSatisfy(event -> {
            assertThat(event.getFormattedMessage()).doesNotContain(forbidden);
            assertNoRawBodyInThrowableProxyChain(event.getThrowableProxy(), forbidden);
        });
    }

    private void assertSensitiveLogShape(ListAppender<ILoggingEvent> appender) {
        // sensitive 분기에서는 throwable이 첨부되지 않아야 한다 (메시지/스택 로깅 생략)
        assertThat(appender.list)
                .filteredOn(event -> event.getLevel() == Level.WARN || event.getLevel() == Level.ERROR)
                .allSatisfy(event -> assertThat(event.getThrowableProxy()).isNull());
    }

    private void assertHasThrowableAttached(ListAppender<ILoggingEvent> appender) {
        assertThat(appender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .anySatisfy(event -> assertThat(event.getThrowableProxy()).isNotNull());
    }

    private void assertNoRawBodyInChain(Throwable thrown, String forbidden) {
        Throwable current = thrown;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                assertThat(message).doesNotContain(forbidden);
            }
            for (StackTraceElement element : current.getStackTrace()) {
                assertThat(element.toString()).doesNotContain(forbidden);
            }
            current = current.getCause();
        }
    }

    private void assertNoRawBodyInThrowableProxyChain(IThrowableProxy proxy, String forbidden) {
        IThrowableProxy current = proxy;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                assertThat(message).doesNotContain(forbidden);
            }
            StackTraceElementProxy[] stack = current.getStackTraceElementProxyArray();
            if (stack != null) {
                for (StackTraceElementProxy element : stack) {
                    assertThat(element.getSTEAsString()).doesNotContain(forbidden);
                }
            }
            current = current.getCause();
        }
    }
}
