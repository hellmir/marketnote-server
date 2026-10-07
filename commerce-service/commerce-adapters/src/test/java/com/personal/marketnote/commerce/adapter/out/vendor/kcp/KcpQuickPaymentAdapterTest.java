package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyDeletionResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchPaymentApprovalResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.exception.PaymentVendorConnectionFailedException;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.ApproveQuickPaymentPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.DeleteBatchKeyPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.DeleteBatchKeyPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortResult;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KcpQuickPaymentAdapter 단위 테스트")
class KcpQuickPaymentAdapterTest {

    @InjectMocks
    private KcpQuickPaymentAdapter adapter;

    @Mock
    private KcpProperties kcpProperties;

    @Mock
    private KcpApiClient kcpApiClient;

    @Mock
    private KcpCertificateLoader kcpCertificateLoader;

    @Mock
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    private static final String SITE_CD = "T0000";
    private static final String CERT_INFO = "CERT_INFO";

    @BeforeEach
    void setUp() {
        when(kcpApiClient.toJsonNode(any())).thenReturn(new ObjectMapper().createObjectNode());
    }

    @Nested
    @DisplayName("registerTransaction")
    class RegisterTransaction {

        @Test
        @DisplayName("거래등록 성공 응답을 매핑한다")
        void shouldRegisterTransaction() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpProperties.getRetUrl()).thenReturn("http://return");
            KcpTradeRegisterResponse response = new KcpTradeRegisterResponse(
                    "0000", "OK", "APR_KEY", "http://pay", "TR_001", "CARD"
            );
            when(kcpApiClient.registerTrade(any())).thenReturn(response);

