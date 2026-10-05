package com.personal.marketnote.reward.adapter.in.web.offerwall;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.VendorVerificationFailedException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.reward.adapter.in.web.point.response.UpdateUserPointResponse;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.exception.DuplicateOfferwallRewardException;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.usecase.offerwall.HandleOfferwallRewardUseCase;
import com.personal.marketnote.reward.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.reward.utility.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OfferwallController 테스트")
class OfferwallControllerTest {

    @Mock
    private HandleOfferwallRewardUseCase handleOfferwallRewardUseCase;
    @Mock
    private VendorCommunicationRecorder vendorCommunicationRecorder;
    @Mock
    private VendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    @Mock
    private VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;

    @InjectMocks
    private OfferwallController controller;

    private final JsonNode emptyNode = JsonNodeFactory.instance.objectNode();

    @BeforeEach
    void setUp() {
        given(vendorCommunicationPayloadGenerator.buildResponsePayloadJson(true, 1, "success"))
                .willReturn(emptyNode);
    }

    @Nested
    @DisplayName("handleAdpopcornCallback")
    class AdpopcornCallback {

        @BeforeEach
        void setUp() {
            given(vendorCommunicationPayloadGenerator.buildAdpopcornPayloadJson(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).willReturn(emptyNode);
        }

        @Test
        @DisplayName("정상 처리 시 success 응답을 반환하고 요청/응답 통신을 기록한다")
        void shouldReturnSuccessAndRecord() {
            // given
            given(handleOfferwallRewardUseCase.handle(any(RegisterOfferwallRewardCommand.class))).willReturn(1L);

            // when
            ResponseEntity<String> response = controller.handleAdpopcornCallback(
                    "REW-1", "user-1", UserDeviceType.ANDROID, "CAMP-1", 1,
                    "캠페인", 100L, "signed", 1001, "앱", "adid", "idfa", "20260414120000"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(vendorCommunicationRecorder, times(2)).record(any(), any(), any(), anyString(), any(), anyString(), any());
        }

        @Test
        @DisplayName("VendorVerificationFailedException 발생 시 1100 코드로 실패 핸들러를 호출한다")
        void shouldHandleVendorVerificationFailed() {
            // given
            given(handleOfferwallRewardUseCase.handle(any())).willThrow(new VendorVerificationFailedException("invalid"));
            given(vendorCommunicationFailureHandler.handleFailure(any(), any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(ResponseEntity.badRequest().body("fail"));

            // when
            ResponseEntity<String> response = controller.handleAdpopcornCallback(
                    "REW-1", "user-1", UserDeviceType.ANDROID, "CAMP-1", 1,
                    "캠페인", 100L, "signed", 1001, "앱", "adid", "idfa", "20260414120000"
            );

            // then
            verify(vendorCommunicationFailureHandler).handleFailure(
                    any(), any(), any(), any(), any(VendorVerificationFailedException.class), eq1100(), anyString()
            );
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        }

        private int eq1100() {
            return org.mockito.ArgumentMatchers.eq(1100);
        }

        @Test
        @DisplayName("UserNotFoundException 발생 시 3200 코드로 실패 핸들러를 호출한다")
        void shouldHandleUserNotFound() {
            given(handleOfferwallRewardUseCase.handle(any())).willThrow(new UserNotFoundException("user-99"));
            given(vendorCommunicationFailureHandler.handleFailure(any(), any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(ResponseEntity.badRequest().body("fail"));

            controller.handleAdpopcornCallback(
                    "REW-1", "user-1", UserDeviceType.ANDROID, "CAMP-1", 1,
                    "캠페인", 100L, "signed", 1001, "앱", "adid", "idfa", "20260414120000"
            );

            verify(vendorCommunicationFailureHandler).handleFailure(
                    any(), any(), any(), any(), any(UserNotFoundException.class),
                    org.mockito.ArgumentMatchers.eq(3200), anyString()
            );
        }

        @Test
        @DisplayName("DuplicateOfferwallRewardException 발생 시 3100 코드로 실패 핸들러를 호출한다")
        void shouldHandleDuplicate() {
            given(handleOfferwallRewardUseCase.handle(any())).willThrow(new DuplicateOfferwallRewardException("dup"));
            given(vendorCommunicationFailureHandler.handleFailure(any(), any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(ResponseEntity.badRequest().body("fail"));

            controller.handleAdpopcornCallback(
                    "REW-1", "user-1", UserDeviceType.ANDROID, "CAMP-1", 1,
                    "캠페인", 100L, "signed", 1001, "앱", "adid", "idfa", "20260414120000"
            );

            verify(vendorCommunicationFailureHandler).handleFailure(
                    any(), any(), any(), any(), any(DuplicateOfferwallRewardException.class),
                    org.mockito.ArgumentMatchers.eq(3100), anyString()
            );
        }
    }

    @Nested
    @DisplayName("handleTnkCallback")
    class TnkCallback {

        @BeforeEach
        void setUp() {
            given(vendorCommunicationPayloadGenerator.buildTnkPayloadJson(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).willReturn(emptyNode);
        }

        @Test
        @DisplayName("정상 처리 시 BaseResponse OK를 반환한다")
        void shouldReturnOkResponse() {
            given(handleOfferwallRewardUseCase.handle(any())).willReturn(2L);

            ResponseEntity<BaseResponse<UpdateUserPointResponse>> response = controller.handleTnkCallback(
                    "REW-2", "user-2", UserDeviceType.IOS, 1, 200L, "signed",
                    "앱", "adid", "20260414120000", BigDecimal.valueOf(1000)
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(vendorCommunicationRecorder, times(2)).record(any(), any(), any(), anyString(), any(), anyString(), any());
        }

        @Test
        @DisplayName("VendorVerificationFailedException 발생 시 핸들러 호출 후 예외를 다시 던진다")
        void shouldRethrowAfterHandleFailure() {
            given(handleOfferwallRewardUseCase.handle(any())).willThrow(new VendorVerificationFailedException("invalid"));

            assertThatThrownBy(() -> controller.handleTnkCallback(
                    "REW-2", "user-2", UserDeviceType.IOS, 1, 200L, "signed",
                    "앱", "adid", "20260414120000", BigDecimal.valueOf(1000)
            )).isInstanceOf(VendorVerificationFailedException.class);

            verify(vendorCommunicationFailureHandler).handleFailure(
                    any(), any(), any(), any(), any(VendorVerificationFailedException.class), anyInt(), anyString()
            );
        }
    }

    @Nested
    @DisplayName("handleAdiscopeCallback")
    class AdiscopeCallback {

        @BeforeEach
        void setUp() {
            given(vendorCommunicationPayloadGenerator.buildAdiscopePayloadJson(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).willReturn(emptyNode);
        }

        @Test
        @DisplayName("정상 처리 시 BaseResponse OK를 반환한다")
        void shouldReturnOkResponse() {
            given(handleOfferwallRewardUseCase.handle(any())).willReturn(3L);

            ResponseEntity<BaseResponse<UpdateUserPointResponse>> response = controller.handleAdiscopeCallback(
                    "REW-3", "user-3", UserDeviceType.ANDROID, "CAMP-3", "TYPE", "캠페인",
                    "POINT", 300L, "signed", "adid", "network"
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(vendorCommunicationRecorder, times(2)).record(any(), any(), any(), anyString(), any(), anyString(), any());
        }

        @Test
        @DisplayName("DuplicateOfferwallRewardException 발생 시 3100 코드로 핸들러 호출 후 예외 재전파한다")
        void shouldHandleDuplicateAndRethrow() {
            given(handleOfferwallRewardUseCase.handle(any())).willThrow(new DuplicateOfferwallRewardException("dup"));

            assertThatThrownBy(() -> controller.handleAdiscopeCallback(
                    "REW-3", "user-3", UserDeviceType.ANDROID, "CAMP-3", "TYPE", "캠페인",
                    "POINT", 300L, "signed", "adid", "network"
            )).isInstanceOf(DuplicateOfferwallRewardException.class);

            verify(vendorCommunicationFailureHandler).handleFailure(
                    any(), any(), any(), any(), any(DuplicateOfferwallRewardException.class),
                    org.mockito.ArgumentMatchers.eq(3100), anyString()
            );
            verify(vendorCommunicationRecorder, never()).record(any(), any(), any(), anyString(), any(), anyString(), any());
        }
    }
}
