package com.personal.marketnote.commerce.adapter.out.web.user;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserShippingAddressServiceClient 단위 테스트")
class UserShippingAddressServiceClientTest {

    private static final String BASE_URL = "http://user-service.test";

    private MockRestServiceServer mockServer;
    private UserShippingAddressServiceClient client;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacBuilder;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        RestClient.Builder builder = RestClient.builder().requestFactory((uri, httpMethod) ->
                restTemplate.getRequestFactory().createRequest(uri, httpMethod)
        );
        client = new UserShippingAddressServiceClient(builder, BASE_URL, hmacBuilder);

        doAnswer(invocation -> {
            HttpHeaders headers = invocation.getArgument(0);
            headers.add("X-Test-Auth", "value");
            return null;
        }).when(hmacBuilder).applyHeaders(any(HttpHeaders.class), any(String.class), any(String.class));
    }

    @Test
    @DisplayName("배송 요청사항을 PATCH 요청으로 전송한다")
    void shouldSendPatchRequest() {
        // given
        String expectedUrl = BASE_URL + "/api/v1/internal/shipping-addresses/123/delivery-request?userId=99";
        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess());

        // when
        client.updateDeliveryRequest(123L, 99L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞");

        // then
        mockServer.verify();
        verify(hmacBuilder).applyHeaders(any(HttpHeaders.class), eq("PATCH"),
                eq("/api/v1/internal/shipping-addresses/123/delivery-request"));
    }

    @Test
    @DisplayName("응답이 5xx여도 예외를 던지지 않는다 (fire-and-forget)")
    void shouldSwallowServerErrorResponse() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
                .andRespond(withServerError());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
                123L, 99L, DeliveryRequestType.LEAVE_AT_DOOR, null
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("RestClient에서 예외가 발생해도 예외를 삼킨다")
    void shouldSwallowExceptions() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
                .andRespond(withServerError());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
                123L, 99L, DeliveryRequestType.LEAVE_AT_DOOR, "문앞"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deliveryRequestMessage가 null이면 본문에서 제외된다")
    void shouldOmitMessageWhenNull() {
        // given
        mockServer.expect(method(HttpMethod.PATCH))
                .andRespond(withSuccess());

        // when & then
        assertThatCode(() -> client.updateDeliveryRequest(
                1L, 1L, DeliveryRequestType.NONE, null
        )).doesNotThrowAnyException();
    }
}
