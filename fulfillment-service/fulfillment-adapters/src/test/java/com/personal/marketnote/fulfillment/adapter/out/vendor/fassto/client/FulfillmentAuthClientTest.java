package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentAuthDataResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentAuthResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAuthDisconnectFailedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentAuthRequestFailedException;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentAuthClient 테스트")
class FulfillmentAuthClientTest {

    private FulfillmentAuthClient client;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private FulfillmentAuthProperties properties;

    @Mock
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    @Mock
    private VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;

    @Mock
    private VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);

        client = new FulfillmentAuthClient(
                builder,
                objectMapper,
                properties,
                vendorCommunicationRecorder,
                vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler
        );
    }

    private void stubPayloadGenerator() {
        lenient().when(vendorCommunicationPayloadGenerator.buildPayloadJson(any()))
                .thenReturn(objectMapper.createObjectNode());
    }

    private void stubAuthProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getConnectPath()).thenReturn("/auth/connect");
        when(properties.getApiCd()).thenReturn("API_CD");
        when(properties.getApiKey()).thenReturn("API_KEY");
    }

    private void stubDisconnectProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDisconnectPath()).thenReturn("/auth/disconnect");
    }

    @SuppressWarnings("unchecked")
    private void stubPostChain(ResponseEntity<?> response) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private void stubPostChainThrows(Exception exception) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    @Nested
    @DisplayName("requestAccessToken 성공")
    class RequestAccessTokenSuccess {

        @Test
        @DisplayName("정상 응답 시 FulfillmentAccessToken을 반환한다")
        void shouldReturnAccessTokenOnSuccessResponse() {
            // given
            stubAuthProperties();
            stubPayloadGenerator();
            FulfillmentAuthResponse authResponse = new FulfillmentAuthResponse(
                    new FulfillmentResponseHeader("200", "Success", 1),
                    new FulfillmentAuthDataResponse("test-token", "20261231235959"),
                    null
            );
            stubPostChain(ResponseEntity.ok(authResponse));

            // when
            FulfillmentAccessToken token = client.requestAccessToken();

            // then
            assertThat(token.getValue()).isEqualTo("test-token");
        }
    }

    @Nested
    @DisplayName("requestAccessToken 실패")
    class RequestAccessTokenFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 FulfillmentAuthRequestFailedException이 발생한다")
        void shouldThrowRequestFailedExceptionWhenAllRetriesFail() {
            // given
            stubAuthProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new RuntimeException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> client.requestAccessToken())
                    .isInstanceOf(FulfillmentAuthRequestFailedException.class);
        }

        @Test
        @DisplayName("응답 헤더가 실패 코드이면 FulfillmentAuthRequestFailedException이 발생한다")
        void shouldThrowRequestFailedExceptionWhenResponseHeaderIndicatesFailure() {
            // given
            stubAuthProperties();
            stubPayloadGenerator();
            FulfillmentAuthResponse authResponse = new FulfillmentAuthResponse(
                    new FulfillmentResponseHeader("400", "Bad Request", 0),
                    null,
                    null
            );
            stubPostChain(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse));

            // when & then
            assertThatThrownBy(() -> client.requestAccessToken())
                    .isInstanceOf(FulfillmentAuthRequestFailedException.class);
        }

        @Test
        @DisplayName("baseUrl이 누락되면 IllegalStateException이 발생한다")
        void shouldThrowIllegalStateExceptionWhenBaseUrlMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> client.requestAccessToken())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("disconnectAccessToken 성공")
    class DisconnectAccessTokenSuccess {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("정상 응답 시 토큰을 해제한다")
        void shouldDisconnectTokenOnSuccessResponse() {
            // given
            stubDisconnectProperties();
            stubPayloadGenerator();
            RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
            RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

            when(restClient.get()).thenReturn(rawSpec);
            when(rawSpec.uri(any(java.net.URI.class))).thenReturn(headerSpec);
            when(headerSpec.headers(any())).thenReturn(headerSpec);
            when(headerSpec.retrieve()).thenReturn(respSpec);
            when(respSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("OK"));

            // when
            client.disconnectAccessToken("valid-token");

            // then
            verify(restClient).get();
        }
    }

    @Nested
    @DisplayName("disconnectAccessToken 실패")
    class DisconnectAccessTokenFailure {

        @Test
        @DisplayName("accessToken이 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowIllegalArgumentExceptionWhenTokenIsNull() {
            // when & then
            assertThatThrownBy(() -> client.disconnectAccessToken(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("모든 재시도 실패 시 FulfillmentAuthDisconnectFailedException이 발생한다")
        void shouldThrowDisconnectFailedExceptionWhenAllRetriesFail() {
            // given
            stubDisconnectProperties();
            stubPayloadGenerator();
            RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
            RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

            when(restClient.get()).thenReturn(rawSpec);
            when(rawSpec.uri(any(java.net.URI.class))).thenReturn(headerSpec);
            when(headerSpec.headers(any())).thenReturn(headerSpec);
            when(headerSpec.retrieve()).thenReturn(respSpec);
            when(respSpec.toEntity(String.class)).thenThrow(new RuntimeException("Connection refused"));

            // when & then
            assertThatThrownBy(() -> client.disconnectAccessToken("test-token"))
                    .isInstanceOf(FulfillmentAuthDisconnectFailedException.class);
        }
    }
}
