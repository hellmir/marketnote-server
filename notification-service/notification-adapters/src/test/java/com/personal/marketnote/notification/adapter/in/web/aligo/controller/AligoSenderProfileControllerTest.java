package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.AuthenticateAligoSenderProfileRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterAligoSenderProfileRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.AuthenticateAligoSenderProfileResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoSenderProfileListResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterAligoSenderProfileResponse;
import com.personal.marketnote.notification.port.in.command.AuthenticateAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.command.RegisterAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.AuthenticateAligoSenderProfileResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoSenderProfileListResult;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoSenderProfileResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.AuthenticateAligoSenderProfileUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoSenderProfileListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterAligoSenderProfileUseCase;
import jakarta.validation.constraints.NotBlank;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AligoSenderProfileControllerTest {

    @InjectMocks
    private AligoSenderProfileController controller;

    @Mock
    private AuthenticateAligoSenderProfileUseCase authenticateAligoSenderProfileUseCase;

    @Mock
    private RegisterAligoSenderProfileUseCase registerAligoSenderProfileUseCase;

    @Mock
    private GetAligoSenderProfileListUseCase getAligoSenderProfileListUseCase;

    @Nested
    @DisplayName("authenticateSenderProfile")
    class AuthenticateSenderProfile {

        @Test
        @DisplayName("인증 요청을 Command로 매핑하고 CREATED 응답을 반환한다")
        void shouldMapRequestAndReturnCreated() {
            // given
            when(authenticateAligoSenderProfileUseCase.authenticateSenderProfile(
                    new AuthenticateAligoSenderProfileCommand("@pluschannel", "01012345678")
            )).thenReturn(new AuthenticateAligoSenderProfileResult("sk-1", "@pluschannel"));

            AuthenticateAligoSenderProfileRequest request =
                    new AuthenticateAligoSenderProfileRequest("@pluschannel", "01012345678");

            // when
            ResponseEntity<BaseResponse<AuthenticateAligoSenderProfileResponse>> response =
                    controller.authenticateSenderProfile(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            AuthenticateAligoSenderProfileResponse body = response.getBody().getContent();
            assertThat(body.senderKey()).isEqualTo("sk-1");
            assertThat(body.plusId()).isEqualTo("@pluschannel");
            verify(authenticateAligoSenderProfileUseCase).authenticateSenderProfile(
                    new AuthenticateAligoSenderProfileCommand("@pluschannel", "01012345678")
            );
        }
    }

    @Nested
    @DisplayName("registerSenderProfile")
    class RegisterSenderProfile {

        @Test
        @DisplayName("senderKey와 categoryCode를 Command로 전달하고 CREATED 응답을 반환한다")
        void shouldDelegateCategoryCodeAndReturnCreated() {
            // given
            when(registerAligoSenderProfileUseCase.registerSenderProfile(
                    new RegisterAligoSenderProfileCommand("sk-1", "CAT-1")
            )).thenReturn(new RegisterAligoSenderProfileResult(1L, "sk-1", "CAT-1"));

            RegisterAligoSenderProfileRequest request =
                    new RegisterAligoSenderProfileRequest("sk-1", "CAT-1");

            // when
            ResponseEntity<BaseResponse<RegisterAligoSenderProfileResponse>> response =
                    controller.registerSenderProfile(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterAligoSenderProfileCommand> captor =
                    ArgumentCaptor.forClass(RegisterAligoSenderProfileCommand.class);
            verify(registerAligoSenderProfileUseCase).registerSenderProfile(captor.capture());
            RegisterAligoSenderProfileCommand command = captor.getValue();
            assertThat(command.senderKey()).isEqualTo("sk-1");
            assertThat(command.categoryCode()).isEqualTo("CAT-1");
        }
    }

    @Nested
    @DisplayName("getSenderProfileList")
    class GetSenderProfileList {

        @Test
        @DisplayName("UseCase가 반환한 전체 발신프로필 목록을 OK 응답으로 반환한다")
        void shouldReturnAllProfiles() {
            // given
            GetAligoSenderProfileListResult result = new GetAligoSenderProfileListResult(List.of(
                    new GetAligoSenderProfileListResult.AligoSenderProfileResult(
                            1L, "sk-1", "@channel1", "채널1",
                            "CAT-1", "APR", "A", true
                    ),
                    new GetAligoSenderProfileListResult.AligoSenderProfileResult(
                            2L, "sk-2", "@channel2", "채널2",
                            "CAT-2", "REG", "W", false
                    )
            ));
            when(getAligoSenderProfileListUseCase.getSenderProfileList()).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetAligoSenderProfileListResponse>> response =
                    controller.getSenderProfileList();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            GetAligoSenderProfileListResponse body = response.getBody().getContent();
            assertThat(body.profiles()).hasSize(2);
            assertThat(body.profiles().get(0).senderKey()).isEqualTo("sk-1");
            assertThat(body.profiles().get(1).plusId()).isEqualTo("@channel2");
            verify(getAligoSenderProfileListUseCase).getSenderProfileList();
        }
    }

    @Nested
    @DisplayName("ADMIN_POINTCUT 권한 부착 검증")
    class AdminPointcutAuthorization {

        @Test
        @DisplayName("모든 public 엔드포인트에 관리자 전용 @PreAuthorize가 부착되어 있다")
        void shouldHaveAdminPreAuthorizeOnAllPublicEndpoints() {
            String expected = "isAuthenticated() and hasRole('ROLE_ADMIN')";
            Method[] methods = AligoSenderProfileController.class.getDeclaredMethods();

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
        @DisplayName("AuthenticateAligoSenderProfileRequest 필수 필드에 @NotBlank가 부착되어 있다")
        void authenticateRequestHasNotBlank() {
            assertNotBlankPresent(AuthenticateAligoSenderProfileRequest.class, "plusId");
            assertNotBlankPresent(AuthenticateAligoSenderProfileRequest.class, "phoneNumber");
        }

        @Test
        @DisplayName("RegisterAligoSenderProfileRequest 필수 필드에 @NotBlank가 부착되어 있다")
        void registerRequestHasNotBlank() {
            assertNotBlankPresent(RegisterAligoSenderProfileRequest.class, "senderKey");
            assertNotBlankPresent(RegisterAligoSenderProfileRequest.class, "categoryCode");
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
