package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.DeleteAligoTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterAligoTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.UpdateAligoTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoTemplateDetailResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoTemplateListResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterAligoTemplateResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.UpdateAligoTemplateResponse;
import com.personal.marketnote.notification.port.in.command.DeleteAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.command.RegisterAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateDetailResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateListResult;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoTemplateResult;
import com.personal.marketnote.notification.port.in.result.aligo.RequestAligoTemplateInspectionResult;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.DeleteAligoTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoTemplateByCodeUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoTemplateListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterAligoTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RequestAligoTemplateInspectionUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.UpdateAligoTemplateUseCase;
import jakarta.validation.constraints.NotBlank;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AligoTemplateControllerTest {

    @InjectMocks
    private AligoTemplateController controller;

    @Mock
    private RegisterAligoTemplateUseCase registerAligoTemplateUseCase;

    @Mock
    private UpdateAligoTemplateUseCase updateAligoTemplateUseCase;

    @Mock
    private DeleteAligoTemplateUseCase deleteAligoTemplateUseCase;

    @Mock
    private GetAligoTemplateListUseCase getAligoTemplateListUseCase;

    @Mock
    private GetAligoTemplateByCodeUseCase getAligoTemplateByCodeUseCase;

    @Mock
    private RequestAligoTemplateInspectionUseCase requestAligoTemplateInspectionUseCase;

    private RegisterAligoTemplateRequest buildRegisterRequest() {
        return new RegisterAligoTemplateRequest(
                "sk-1", "CODE-1", "템플릿명", "본문내용",
                "제목", "부제목", "BA", "NONE",
                false, false, null, null,
                List.of(new RegisterAligoTemplateRequest.ButtonRequest(
                        1, "링크 버튼", "WL", "https://m.example.com",
                        "https://pc.example.com", null, null
                ))
        );
    }

    private UpdateAligoTemplateRequest buildUpdateRequest() {
        return new UpdateAligoTemplateRequest(
                "sk-1", "템플릿명 수정", "본문 수정",
                "제목 수정", "부제목 수정",
                List.of()
        );
    }

    @Nested
    @DisplayName("getTemplateList")
    class GetTemplateList {

        @Test
        @DisplayName("senderKey 파라미터로 UseCase를 호출하고 OK 응답을 반환한다")
        void shouldReturnTemplateListAndDelegateWithSenderKey() {
            // given
            GetAligoTemplateListResult result = new GetAligoTemplateListResult(List.of(
                    new GetAligoTemplateListResult.AligoTemplateResult(
                            1L, "CODE-1", "템플릿명", "BA",
                            "APR", "A", "정상"
                    ),
                    new GetAligoTemplateListResult.AligoTemplateResult(
                            2L, "CODE-2", "템플릿명2", "BA",
                            "REG", "W", null
                    )
            ));
            when(getAligoTemplateListUseCase.getTemplateList("sk-1")).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetAligoTemplateListResponse>> response =
                    controller.getTemplateList("sk-1");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().templates()).hasSize(2);
            assertThat(response.getBody().getContent().templates().get(0).templateCode()).isEqualTo("CODE-1");
            verify(getAligoTemplateListUseCase).getTemplateList("sk-1");
        }
    }

    @Nested
    @DisplayName("getTemplateDetail")
    class GetTemplateDetail {

        @Test
        @DisplayName("templateCode로 UseCase를 호출하고 템플릿+버튼 상세를 반환한다")
        void shouldReturnTemplateDetailWithButtons() {
            // given
            GetAligoTemplateDetailResult result = new GetAligoTemplateDetailResult(
                    1L, "sk-1", "CODE-1", "템플릿명",
                    "본문", "제목", "부제목", "BA", "NONE",
                    false, false, null, null,
                    "APR", "A", "정상",
                    List.of(new GetAligoTemplateDetailResult.ButtonResult(
                            1, "버튼명", "WL",
                            "mo-link", "pc-link", "ios-link", "and-link"
                    ))
            );
            when(getAligoTemplateByCodeUseCase.getTemplateByCode("CODE-1")).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetAligoTemplateDetailResponse>> response =
                    controller.getTemplateDetail("CODE-1");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetAligoTemplateDetailResponse body = response.getBody().getContent();
            assertThat(body.templateCode()).isEqualTo("CODE-1");
            assertThat(body.buttons()).hasSize(1);
            assertThat(body.buttons().get(0).name()).isEqualTo("버튼명");
            verify(getAligoTemplateByCodeUseCase).getTemplateByCode("CODE-1");
        }
    }

    @Nested
    @DisplayName("registerTemplate")
    class RegisterTemplate {

        @Test
        @DisplayName("요청 필드를 Command로 매핑하고 CREATED 응답을 반환한다")
        void shouldMapRequestToCommandAndReturnCreated() {
            // given
            when(registerAligoTemplateUseCase.registerTemplate(any()))
                    .thenReturn(new RegisterAligoTemplateResult(1L, "CODE-1", "템플릿명"));

            // when
            ResponseEntity<BaseResponse<RegisterAligoTemplateResponse>> response =
                    controller.registerTemplate(buildRegisterRequest());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterAligoTemplateCommand> captor =
                    ArgumentCaptor.forClass(RegisterAligoTemplateCommand.class);
            verify(registerAligoTemplateUseCase).registerTemplate(captor.capture());
            RegisterAligoTemplateCommand command = captor.getValue();
            assertThat(command.senderKey()).isEqualTo("sk-1");
            assertThat(command.templateCode()).isEqualTo("CODE-1");
            assertThat(command.templateName()).isEqualTo("템플릿명");
            assertThat(command.templateContent()).isEqualTo("본문내용");
            assertThat(command.buttons()).hasSize(1);
            assertThat(command.buttons().get(0).name()).isEqualTo("링크 버튼");
        }

        @Test
        @DisplayName("buttons가 null이면 Command에 빈 리스트로 전달한다")
        void shouldMapNullButtonsToEmptyList() {
            // given
            when(registerAligoTemplateUseCase.registerTemplate(any()))
                    .thenReturn(new RegisterAligoTemplateResult(1L, "CODE-1", "템플릿명"));
            RegisterAligoTemplateRequest request = new RegisterAligoTemplateRequest(
                    "sk-1", "CODE-1", "템플릿명", "본문내용",
                    null, null, "BA", "NONE",
                    false, false, null, null,
                    null
            );

            // when
            controller.registerTemplate(request);

            // then
            ArgumentCaptor<RegisterAligoTemplateCommand> captor =
                    ArgumentCaptor.forClass(RegisterAligoTemplateCommand.class);
            verify(registerAligoTemplateUseCase).registerTemplate(captor.capture());
            assertThat(captor.getValue().buttons()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateTemplate")
    class UpdateTemplate {

        @Test
        @DisplayName("templateCode 경로변수와 요청 본문을 Command로 매핑하고 OK 응답을 반환한다")
        void shouldMapPathAndBodyToCommand() {
            // given
            when(updateAligoTemplateUseCase.updateTemplate(any()))
                    .thenReturn(new UpdateAligoTemplateResult(1L, "CODE-1", "템플릿명 수정"));

            // when
            ResponseEntity<BaseResponse<UpdateAligoTemplateResponse>> response =
                    controller.updateTemplate("CODE-1", buildUpdateRequest());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdateAligoTemplateCommand> captor =
                    ArgumentCaptor.forClass(UpdateAligoTemplateCommand.class);
            verify(updateAligoTemplateUseCase).updateTemplate(captor.capture());
            UpdateAligoTemplateCommand command = captor.getValue();
            assertThat(command.templateCode()).isEqualTo("CODE-1");
            assertThat(command.senderKey()).isEqualTo("sk-1");
            assertThat(command.templateName()).isEqualTo("템플릿명 수정");
            assertThat(command.templateContent()).isEqualTo("본문 수정");
        }
    }

    @Nested
    @DisplayName("requestInspection")
    class RequestInspection {

        @Test
        @DisplayName("templateCode로 검수요청 UseCase를 호출하고 OK 응답을 반환한다")
        void shouldCallInspectionUseCaseAndReturnOk() {
            // given
            when(requestAligoTemplateInspectionUseCase.requestInspection("CODE-1"))
                    .thenReturn(new RequestAligoTemplateInspectionResult("CODE-1", "REG"));

            // when
            ResponseEntity<BaseResponse<RequestAligoTemplateInspectionResult>> response =
                    controller.requestInspection("CODE-1");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().inspStatus()).isEqualTo("REG");
            verify(requestAligoTemplateInspectionUseCase).requestInspection("CODE-1");
        }
    }

    @Nested
    @DisplayName("deleteTemplate")
    class DeleteTemplate {

        @Test
        @DisplayName("삭제 요청을 Command로 매핑하고 UseCase를 호출한다")
        void shouldDelegateToDeleteUseCase() {
            // given
            DeleteAligoTemplateRequest request = new DeleteAligoTemplateRequest("sk-1");

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.deleteTemplate("CODE-1", request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<DeleteAligoTemplateCommand> captor =
                    ArgumentCaptor.forClass(DeleteAligoTemplateCommand.class);
            verify(deleteAligoTemplateUseCase).deleteTemplate(captor.capture());
            DeleteAligoTemplateCommand command = captor.getValue();
            assertThat(command.templateCode()).isEqualTo("CODE-1");
            assertThat(command.senderKey()).isEqualTo("sk-1");
        }
    }

    @Nested
    @DisplayName("ADMIN_POINTCUT 권한 부착 검증")
    class AdminPointcutAuthorization {

        @Test
        @DisplayName("모든 public 엔드포인트에 관리자 전용 @PreAuthorize가 부착되어 있다")
        void shouldHaveAdminPreAuthorizeOnAllPublicEndpoints() {
            String expected = "isAuthenticated() and hasRole('ROLE_ADMIN')";
            Method[] methods = AligoTemplateController.class.getDeclaredMethods();

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
        @DisplayName("RegisterAligoTemplateRequest 필수 필드에 @NotBlank가 부착되어 있다")
        void registerRequestHasNotBlankOnMandatoryFields() {
            assertNotBlankPresent(RegisterAligoTemplateRequest.class, "senderKey");
            assertNotBlankPresent(RegisterAligoTemplateRequest.class, "templateCode");
            assertNotBlankPresent(RegisterAligoTemplateRequest.class, "templateName");
            assertNotBlankPresent(RegisterAligoTemplateRequest.class, "templateContent");
            assertNotBlankPresent(RegisterAligoTemplateRequest.class, "templateType");
            assertNotBlankPresent(RegisterAligoTemplateRequest.class, "templateEmType");
        }

        @Test
        @DisplayName("RegisterAligoTemplateRequest.buttons 필드에 @Size(max=5)가 부착되어 있다")
        void registerRequestButtonsHasSizeMax5() throws NoSuchFieldException {
            Field buttons = RegisterAligoTemplateRequest.class.getDeclaredField("buttons");
            Size size = buttons.getAnnotation(Size.class);
            assertThat(size).isNotNull();
            assertThat(size.max()).isEqualTo(5);
        }

        @Test
        @DisplayName("UpdateAligoTemplateRequest 필수 필드에 @NotBlank가 부착되어 있다")
        void updateRequestHasNotBlankOnMandatoryFields() {
            assertNotBlankPresent(UpdateAligoTemplateRequest.class, "senderKey");
            assertNotBlankPresent(UpdateAligoTemplateRequest.class, "templateName");
            assertNotBlankPresent(UpdateAligoTemplateRequest.class, "templateContent");
        }

        @Test
        @DisplayName("DeleteAligoTemplateRequest senderKey 필드에 @NotBlank가 부착되어 있다")
        void deleteRequestHasNotBlankOnSenderKey() {
            assertNotBlankPresent(DeleteAligoTemplateRequest.class, "senderKey");
        }

        private void assertNotBlankPresent(Class<?> recordClass, String fieldName) {
            try {
                Field field = recordClass.getDeclaredField(fieldName);
                NotBlank annotation = field.getAnnotation(NotBlank.class);
                assertThat(annotation)
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
