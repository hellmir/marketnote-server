package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentSuppliersFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentSupplierFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
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
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentSupplierClient 테스트")
class FulfillmentSupplierClientTest {

    private FulfillmentSupplierClient client;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

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

        client = new FulfillmentSupplierClient(
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

    private void stubSupplierProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getSupplierPath()).thenReturn("/supplier/{customerCode}");
    }

    @SuppressWarnings("unchecked")
    private void stubPatchChainThrows(Exception exception) {
        RestClient.RequestBodyUriSpec methodSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(restClient.method(HttpMethod.PATCH)).thenReturn(methodSpec);
        when(methodSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    @SuppressWarnings("unchecked")
    private void stubGetChainThrows(Exception exception) {
        RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(rawSpec);
        when(rawSpec.uri(any(java.net.URI.class))).thenReturn(headerSpec);
        when(headerSpec.headers(any())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    @Nested
    @DisplayName("registerSupplier 실패")
    class RegisterSupplierFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentSupplierCommand command = RegisterFulfillmentSupplierCommand.of(
                    "CUST001", "valid-token", "테스트 공급사", "SUP001",
                    "Y", "20260101", "20261231", "12345", "서울시", "강남구",
                    "홍길동", "123-45-67890", "도매", "유통",
                    "02-1234-5678", "02-1234-5679",
                    "김철수", "과장", "010-1234-5678", "test@test.com",
                    null, null, null, null
            );

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(command))
                    .isInstanceOf(RegisterFulfillmentSupplierFailedException.class);
        }
    }

    @Nested
    @DisplayName("getSuppliers 실패")
    class GetSuppliersFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentSuppliersFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                    .isInstanceOf(GetFulfillmentSuppliersFailedException.class);
        }
    }

    @Nested
    @DisplayName("설정 검증 실패")
    class PropertyValidationFailure {

        @Test
        @DisplayName("baseUrl이 누락되면 IllegalStateException이 발생한다")
        void shouldThrowIllegalStateExceptionWhenBaseUrlMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn(null);

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
