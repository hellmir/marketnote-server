package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DisconnectFulfillmentAuthService 테스트")
class DisconnectFulfillmentAuthUseCaseTest {
    @InjectMocks
    private DisconnectFulfillmentAuthService disconnectFulfillmentAuthService;

    @Mock
    private DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;

    @Test
    @DisplayName("액세스 토큰을 전달하면 포트의 disconnectAccessToken을 호출한다")
    void shouldCallPortDisconnectAccessToken() {
        // given
        String accessToken = "test-access-token";

        // when
        disconnectFulfillmentAuthService.disconnectAccessToken(accessToken);

        // then
        verify(disconnectFulfillmentAuthPort).disconnectAccessToken(accessToken);
    }
}
