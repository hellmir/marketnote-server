package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
import com.personal.marketnote.commerce.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("KcpPaymentVendorAdapter 거래 등록 테스트")
class KcpPaymentVendorAdapterRegisterTradeTest {

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
    private static final String RET_URL = "https://ret.test";
    private static final String ORDER_KEY = "order-key-1";

    @BeforeEach
    void setUp() {
        when(kcpProperties.getSiteCd()).thenReturn(SITE_CD);
        when(kcpProperties.getRetUrl()).thenReturn(RET_URL);
        when(kcpApiClient.toJsonNode(any())).thenReturn(new ObjectMapper().createObjectNode());
    }

    @Nested
    @DisplayName("getVendorKey / getShopCode")
    class VendorMetadata {

        @Test
        @DisplayName("벤더 키는 NHN_KCP를 반환한다")
        void shouldReturnVendorKey() {
            assertThat(adapter.getVendorKey()).isEqualTo("NHN_KCP");
        }

        @Test
        @DisplayName("샵 코드는 KcpProperties의 siteCd를 반환한다")
        void shouldReturnShopCode() {
            assertThat(adapter.getShopCode()).isEqualTo(SITE_CD);
        }
    }

    @Nested
    @DisplayName("registerTrade")
    class RegisterTrade {

        @Test
        @DisplayName("거래 등록이 성공하면 success=true 결과를 반환하고 요청/응답을 기록한다")
        void shouldReturnSuccessResult() {
            // given
            TradeRegisterVendorCommand command = TradeRegisterVendorCommand.builder()
                    .orderKey(ORDER_KEY)
                    .orderAmount("10000")
                    .payMethod("CARD")
                    .goodName("테스트 상품")
                    .build();

            KcpTradeRegisterResponse response = new KcpTradeRegisterResponse(
                    "0000", "정상 처리", "APPROVAL_KEY_1", "https://pg/pay", "TRACE_1", "CARD"
            );
            when(kcpApiClient.registerTrade(any())).thenReturn(response);

            // when
            TradeRegisterVendorResult result = adapter.registerTrade(command);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.resultCode()).isEqualTo("0000");
            assertThat(result.approvalKey()).isEqualTo("APPROVAL_KEY_1");
            assertThat(result.payUrl()).isEqualTo("https://pg/pay");
            assertThat(result.traceNo()).isEqualTo("TRACE_1");
            verify(kcpApiClient).registerTrade(any());
            verify(vendorCommunicationRecorder, org.mockito.Mockito.atLeast(2))
                    .record(
                            any(CommerceVendorCommunicationTargetType.class),
                            any(CommerceVendorCommunicationType.class),
                            any(CommerceVendorCommunicationSenderType.class),
                            any(String.class),
                            any(CommerceVendorName.class),
                            any(String.class),
                            any(JsonNode.class)
                    );
        }

        @Test
        @DisplayName("거래 등록 응답 코드가 0000이 아니면 success=false 결과를 반환한다")
        void shouldReturnFailureResultWhenResponseNotSuccess() {
            // given
            TradeRegisterVendorCommand command = TradeRegisterVendorCommand.builder()
                    .orderKey(ORDER_KEY)
                    .orderAmount("10000")
                    .payMethod("CARD")
                    .goodName("테스트 상품")
                    .build();

            KcpTradeRegisterResponse response = new KcpTradeRegisterResponse(
                    "9999", "오류", null, null, null, null
            );
            when(kcpApiClient.registerTrade(any())).thenReturn(response);

            // when
            TradeRegisterVendorResult result = adapter.registerTrade(command);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.resultCode()).isEqualTo("9999");
            assertThat(result.resultMessage()).isEqualTo("오류");
        }

        @Test
        @DisplayName("거래 등록 중 KcpCommunicationException이 발생하면 예외를 전파하고 에러를 기록한다")
        void shouldRecordErrorAndRethrow() {
            // given
            TradeRegisterVendorCommand command = TradeRegisterVendorCommand.builder()
                    .orderKey(ORDER_KEY)
                    .orderAmount("10000")
                    .payMethod("CARD")
                    .goodName("테스트 상품")
                    .build();

            KcpCommunicationException exception = new KcpCommunicationException("통신 실패", new RuntimeException());
            when(kcpApiClient.registerTrade(any())).thenThrow(exception);

            // when & then
            assertThatThrownBy(() -> adapter.registerTrade(command))
                    .isInstanceOf(KcpCommunicationException.class);

            verify(vendorCommunicationRecorder)
                    .record(
                            any(CommerceVendorCommunicationTargetType.class),
                            any(CommerceVendorCommunicationType.class),
                            any(CommerceVendorCommunicationSenderType.class),
                            any(String.class),
                            any(CommerceVendorName.class),
                            any(String.class),
                            any(JsonNode.class),
                            any(String.class)
                    );
            verify(kcpCertificateLoader, never()).loadCertInfo();
        }
    }
}
