package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.SendAligoAlimTalkRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.SendAligoAlimTalkResponse;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendAligoAlimTalkResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
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
class AligoAlimTalkControllerTest {

    @InjectMocks
    private AligoAlimTalkController controller;

    @Mock
    private SendAligoAlimTalkUseCase sendAligoAlimTalkUseCase;

    private SendAligoAlimTalkRequest buildRequest(
            Map<String, String> variables,
            SendAligoAlimTalkRequest.FailoverRequest failover
    ) {
        return new SendAligoAlimTalkRequest(
                "CODE-1",
                "01012345678",
                variables,
                List.of(
                        new SendAligoAlimTalkRequest.RecipientRequest("01011112222", "홍길동"),
                        new SendAligoAlimTalkRequest.RecipientRequest("01033334444", "김철수")
                ),
                failover
        );
    }

    private SendAligoAlimTalkCommand captureCommand() {
        ArgumentCaptor<SendAligoAlimTalkCommand> captor =
                ArgumentCaptor.forClass(SendAligoAlimTalkCommand.class);
        verify(sendAligoAlimTalkUseCase).sendAlimTalk(captor.capture());
        return captor.getValue();
    }

    private void stubUseCase() {
        when(sendAligoAlimTalkUseCase.sendAlimTalk(any()))
                .thenReturn(new SendAligoAlimTalkResult(1L, "MID-1", 2, 0));
    }

    @Nested
    @DisplayName("sendAlimTalk - 수신자/failover 매핑")
    class SendAlimTalkRecipientsAndFailover {

        @Test
        @DisplayName("수신자 목록과 failover를 Command로 매핑하고 OK 응답을 반환한다")
        void shouldMapRecipientsAndFailoverToCommand() {
            // given
            stubUseCase();
            SendAligoAlimTalkRequest.FailoverRequest failover =
                    new SendAligoAlimTalkRequest.FailoverRequest(true, "제목", "대체발송 본문");

            // when
            ResponseEntity<BaseResponse<SendAligoAlimTalkResponse>> response =
                    controller.sendAlimTalk(buildRequest(Map.of("name", "홍길동"), failover));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            SendAligoAlimTalkCommand command = captureCommand();
            assertThat(command.templateCode()).isEqualTo("CODE-1");
            assertThat(command.sender()).isEqualTo("01012345678");
            assertThat(command.recipients()).hasSize(2);
            assertThat(command.recipients().get(0).receiver()).isEqualTo("01011112222");
            assertThat(command.recipients().get(0).recvName()).isEqualTo("홍길동");
            assertThat(command.recipients().get(1).receiver()).isEqualTo("01033334444");
            assertThat(command.failover()).isNotNull();
            assertThat(command.failover().enabled()).isTrue();
            assertThat(command.failover().subject()).isEqualTo("제목");
            assertThat(command.failover().body()).isEqualTo("대체발송 본문");
        }
    }

    @Nested
    @DisplayName("sendAlimTalk - variables NULL/빈 map 디폴트 처리")
    class VariablesDefault {

        @Test
        @DisplayName("variables가 null이면 Command에 빈 map으로 전달한다")
        void shouldDefaultNullVariablesToEmptyMap() {
            // given
            stubUseCase();

            // when
            controller.sendAlimTalk(buildRequest(null, null));

            // then
            SendAligoAlimTalkCommand command = captureCommand();
            assertThat(command.variables()).isNotNull();
            assertThat(command.variables()).isEmpty();
        }

        @Test
        @DisplayName("variables가 빈 map이면 Command에 빈 map으로 그대로 전달한다")
        void shouldPassEmptyVariablesAsIs() {
            // given
            stubUseCase();

            // when
            controller.sendAlimTalk(buildRequest(Map.of(), null));

            // then
            SendAligoAlimTalkCommand command = captureCommand();
            assertThat(command.variables()).isEmpty();
        }

        @Test
        @DisplayName("variables에 값이 있으면 Command에 그대로 전달한다")
        void shouldPassNonEmptyVariablesAsIs() {
            // given
            stubUseCase();

            // when
            controller.sendAlimTalk(buildRequest(Map.of("name", "홍길동", "amount", "1000"), null));

            // then
            SendAligoAlimTalkCommand command = captureCommand();
            assertThat(command.variables())
                    .containsEntry("name", "홍길동")
                    .containsEntry("amount", "1000");
        }
    }

    @Nested
    @DisplayName("sendAlimTalk - failover NULL 처리")
    class FailoverNullMapping {

        @Test
        @DisplayName("failover가 null이면 Command.failover는 null이다")
        void shouldMapNullFailoverToNull() {
            // given
            stubUseCase();

            // when
            controller.sendAlimTalk(buildRequest(Map.of(), null));

            // then
            SendAligoAlimTalkCommand command = captureCommand();
            assertThat(command.failover()).isNull();
        }
    }

    @Nested
    @DisplayName("ADMIN_POINTCUT 권한 부착 검증")
    class AdminPointcutAuthorization {

        @Test
        @DisplayName("sendAlimTalk 메서드에 관리자 전용 @PreAuthorize가 부착되어 있다")
        void shouldHaveAdminPreAuthorizeOnSendAlimTalk() throws NoSuchMethodException {
            Method method = AligoAlimTalkController.class.getMethod(
                    "sendAlimTalk", SendAligoAlimTalkRequest.class
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
        @DisplayName("SendAligoAlimTalkRequest 필수 필드에 @NotBlank/@NotNull/@Size가 부착되어 있다")
        void requestHasValidationOnMandatoryFields() throws NoSuchFieldException {
            Field templateCode = SendAligoAlimTalkRequest.class.getDeclaredField("templateCode");
            Field sender = SendAligoAlimTalkRequest.class.getDeclaredField("sender");
            Field recipients = SendAligoAlimTalkRequest.class.getDeclaredField("recipients");
            assertThat(templateCode.getAnnotation(NotBlank.class)).isNotNull();
            assertThat(sender.getAnnotation(NotBlank.class)).isNotNull();
            assertThat(recipients.getAnnotation(NotNull.class)).isNotNull();
            Size size = recipients.getAnnotation(Size.class);
            assertThat(size).isNotNull();
            assertThat(size.min()).isEqualTo(1);
            assertThat(size.max()).isEqualTo(500);
        }

        @Test
        @DisplayName("RecipientRequest.receiver 필드에 @NotBlank가 부착되어 있다")
        void recipientHasNotBlankOnReceiver() throws NoSuchFieldException {
            Field receiver = SendAligoAlimTalkRequest.RecipientRequest.class.getDeclaredField("receiver");
            assertThat(receiver.getAnnotation(NotBlank.class)).isNotNull();
        }
    }
}
