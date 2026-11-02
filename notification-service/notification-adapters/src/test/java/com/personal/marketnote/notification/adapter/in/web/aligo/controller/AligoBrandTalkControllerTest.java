package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.SendBrandTalkRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.SendBrandTalkResponse;
import com.personal.marketnote.notification.port.in.command.SendBrandTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendBrandTalkResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendBrandTalkUseCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AligoBrandTalkControllerTest {

    @InjectMocks
    private AligoBrandTalkController controller;

    @Mock
    private SendBrandTalkUseCase sendBrandTalkUseCase;

    private SendBrandTalkRequest buildRequest(
            String kakaoTarget,
            SendBrandTalkRequest.FailoverRequest failover
    ) {
        return new SendBrandTalkRequest(
                "sk-1",
                "01012345678",
                "CODE-1",
                kakaoTarget,
                false,
                List.of(
                        new SendBrandTalkRequest.RecipientRequest("01011112222", Map.of("name", "홍길동")),
                        new SendBrandTalkRequest.RecipientRequest("01033334444", Map.of())
                ),
                failover
        );
    }

    private SendBrandTalkCommand captureCommand() {
        ArgumentCaptor<SendBrandTalkCommand> captor =
                ArgumentCaptor.forClass(SendBrandTalkCommand.class);
        verify(sendBrandTalkUseCase).sendBrandTalk(captor.capture());
        return captor.getValue();
    }

    private void stubUseCase() {
        when(sendBrandTalkUseCase.sendBrandTalk(any()))
                .thenReturn(new SendBrandTalkResult(1L, "MID-1", 2, 0, "FT", 15, 30));
    }

    @Nested
    @DisplayName("sendBrandTalk - kakao target 매핑")
    class KakaoTargetMapping {

        @Test
        @DisplayName("kakaoTarget과 수신자 목록을 Command로 매핑하고 OK 응답을 반환한다")
        void shouldMapKakaoTargetAndRecipients() {
            // given
            stubUseCase();

            // when
            ResponseEntity<BaseResponse<SendBrandTalkResponse>> response =
                    controller.sendBrandTalk(buildRequest("M", null));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            SendBrandTalkCommand command = captureCommand();
            assertThat(command.kakaoTarget()).isEqualTo("M");
            assertThat(command.senderKey()).isEqualTo("sk-1");
            assertThat(command.sender()).isEqualTo("01012345678");
            assertThat(command.templateCode()).isEqualTo("CODE-1");
            assertThat(command.advertYn()).isFalse();
            assertThat(command.recipients()).hasSize(2);
            assertThat(command.recipients().get(0).receiver()).isEqualTo("01011112222");
            assertThat(command.recipients().get(0).variables()).containsEntry("name", "홍길동");
            assertThat(command.recipients().get(1).variables()).isEmpty();
        }

        @Test
        @DisplayName("kakaoTarget이 I(알림톡 대상)이면 Command에 그대로 전달한다")
        void shouldPassInformationalKakaoTargetAsIs() {
            // given
            stubUseCase();

            // when
            controller.sendBrandTalk(buildRequest("I", null));

            // then
            SendBrandTalkCommand command = captureCommand();
            assertThat(command.kakaoTarget()).isEqualTo("I");
        }
    }

    @Nested
    @DisplayName("sendBrandTalk - failover 타입 매핑")
    class FailoverTypeMapping {

        @Test
        @DisplayName("failover가 null이면 Command.failover는 null이다")
        void shouldMapNullFailoverToNull() {
            // given
            stubUseCase();

            // when
            controller.sendBrandTalk(buildRequest("M", null));

            // then
            assertThat(captureCommand().failover()).isNull();
        }

        @Test
        @DisplayName("failover의 failoverType(SMS/LMS/MMS)이 Command.failoverType으로 전달된다")
        void shouldMapFailoverTypeToCommand() {
            // given
            stubUseCase();
            SendBrandTalkRequest.FailoverRequest failover =
                    new SendBrandTalkRequest.FailoverRequest(true, "제목", "LMS");

            // when
            controller.sendBrandTalk(buildRequest("M", failover));

            // then
            SendBrandTalkCommand command = captureCommand();
            assertThat(command.failover()).isNotNull();
            assertThat(command.failover().enabled()).isTrue();
            assertThat(command.failover().subject()).isEqualTo("제목");
            assertThat(command.failover().failoverType()).isEqualTo("LMS");
        }

        @Test
        @DisplayName("failover가 있으면 mmsImages/mmsImageFileNames는 빈 리스트로 초기화된다")
        void shouldInitializeMmsListsAsEmpty() {
            // given
            stubUseCase();
            SendBrandTalkRequest.FailoverRequest failover =
                    new SendBrandTalkRequest.FailoverRequest(true, "제목", "SMS");

            // when
            controller.sendBrandTalk(buildRequest("M", failover));

            // then
            SendBrandTalkCommand command = captureCommand();
            assertThat(command.failover().mmsImages()).isEmpty();
            assertThat(command.failover().mmsImageFileNames()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ADMIN_POINTCUT 권한 부착 검증")
    class AdminPointcutAuthorization {

        @Test
        @DisplayName("sendBrandTalk 메서드에 관리자 전용 @PreAuthorize가 부착되어 있다")
        void shouldHaveAdminPreAuthorizeOnSendBrandTalk() throws NoSuchMethodException {
            Method method = AligoBrandTalkController.class.getMethod(
                    "sendBrandTalk", SendBrandTalkRequest.class
            );
            PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.value()).isEqualTo("isAuthenticated() and hasRole('ROLE_ADMIN')");
        }
    }

    @Nested
    @DisplayName("Bean Validation 부착 검증")
    class BeanValidationAnnotations {

        @Test
        @DisplayName("SendBrandTalkRequest 필수 필드에 @NotBlank/@NotNull/@Size가 부착되어 있다")
        void requestHasValidationOnMandatoryFields() throws NoSuchFieldException {
            assertNotBlankPresent(SendBrandTalkRequest.class, "senderKey");
            assertNotBlankPresent(SendBrandTalkRequest.class, "sender");
            assertNotBlankPresent(SendBrandTalkRequest.class, "templateCode");
            assertNotBlankPresent(SendBrandTalkRequest.class, "kakaoTarget");

            Field recipients = SendBrandTalkRequest.class.getDeclaredField("recipients");
            assertThat(recipients.getAnnotation(NotNull.class)).isNotNull();
            Size size = recipients.getAnnotation(Size.class);
            assertThat(size).isNotNull();
            assertThat(size.min()).isEqualTo(1);
            assertThat(size.max()).isEqualTo(500);
        }

        @Test
        @DisplayName("RecipientRequest.receiver 필드에 @NotBlank가 부착되어 있다")
        void recipientHasNotBlankOnReceiver() throws NoSuchFieldException {
            Field receiver = SendBrandTalkRequest.RecipientRequest.class.getDeclaredField("receiver");
            assertThat(receiver.getAnnotation(NotBlank.class)).isNotNull();
        }

        private void assertNotBlankPresent(Class<?> recordClass, String fieldName) {
            try {
                Field field = recordClass.getDeclaredField(fieldName);
                assertThat(field.getAnnotation(NotBlank.class))
                        .as("%s.%s 필드에 @NotBlank가 부착되어야 한다", recordClass.getSimpleName(), fieldName)
                        .isNotNull();
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                        recordClass.getSimpleName() + "에 " + fieldName + " 필드가 없다", e
                );
            }
        }
    }
}
