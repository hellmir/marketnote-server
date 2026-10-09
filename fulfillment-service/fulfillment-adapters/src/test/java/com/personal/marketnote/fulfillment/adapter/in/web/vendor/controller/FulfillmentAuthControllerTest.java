package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.FulfillmentAuthTokenResponse;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.DisconnectFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentAuthController 풀필먼트 인증")
class FulfillmentAuthControllerTest {

    @InjectMocks
    private FulfillmentAuthController controller;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @Mock
    private DisconnectFulfillmentAuthUseCase disconnectFulfillmentAuthUseCase;

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/auth - 파스토 인증 요청")
    class RequestAccessToken {

        @Test
        @DisplayName("정상 요청 시 발급된 액세스 토큰을 응답으로 래핑하여 OK를 반환한다")
        void returnsOkWithIssuedAccessToken() {
            // given
            FulfillmentAccessToken token = FulfillmentAccessToken.of("ACC_TOKEN_XYZ", "20260415235959");
            when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(token);

            // when
            ResponseEntity<BaseResponse<FulfillmentAuthTokenResponse>> response = controller.requestAccessToken();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().tokenInfo().accessToken()).isEqualTo("ACC_TOKEN_XYZ");
            verify(requestFulfillmentAuthUseCase).requestAccessToken();
            verifyNoInteractions(disconnectFulfillmentAuthUseCase);
            verifyNoMoreInteractions(requestFulfillmentAuthUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/auth/disconnect - 파스토 인증 해제 요청")
    class DisconnectAccessToken {

        @Test
        @DisplayName("액세스 토큰 헤더를 받아 UseCase에 위임하고 OK 응답을 반환한다")
        void disconnectsWithGivenAccessToken() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.disconnectAccessToken("ACC_TOKEN_XYZ");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNull();
            verify(disconnectFulfillmentAuthUseCase).disconnectAccessToken("ACC_TOKEN_XYZ");
            verifyNoInteractions(requestFulfillmentAuthUseCase);
            verifyNoMoreInteractions(disconnectFulfillmentAuthUseCase);
        }
    }
}
