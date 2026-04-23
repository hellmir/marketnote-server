package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpPaymentCancelResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KcpPaymentVendorAdapter 결제 취소 재시도 테스트")
class KcpPaymentVendorAdapterCancelRetryTest {

    @InjectMocks
    private KcpPaymentVendorAdapter adapter;

    @Mock
    private KcpProperties kcpProperties;

    @Mock
    private KcpApiClient kcpApiClient;

    @Mock
    private KcpCertificateLoader kcpCertificateLoader;

    @Mock
    private KcpSignatureGenerator kcpSignatureGenerator;

    @Mock
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    private static final String SITE_CD = "T0000";
    private static final String TRANSACTION_ID = "tno_cancel_123";
    private static final String CERT_INFO = "test-cert-info";
    private static final String SIGN_DATA = "test-sign-data";

    @BeforeEach
    void setUp() {
        when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
        when(kcpCertificateLoader.loadCertInfo()).thenReturn(CERT_INFO);
        when(kcpSignatureGenerator.generateSignData(any(), any(), any())).thenReturn(SIGN_DATA);

        KcpProperties.Retry retryConfig = new KcpProperties.Retry();
        retryConfig.setMaxAttempts(3);
        retryConfig.setInitialDelayMs(10L);
        retryConfig.setBackoffMultiplier(2);
        retryConfig.setReadTimeoutMaxAttempts(2);
        when(kcpProperties.getRetry()).thenReturn(retryConfig);

        when(kcpApiClient.toJsonNode(any())).thenReturn(new ObjectMapper().createObjectNode());
    }

    @Nested
    @DisplayName("재시도 없이 성공")
    class NoRetrySuccessTest {

        @Test
        @DisplayName("첫 번째 시도에서 성공하면 1회만 호출하고 결과를 반환한다")
        void shouldReturnResultOnFirstSuccess() {
            KcpPaymentCancelResponse response = createSuccessCancelResponse();
            when(kcpApiClient.cancelPayment(any())).thenReturn(response);

            PaymentCancelVendorResult result = adapter.cancelPayment(createCancelCommand());

            assertThat(result.resultCode()).isEqualTo("0000");
            assertThat(result.amount()).isEqualTo("50000");
            verify(kcpApiClient, times(1)).cancelPayment(any());
        }

