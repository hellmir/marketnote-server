package com.personal.marketnote.notification.adapter.in.web.device.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.device.request.RegisterDeviceTokenRequest;
import com.personal.marketnote.notification.adapter.in.web.device.response.RegisterDeviceTokenResponse;
import com.personal.marketnote.notification.port.in.command.RegisterDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.result.device.RegisterDeviceTokenResult;
import com.personal.marketnote.notification.port.in.usecase.device.DeleteDeviceTokenUseCase;
import com.personal.marketnote.notification.port.in.usecase.device.RegisterDeviceTokenUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenControllerTest {

    @InjectMocks
    private DeviceTokenController controller;

    @Mock
    private RegisterDeviceTokenUseCase registerDeviceTokenUseCase;

    @Mock
    private DeleteDeviceTokenUseCase deleteDeviceTokenUseCase;

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId),
                Map.of("name", String.valueOf(userId)),
                List.of()
        );
    }

    @Nested
    @DisplayName("registerDeviceToken")
    class RegisterDeviceToken {

        @Test
        @DisplayName("디바이스 토큰을 등록하면 CREATED 응답을 반환한다")
        void shouldRegisterAndReturnCreated() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(
                    "fcm-token-123", "ANDROID", "device-001"
            );
            when(registerDeviceTokenUseCase.registerDeviceToken(any(RegisterDeviceTokenCommand.class)))
                    .thenReturn(RegisterDeviceTokenResult.ofCreated(1L));

            // when
            ResponseEntity<BaseResponse<RegisterDeviceTokenResponse>> response =
                    controller.registerDeviceToken(request, principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            assertThat(response.getBody().getContent().isNew()).isTrue();
        }

        @Test
        @DisplayName("기존 토큰을 갱신하면 isNew가 false인 CREATED 응답을 반환한다")
        void shouldUpdateAndReturnNotNew() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
            RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(
                    "fcm-token-updated", "IOS", "device-001"
            );
            when(registerDeviceTokenUseCase.registerDeviceToken(any(RegisterDeviceTokenCommand.class)))
                    .thenReturn(RegisterDeviceTokenResult.ofUpdated(1L));

            // when
            ResponseEntity<BaseResponse<RegisterDeviceTokenResponse>> response =
                    controller.registerDeviceToken(request, principal);

            // then
            assertThat(response.getBody().getContent().isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteDeviceToken")
    class DeleteDeviceToken {

        @Test
        @DisplayName("디바이스 토큰을 삭제하면 OK 응답을 반환한다")
        void shouldDeleteAndReturnOk() {
            // given
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    controller.deleteDeviceToken("device-001", principal);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteDeviceTokenUseCase).deleteDeviceToken(any());
        }
    }
}
