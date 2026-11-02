package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileCreateState;
import com.personal.marketnote.notification.port.in.command.AuthenticateAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.AuthenticateAligoSenderProfileResult;
import com.personal.marketnote.notification.port.out.aligo.RequestAligoSenderAuthenticationPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoSenderProfilePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateAligoSenderProfileUseCaseTest {

    @InjectMocks
    private AuthenticateAligoSenderProfileService authenticateAligoSenderProfileService;

    @Mock
    private RequestAligoSenderAuthenticationPort requestAligoSenderAuthenticationPort;

    @Mock
    private SaveAligoSenderProfilePort saveAligoSenderProfilePort;

    @Test
    @DisplayName("발신프로필 인증 요청 시 외부 API를 호출하고 결과를 저장한다")
    void shouldCallExternalApiAndSaveResult() {
        // given
        AuthenticateAligoSenderProfileCommand command = new AuthenticateAligoSenderProfileCommand(
                "@testChannel", "01012345678"
        );
        when(requestAligoSenderAuthenticationPort.requestAuthentication("@testChannel", "01012345678"))
                .thenReturn("test-sender-key");

        AligoSenderProfile savedProfile = AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey("test-sender-key")
                        .plusId("@testChannel")
                        .phoneNumber("01012345678")
                        .build()
        );
        when(saveAligoSenderProfilePort.save(any(AligoSenderProfile.class))).thenReturn(savedProfile);

        // when
        AuthenticateAligoSenderProfileResult result = authenticateAligoSenderProfileService
                .authenticateSenderProfile(command);

        // then
        assertThat(result.senderKey()).isEqualTo("test-sender-key");
        assertThat(result.plusId()).isEqualTo("@testChannel");
        verify(requestAligoSenderAuthenticationPort).requestAuthentication("@testChannel", "01012345678");
        verify(saveAligoSenderProfilePort).save(any(AligoSenderProfile.class));
    }
}
