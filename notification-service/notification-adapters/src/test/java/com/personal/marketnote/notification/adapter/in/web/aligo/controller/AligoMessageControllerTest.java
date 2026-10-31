package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoMessageListResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoMessageRecipientListResponse;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;
import com.personal.marketnote.notification.port.in.command.GetAligoMessageListCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.CancelAligoMessageUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoMessageListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoMessageRecipientListUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AligoMessageControllerTest {

    @InjectMocks
    private AligoMessageController controller;

    @Mock
    private GetAligoMessageListUseCase getAligoMessageListUseCase;

    @Mock
    private GetAligoMessageRecipientListUseCase getAligoMessageRecipientListUseCase;

    @Mock
    private CancelAligoMessageUseCase cancelAligoMessageUseCase;

    @Nested
    @DisplayName("getMessageList")
    class GetMessageList {

        @Test
        @DisplayName("page/limit/시작일/종료일 파라미터를 Command로 매핑하고 OK 응답을 반환한다")
        void shouldMapPagingAndDateRangeToCommand() {
            // given
            LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);

            GetAligoMessageListResult result = new GetAligoMessageListResult(
                    List.of(new GetAligoMessageListResult.AligoMessageSummary(
                            1L, "CODE-1", "01012345678",
                            AligoMessageType.AT, AligoMessageStatus.SENT,
                            "MID-1", 1, 0, false,
                            start, start.plusMinutes(1), start
                    )),
                    1L, 1, 1
            );
            when(getAligoMessageListUseCase.getMessageList(
                    new GetAligoMessageListCommand(2, 50, start, end)
            )).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetAligoMessageListResponse>> response =
                    controller.getMessageList(2, 50, start, end);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetAligoMessageListResponse body = response.getBody().getContent();
            assertThat(body.messages()).hasSize(1);
            assertThat(body.totalElements()).isEqualTo(1L);
            assertThat(body.currentPage()).isEqualTo(1);

            ArgumentCaptor<GetAligoMessageListCommand> captor =
                    ArgumentCaptor.forClass(GetAligoMessageListCommand.class);
            verify(getAligoMessageListUseCase).getMessageList(captor.capture());
            GetAligoMessageListCommand command = captor.getValue();
            assertThat(command.page()).isEqualTo(2);
            assertThat(command.limit()).isEqualTo(50);
            assertThat(command.startDate()).isEqualTo(start);
            assertThat(command.endDate()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("getRecipientList")
    class GetRecipientList {

        @Test
        @DisplayName("messageId로 UseCase를 호출하고 수신자 상세 목록을 반환한다")
        void shouldReturnRecipientDetails() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 20, 10, 0);
            GetAligoMessageRecipientListResult result = new GetAligoMessageRecipientListResult(
                    100L,
                    List.of(
                            new GetAligoMessageRecipientListResult.RecipientDetail(
                                    1L, "01011112222", "홍길동",
                                    "msg-1", "smid-1",
                                    AligoMessageRecipientStatus.SUCCESS,
                                    "1", "정상전송",
                                    now, now.plusSeconds(1), now.plusSeconds(2)
                            ),
                            new GetAligoMessageRecipientListResult.RecipientDetail(
                                    2L, "01033334444", "김철수",
                                    "msg-2", "smid-2",
                                    AligoMessageRecipientStatus.FAILED,
                                    "0", "실패",
                                    now, now.plusSeconds(1), now.plusSeconds(2)
                            )
                    )
            );
            when(getAligoMessageRecipientListUseCase.getRecipientList(100L)).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetAligoMessageRecipientListResponse>> response =
                    controller.getRecipientList(100L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetAligoMessageRecipientListResponse body = response.getBody().getContent();
            assertThat(body.messageId()).isEqualTo(100L);
            assertThat(body.recipients()).hasSize(2);
            assertThat(body.recipients().get(0).recipientStatus()).isEqualTo(AligoMessageRecipientStatus.SUCCESS);
            verify(getAligoMessageRecipientListUseCase).getRecipientList(100L);
        }
    }

    @Nested
    @DisplayName("cancelMessage")
    class CancelMessage {

        @Test
        @DisplayName("messageId로 취소 UseCase를 호출하고 OK 응답을 반환한다")
        void shouldDelegateCancelToUseCase() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.cancelMessage(100L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cancelAligoMessageUseCase).cancelMessage(100L);
        }
    }

    @Nested
    @DisplayName("ADMIN_POINTCUT 권한 부착 검증")
    class AdminPointcutAuthorization {

        @Test
        @DisplayName("모든 public 엔드포인트에 관리자 전용 @PreAuthorize가 부착되어 있다")
        void shouldHaveAdminPreAuthorizeOnAllPublicEndpoints() {
            String expected = "isAuthenticated() and hasRole('ROLE_ADMIN')";
            Method[] methods = AligoMessageController.class.getDeclaredMethods();

            for (Method method : methods) {
                if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertThat(annotation)
                        .as("메서드 %s 에 @PreAuthorize가 부착되어야 한다", method.getName())
                        .isNotNull();
                assertThat(annotation.value())
                        .as("메서드 %s 의 @PreAuthorize 값은 ADMIN_POINTCUT이어야 한다", method.getName())
                        .isEqualTo(expected);
            }
        }
    }

    @Nested
    @DisplayName("Bean Validation 부착 검증")
    class BeanValidationAnnotations {

        @Test
        @DisplayName("getMessageList의 page 파라미터에 @Min(1)이 부착되어 있다")
        void getMessageListPageHasMin1() throws NoSuchMethodException {
            Method method = AligoMessageController.class.getMethod(
                    "getMessageList", int.class, int.class, LocalDateTime.class, LocalDateTime.class
            );
            Parameter pageParam = method.getParameters()[0];
            Min min = findAnnotation(pageParam, Min.class);
            assertThat(min).isNotNull();
            assertThat(min.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getMessageList의 limit 파라미터에 @Min(1), @Max(100)이 부착되어 있다")
        void getMessageListLimitHasMinAndMax() throws NoSuchMethodException {
            Method method = AligoMessageController.class.getMethod(
                    "getMessageList", int.class, int.class, LocalDateTime.class, LocalDateTime.class
            );
            Parameter limitParam = method.getParameters()[1];
            Min min = findAnnotation(limitParam, Min.class);
            Max max = findAnnotation(limitParam, Max.class);
            assertThat(min).isNotNull();
            assertThat(min.value()).isEqualTo(1L);
            assertThat(max).isNotNull();
            assertThat(max.value()).isEqualTo(100L);
        }

        private <A extends Annotation> A findAnnotation(Parameter parameter, Class<A> annotationClass) {
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotationClass.isInstance(annotation)) {
                    return annotationClass.cast(annotation);
                }
            }
            return null;
        }
    }
}
