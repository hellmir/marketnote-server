package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 인증 요청 테스트")
class RequestFulfillmentAuthUseCaseTest {
    @InjectMocks
    private RequestFulfillmentAuthService service;

    @Mock
    private RequestFulfillmentAuthPort requestFulfillmentAuthPort;

    @Test
    @DisplayName("포트를 통해 액세스 토큰을 요청하여 반환한다")
    void shouldRequestAccessTokenFromPort() {
        // given
        FulfillmentAccessToken expectedToken = FulfillmentAccessToken.of("token-value", "20270101120000");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(expectedToken);

        // when
        FulfillmentAccessToken result = service.requestAccessToken();

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(requestFulfillmentAuthPort).requestAccessToken();
    }
}
