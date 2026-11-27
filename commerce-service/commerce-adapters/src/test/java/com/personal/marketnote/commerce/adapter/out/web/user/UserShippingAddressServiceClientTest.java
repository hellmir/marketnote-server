package com.personal.marketnote.commerce.adapter.out.web.user;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserShippingAddressServiceClient ExternalCallLogger 통합 단위 테스트")
class UserShippingAddressServiceClientTest {

    private static final String BASE_URL = "http://user-service.test";
    private static final String OPERATION = "shipping.update_delivery_request";

    private MockRestServiceServer mockServer;
    private UserShippingAddressServiceClient client;
    private MeterRegistry meterRegistry;
    private Logger userLogger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacBuilder;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        RestClient.Builder builder = RestClient.builder().requestFactory((uri, httpMethod) ->
            restTemplate.getRequestFactory().createRequest(uri, httpMethod)
        );

        meterRegistry = new SimpleMeterRegistry();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(meterRegistry);

        client = new UserShippingAddressServiceClient(builder, BASE_URL, hmacBuilder, externalCallLogger);

        doAnswer(invocation -> {
            HttpHeaders headers = invocation.getArgument(0);
            headers.add("X-Test-Auth", "value");
            return null;
        }).when(hmacBuilder).applyHeaders(any(HttpHeaders.class), any(String.class), any(String.class));

        userLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.USER.loggerName());
        previousAdditive = userLogger.isAdditive();
        previousLevel = userLogger.getLevel();
        userLogger.setAdditive(false);
        userLogger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        userLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        userLogger.detachAppender(appender);
        appender.stop();
        userLogger.setAdditive(previousAdditive);
        userLogger.setLevel(previousLevel);
    }

    @Test
    @DisplayName("정상 응답 시 ExternalCallLogger의 success Timer가 기록되고 RestClient PATCH가 실행된다")
    void shouldRecordSuccessAndSendPatchRequest() {
        // given
        String expectedUrl = BASE_URL + "/api/v1/internal/shipping-addresses/123/delivery-request?userId=99";
        mockServer.expect(requestTo(expectedUrl))
            .andExpect(method(HttpMethod.PATCH))
            .andRespond(withSuccess());

        // when
        client.updateDeliveryRequest(123L, 99L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞");

        // then
        mockServer.verify();
        verify(hmacBuilder).applyHeaders(
            any(HttpHeaders.class), eq("PATCH"),
            eq("/api/v1/internal/shipping-addresses/123/delivery-request")
        );
        assertSuccessTimerExists();
    }

    @Test
    @DisplayName("응답 status가 4xx인 경우 ExternalCallLogger failure timer가 기록되고 외부 catch에서 swallow된다")
    void shouldRecordFailureAndSwallowOn4xx() {
        // given: RestClient 기본 default handler가 4xx에서 HttpClientErrorException(RestClientException 하위) throw
        mockServer.expect(method(HttpMethod.PATCH))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
            123L, 99L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞"
        )).doesNotThrowAnyException();

        assertFailureTimerExists();
    }

    @Test
    @DisplayName("RestClientException 발생 시 ExternalCallLogger가 failure timer를 기록하고 외부 catch에서 swallow된다")
    void shouldRecordFailureAndSwallowOnRestClientException() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andRespond(withServerError());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
            123L, 99L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞"
        )).doesNotThrowAnyException();

        assertFailureTimerExists();
    }

    @Test
    @DisplayName("tags에 shippingAddressId와 userId가 포함된다")
    void shouldIncludeShippingAddressIdAndUserIdInTags() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andRespond(withSuccess());

        // when
        client.updateDeliveryRequest(777L, 555L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞");

        // then
        boolean foundShippingKey = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains("shippingAddressId"));
        boolean foundShippingValue = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains("777"));
        boolean foundUserKey = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains("userId"));
        boolean foundUserValue = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains("555"));
        assertThat(foundShippingKey).isTrue();
        assertThat(foundShippingValue).isTrue();
        assertThat(foundUserKey).isTrue();
        assertThat(foundUserValue).isTrue();
    }

    @Test
    @DisplayName("deliveryRequestMessage가 null이면 body에 포함되지 않는다")
    void shouldOmitMessageInBodyWhenNull() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andExpect(jsonPath("$.deliveryRequestType").value("NONE"))
            .andExpect(jsonPath("$.deliveryRequestMessage").doesNotExist())
            .andRespond(withSuccess());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
            1L, 1L, DeliveryRequestType.NONE, null
        )).doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    @DisplayName("deliveryRequestMessage가 blank이면 body에 포함되지 않는다")
    void shouldOmitMessageInBodyWhenBlank() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andExpect(jsonPath("$.deliveryRequestMessage").doesNotExist())
            .andRespond(withSuccess());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
            1L, 1L, DeliveryRequestType.NONE, "   "
        )).doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    @DisplayName("deliveryRequestMessage가 있으면 body에 포함된다")
    void shouldIncludeMessageInBodyWhenPresent() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andExpect(jsonPath("$.deliveryRequestType").value("LEAVE_AT_DOOR"))
            .andExpect(jsonPath("$.deliveryRequestMessage").value("문앞에 두세요"))
            .andRespond(withSuccess());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
            1L, 1L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞에 두세요"
        )).doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    @DisplayName("operation 이름이 'shipping.update_delivery_request'이다")
    void shouldUseExpectedOperationName() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andRespond(withSuccess());

        // when
        client.updateDeliveryRequest(1L, 1L, DeliveryRequestType.NONE, null);

        // then
        Timer timer = meterRegistry.find("external.call")
            .tags("system", ExternalSystem.USER.metricTag(),
                "operation", OPERATION,
                "outcome", "success"
            )
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("system 메트릭 태그가 ExternalSystem.USER('user')이다")
    void shouldUseUserSystemTag() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
            .andRespond(withSuccess());

        // when
        client.updateDeliveryRequest(1L, 1L, DeliveryRequestType.NONE, null);

        // then
        Timer timer = meterRegistry.find("external.call")
            .tags("system", "user",
                "operation", OPERATION,
                "outcome", "success"
            )
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(ExternalSystem.USER.metricTag()).isEqualTo("user");
        assertThat(ExternalSystem.USER.loggerName()).isEqualTo("external.user");
    }

    private void assertSuccessTimerExists() {
        Timer timer = meterRegistry.find("external.call")
            .tags("system", ExternalSystem.USER.metricTag(),
                "operation", OPERATION,
                "outcome", "success"
            )
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    private void assertFailureTimerExists() {
        Timer timer = meterRegistry.find("external.call")
            .tags("system", ExternalSystem.USER.metricTag(),
                "operation", OPERATION,
                "outcome", "failure"
            )
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }
}