        @Test
        @DisplayName("첫 번째 시도에서 KCP 실패 응답을 받으면 재시도 없이 결과를 반환한다")
        void shouldReturnFailureResponseWithoutRetry() {
            KcpPaymentCancelResponse response = createFailureCancelResponse("8102", "이미 취소된 거래");
            when(kcpApiClient.cancelPayment(any())).thenReturn(response);

            PaymentCancelVendorResult result = adapter.cancelPayment(createCancelCommand());

            assertThat(result.resultCode()).isEqualTo("8102");
            assertThat(result.success()).isFalse();
            verify(kcpApiClient, times(1)).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("ConnectException 재시도")
    class ConnectExceptionRetryTest {

        @Test
        @DisplayName("ConnectException이 maxAttempts회 연속 발생하면 PaymentVendorConnectionFailedException을 던진다")
        void shouldThrowConnectionFailedAfterMaxAttempts() {
            ResourceAccessException connectError = new ResourceAccessException(
                    "Connection refused", new ConnectException("Connection refused")
            );
            when(kcpApiClient.cancelPayment(any())).thenThrow(connectError);

            assertThatThrownBy(() -> adapter.cancelPayment(createCancelCommand()))
                    .isInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(3)).cancelPayment(any());
        }

        @Test
        @DisplayName("ConnectException 발생 후 재시도에서 성공하면 결과를 반환한다")
        void shouldReturnResultWhenRetrySucceeds() {
            ResourceAccessException connectError = new ResourceAccessException(
                    "Connection refused", new ConnectException("Connection refused")
            );
            KcpPaymentCancelResponse successResponse = createSuccessCancelResponse();

            when(kcpApiClient.cancelPayment(any()))
                    .thenThrow(connectError)
                    .thenThrow(connectError)
                    .thenReturn(successResponse);

            PaymentCancelVendorResult result = adapter.cancelPayment(createCancelCommand());

            assertThat(result.resultCode()).isEqualTo("0000");
            verify(kcpApiClient, times(3)).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("UnknownHostException 재시도")
    class UnknownHostExceptionRetryTest {

        @Test
        @DisplayName("UnknownHostException이 maxAttempts회 연속 발생하면 PaymentVendorConnectionFailedException을 던진다")
        void shouldThrowConnectionFailedAfterMaxAttempts() {
            ResourceAccessException dnsError = new ResourceAccessException(
                    "Unknown host", new UnknownHostException("spl.kcp.co.kr")
            );
            when(kcpApiClient.cancelPayment(any())).thenThrow(dnsError);

            assertThatThrownBy(() -> adapter.cancelPayment(createCancelCommand()))
                    .isInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(3)).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("SocketTimeoutException 재시도")
    class SocketTimeoutRetryTest {

        @Test
        @DisplayName("SocketTimeoutException이 readTimeoutMaxAttempts회 발생하면 KcpCommunicationException을 던진다")
        void shouldThrowCommunicationExceptionAfterReadTimeoutMaxAttempts() {
            ResourceAccessException timeoutError = new ResourceAccessException(
                    "Read timed out", new SocketTimeoutException("Read timed out")
            );
            when(kcpApiClient.cancelPayment(any())).thenThrow(timeoutError);

            assertThatThrownBy(() -> adapter.cancelPayment(createCancelCommand()))
                    .isInstanceOf(KcpCommunicationException.class)
                    .isNotInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(2)).cancelPayment(any());
        }

        @Test
        @DisplayName("SocketTimeoutException 발생 후 재시도에서 성공하면 결과를 반환한다")
        void shouldReturnResultWhenRetrySucceeds() {
            ResourceAccessException timeoutError = new ResourceAccessException(
                    "Read timed out", new SocketTimeoutException("Read timed out")
            );
            KcpPaymentCancelResponse successResponse = createSuccessCancelResponse();

            when(kcpApiClient.cancelPayment(any()))
                    .thenThrow(timeoutError)
                    .thenReturn(successResponse);

            PaymentCancelVendorResult result = adapter.cancelPayment(createCancelCommand());

            assertThat(result.resultCode()).isEqualTo("0000");
            verify(kcpApiClient, times(2)).cancelPayment(any());
        }

        @Test
        @DisplayName("SocketTimeoutException 발생 후 재시도에서 KCP 실패 응답을 받으면 결과를 반환한다")
        void shouldReturnFailureResponseWhenRetryGetsKcpFailure() {
            ResourceAccessException timeoutError = new ResourceAccessException(
                    "Read timed out", new SocketTimeoutException("Read timed out")
            );
            KcpPaymentCancelResponse failureResponse = createFailureCancelResponse("8102", "이미 취소된 거래");

            when(kcpApiClient.cancelPayment(any()))
                    .thenThrow(timeoutError)
                    .thenReturn(failureResponse);

            PaymentCancelVendorResult result = adapter.cancelPayment(createCancelCommand());

            assertThat(result.resultCode()).isEqualTo("8102");
            verify(kcpApiClient, times(2)).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("즉시 실패 (재시도 불가)")
    class ImmediateFailureTest {

        @Test
        @DisplayName("KcpCommunicationException 발생 시 재시도 없이 즉시 던진다")
        void shouldThrowImmediatelyOnKcpCommunicationException() {
            KcpCommunicationException parseError = new KcpCommunicationException("응답 파싱 실패");
            when(kcpApiClient.cancelPayment(any())).thenThrow(parseError);

            assertThatThrownBy(() -> adapter.cancelPayment(createCancelCommand()))
                    .isInstanceOf(KcpCommunicationException.class)
                    .hasMessage("응답 파싱 실패");

            verify(kcpApiClient, times(1)).cancelPayment(any());
        }

        @Test
        @DisplayName("일반 RuntimeException 발생 시 재시도 없이 즉시 던진다")
        void shouldThrowImmediatelyOnUnexpectedException() {
            when(kcpApiClient.cancelPayment(any())).thenThrow(new RuntimeException("예상치 못한 오류"));

            assertThatThrownBy(() -> adapter.cancelPayment(createCancelCommand()))
                    .isInstanceOf(RuntimeException.class);

            verify(kcpApiClient, times(1)).cancelPayment(any());
        }
    }

    @Nested
    @DisplayName("혼합 예외 시나리오")
    class MixedExceptionTest {

        @Test
        @DisplayName("ConnectException 후 SocketTimeoutException 발생 시 readTimeout 횟수를 별도 추적한다")
        void shouldTrackReadTimeoutSeparately() {
            ResourceAccessException connectError = new ResourceAccessException(
                    "Connection refused", new ConnectException("Connection refused")
            );
            ResourceAccessException timeoutError = new ResourceAccessException(
                    "Read timed out", new SocketTimeoutException("Read timed out")
            );

            when(kcpApiClient.cancelPayment(any()))
                    .thenThrow(connectError)
                    .thenThrow(timeoutError)
                    .thenThrow(timeoutError);

            assertThatThrownBy(() -> adapter.cancelPayment(createCancelCommand()))
                    .isInstanceOf(KcpCommunicationException.class)
                    .isNotInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(3)).cancelPayment(any());
        }
    }

    private PaymentCancelVendorCommand createCancelCommand() {
        return PaymentCancelVendorCommand.builder()
                .transactionId(TRANSACTION_ID)
                .cancelType("STSC")
                .cancelAmount(50000L)
                .remainAmount(0L)
                .cancelReason("테스트 취소")
                .build();
    }

    private KcpPaymentCancelResponse createSuccessCancelResponse() {
        return new KcpPaymentCancelResponse("0000", "취소 성공", "50000");
    }

    private KcpPaymentCancelResponse createFailureCancelResponse(String resCd, String resMsg) {
        return new KcpPaymentCancelResponse(resCd, resMsg, null);
    }
}
