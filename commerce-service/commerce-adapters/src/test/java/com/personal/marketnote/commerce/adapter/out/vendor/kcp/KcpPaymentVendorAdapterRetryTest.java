package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpPaymentApprovalResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
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
@DisplayName("KcpPaymentVendorAdapter 결제 승인 재시도 테스트")
class KcpPaymentVendorAdapterRetryTest {

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
    private static final String ORDER_NO = "test-order-key";
    private static final String CERT_INFO = "test-cert-info";

    @BeforeEach
    void setUp() {
        when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
        when(kcpCertificateLoader.loadCertInfo()).thenReturn(CERT_INFO);

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
            KcpPaymentApprovalResponse response = createSuccessResponse();
            when(kcpApiClient.approvePayment(any())).thenReturn(response);

            PaymentApprovalVendorResult result = adapter.approvePayment(createCommand());

            assertThat(result.resCd()).isEqualTo("0000");
            assertThat(result.tno()).isEqualTo("tno_123");
            verify(kcpApiClient, times(1)).approvePayment(any());
        }

        @Test
        @DisplayName("첫 번째 시도에서 KCP 실패 응답을 받으면 재시도 없이 결과를 반환한다")
        void shouldReturnFailureResponseWithoutRetry() {
            KcpPaymentApprovalResponse response = createFailureResponse("8001", "카드 인증 실패");
            when(kcpApiClient.approvePayment(any())).thenReturn(response);

            PaymentApprovalVendorResult result = adapter.approvePayment(createCommand());

            assertThat(result.resCd()).isEqualTo("8001");
            verify(kcpApiClient, times(1)).approvePayment(any());
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
            when(kcpApiClient.approvePayment(any())).thenThrow(connectError);

            assertThatThrownBy(() -> adapter.approvePayment(createCommand()))
                    .isInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(3)).approvePayment(any());
        }

        @Test
        @DisplayName("ConnectException 발생 후 재시도에서 성공하면 결과를 반환한다")
        void shouldReturnResultWhenRetrySucceeds() {
            ResourceAccessException connectError = new ResourceAccessException(
                    "Connection refused", new ConnectException("Connection refused")
            );
            KcpPaymentApprovalResponse successResponse = createSuccessResponse();

            when(kcpApiClient.approvePayment(any()))
                    .thenThrow(connectError)
                    .thenThrow(connectError)
                    .thenReturn(successResponse);

            PaymentApprovalVendorResult result = adapter.approvePayment(createCommand());

            assertThat(result.resCd()).isEqualTo("0000");
            verify(kcpApiClient, times(3)).approvePayment(any());
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
            when(kcpApiClient.approvePayment(any())).thenThrow(dnsError);

            assertThatThrownBy(() -> adapter.approvePayment(createCommand()))
                    .isInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(3)).approvePayment(any());
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
            when(kcpApiClient.approvePayment(any())).thenThrow(timeoutError);

            assertThatThrownBy(() -> adapter.approvePayment(createCommand()))
                    .isInstanceOf(KcpCommunicationException.class)
                    .isNotInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(2)).approvePayment(any());
        }

        @Test
        @DisplayName("SocketTimeoutException 발생 후 재시도에서 성공하면 결과를 반환한다")
        void shouldReturnResultWhenRetrySucceeds() {
            ResourceAccessException timeoutError = new ResourceAccessException(
                    "Read timed out", new SocketTimeoutException("Read timed out")
            );
            KcpPaymentApprovalResponse successResponse = createSuccessResponse();

            when(kcpApiClient.approvePayment(any()))
                    .thenThrow(timeoutError)
                    .thenReturn(successResponse);

            PaymentApprovalVendorResult result = adapter.approvePayment(createCommand());

            assertThat(result.resCd()).isEqualTo("0000");
            verify(kcpApiClient, times(2)).approvePayment(any());
        }

        @Test
        @DisplayName("SocketTimeoutException 발생 후 재시도에서 KCP 실패 응답을 받으면 결과를 반환한다")
        void shouldReturnFailureResponseWhenRetryGetsKcpFailure() {
            ResourceAccessException timeoutError = new ResourceAccessException(
                    "Read timed out", new SocketTimeoutException("Read timed out")
            );
            KcpPaymentApprovalResponse failureResponse = createFailureResponse("8001", "카드 인증 실패");

            when(kcpApiClient.approvePayment(any()))
                    .thenThrow(timeoutError)
                    .thenReturn(failureResponse);

            PaymentApprovalVendorResult result = adapter.approvePayment(createCommand());

            assertThat(result.resCd()).isEqualTo("8001");
            verify(kcpApiClient, times(2)).approvePayment(any());
        }
    }

    @Nested
    @DisplayName("즉시 실패 (재시도 불가)")
    class ImmediateFailureTest {

        @Test
        @DisplayName("KcpCommunicationException 발생 시 재시도 없이 즉시 던진다")
        void shouldThrowImmediatelyOnKcpCommunicationException() {
            KcpCommunicationException parseError = new KcpCommunicationException("응답 파싱 실패");
            when(kcpApiClient.approvePayment(any())).thenThrow(parseError);

            assertThatThrownBy(() -> adapter.approvePayment(createCommand()))
                    .isInstanceOf(KcpCommunicationException.class)
                    .hasMessage("응답 파싱 실패");

            verify(kcpApiClient, times(1)).approvePayment(any());
        }

        @Test
        @DisplayName("일반 RuntimeException 발생 시 재시도 없이 즉시 던진다")
        void shouldThrowImmediatelyOnUnexpectedException() {
            when(kcpApiClient.approvePayment(any())).thenThrow(new RuntimeException("예상치 못한 오류"));

            assertThatThrownBy(() -> adapter.approvePayment(createCommand()))
                    .isInstanceOf(RuntimeException.class);

            verify(kcpApiClient, times(1)).approvePayment(any());
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

            when(kcpApiClient.approvePayment(any()))
                    .thenThrow(connectError)
                    .thenThrow(timeoutError)
                    .thenThrow(timeoutError);

            assertThatThrownBy(() -> adapter.approvePayment(createCommand()))
                    .isInstanceOf(KcpCommunicationException.class)
                    .isNotInstanceOf(PaymentVendorConnectionFailedException.class);

            verify(kcpApiClient, times(3)).approvePayment(any());
        }
    }

    private PaymentApprovalVendorCommand createCommand() {
        return PaymentApprovalVendorCommand.builder()
                .encData("enc_data_test")
                .encInfo("enc_info_test")
                .ordrMony("50000")
                .ordrNo(ORDER_NO)
                .payType("PACA")
                .build();
    }

    private KcpPaymentApprovalResponse createSuccessResponse() {
        return new KcpPaymentApprovalResponse(
                "0000", "승인 성공", "Approved", "tno_123", "50000",
                "PACA", "CCLG", "신한카드", "1234-****-****-5678",
                "12345678", "20260305153000", null, null, "00",
                "50000", "0", "Y", null, null
        );
    }

    private KcpPaymentApprovalResponse createFailureResponse(String resCd, String resMsg) {
        return new KcpPaymentApprovalResponse(
                resCd, resMsg, null, null, null,
                null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null
        );
    }
}