            // when
            RegisterQuickPaymentTransactionPortResult result = adapter.registerTransaction(
                    RegisterQuickPaymentTransactionPortCommand.builder().transactionId("TX_001").build()
            );

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.approvalKey()).isEqualTo("APR_KEY");
            assertThat(result.payUrl()).isEqualTo("http://pay");
        }

        @Test
        @DisplayName("실패 응답이면 success false를 반환한다")
        void shouldReturnFailure() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpProperties.getRetUrl()).thenReturn("http://return");
            KcpTradeRegisterResponse response = new KcpTradeRegisterResponse(
                    "9999", "FAIL", null, null, null, null
            );
            when(kcpApiClient.registerTrade(any())).thenReturn(response);

            // when
            RegisterQuickPaymentTransactionPortResult result = adapter.registerTransaction(
                    RegisterQuickPaymentTransactionPortCommand.builder().transactionId("TX_001").build()
            );

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.resultCode()).isEqualTo("9999");
        }

        @Test
        @DisplayName("KcpCommunicationException 발생 시 그대로 던진다")
        void shouldRethrowCommunicationException() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpProperties.getRetUrl()).thenReturn("http://return");
            when(kcpApiClient.registerTrade(any())).thenThrow(new KcpCommunicationException("통신 실패"));

            // when & then
            assertThatThrownBy(() -> adapter.registerTransaction(
                    RegisterQuickPaymentTransactionPortCommand.builder().transactionId("TX_001").build()
            )).isInstanceOf(KcpCommunicationException.class);
        }
    }

    @Nested
    @DisplayName("issueBatchKey")
    class IssueBatchKey {

        @Test
        @DisplayName("배치키 발급 성공 응답을 매핑한다")
        void shouldIssueBatchKey() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpCertificateLoader.loadCertInfo()).thenReturn(CERT_INFO);
            KcpBatchKeyIssuanceResponse response = mockBatchKeyResponse("0000", "OK", "BK_001");
            when(kcpApiClient.issueBatchKey(any())).thenReturn(response);

            // when
            IssueBatchKeyPortResult result = adapter.issueBatchKey(
                    IssueBatchKeyPortCommand.builder().encData("ED").encInfo("EI").build()
            );

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.batchKey()).isEqualTo("BK_001");
        }
    }

    @Nested
    @DisplayName("approvePayment")
    class ApprovePayment {

        @Test
        @DisplayName("배치 결제승인 성공 응답을 매핑한다")
        void shouldApprovePayment() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpCertificateLoader.loadCertInfo()).thenReturn(CERT_INFO);
            stubRetryConfig();
            KcpBatchPaymentApprovalResponse response = mockBatchApprovalResponse("0000");
            when(kcpApiClient.approveBatchPayment(any())).thenReturn(response);

            // when
            ApproveQuickPaymentPortResult result = adapter.approvePayment(buildApprovalCommand());

            // then
            assertThat(result.success()).isTrue();
            verify(kcpApiClient, times(1)).approveBatchPayment(any());
        }

        @Test
        @DisplayName("ConnectException이 maxAttempts회 발생하면 PaymentVendorConnectionFailedException을 던진다")
        void shouldThrowConnectionFailedAfterMaxAttempts() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpCertificateLoader.loadCertInfo()).thenReturn(CERT_INFO);
            stubRetryConfig();
            ResourceAccessException connectError = new ResourceAccessException(
                    "Connection refused", new ConnectException("Connection refused")
            );
            when(kcpApiClient.approveBatchPayment(any())).thenThrow(connectError);

            // when & then
            assertThatThrownBy(() -> adapter.approvePayment(buildApprovalCommand()))
                    .isInstanceOf(PaymentVendorConnectionFailedException.class);
            verify(kcpApiClient, times(3)).approveBatchPayment(any());
        }
    }

    @Nested
    @DisplayName("deleteBatchKey")
    class DeleteBatchKey {

        @Test
        @DisplayName("배치키 삭제 성공 응답을 매핑한다")
        void shouldDeleteBatchKey() {
            // given
            when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
            when(kcpCertificateLoader.loadCertInfo()).thenReturn(CERT_INFO);
            KcpBatchKeyDeletionResponse response = mockBatchKeyDeletionResponse("0000");
            when(kcpApiClient.deleteBatchKey(any())).thenReturn(response);

            // when
            DeleteBatchKeyPortResult result = adapter.deleteBatchKey(
                    DeleteBatchKeyPortCommand.builder().batchKey("BK_001").groupId("GROUP").build()
            );

            // then
            assertThat(result.success()).isTrue();
        }
    }

    private void stubRetryConfig() {
        KcpProperties.Retry retryConfig = new KcpProperties.Retry();
        retryConfig.setMaxAttempts(3);
        retryConfig.setInitialDelayMs(1L);
        retryConfig.setBackoffMultiplier(2);
        retryConfig.setReadTimeoutMaxAttempts(2);
        when(kcpProperties.getRetry()).thenReturn(retryConfig);
    }

    private ApproveQuickPaymentPortCommand buildApprovalCommand() {
        return ApproveQuickPaymentPortCommand.builder()
                .batchKey("BK_001")
                .groupId("GROUP")
                .orderKey("ORDER_001")
                .amount("10000")
                .goodName("상품")
                .build();
    }

    private KcpBatchKeyIssuanceResponse mockBatchKeyResponse(String resCd, String resMsg, String batchKey) {
        return new KcpBatchKeyIssuanceResponse(resCd, resMsg, batchKey, "00", "카드", "01", "02");
    }

    private KcpBatchPaymentApprovalResponse mockBatchApprovalResponse(String resCd) {
        return new KcpBatchPaymentApprovalResponse(
                resCd, "OK", "CARD", "TNO_001", "10000", "00", "10000", "카드",
                "1234-****-5678", "APP_001", "20260101120000", "00", "ACQU", "ACQU_NAME",
                "N", "01", "02"
        );
    }

    private KcpBatchKeyDeletionResponse mockBatchKeyDeletionResponse(String resCd) {
        return new KcpBatchKeyDeletionResponse(resCd, "OK");
    }
}
