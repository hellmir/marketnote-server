package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.AuthenticationFailedException;
import com.personal.marketnote.user.adapter.in.web.user.request.AcceptOrCancelTermsRequest;
import com.personal.marketnote.user.adapter.in.web.user.response.GetTermsResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUserTermsResponse;
import com.personal.marketnote.user.port.in.command.AcceptOrCancelTermsCommand;
import com.personal.marketnote.user.port.in.result.GetTermResult;
import com.personal.marketnote.user.port.in.result.GetTermsResult;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;
import com.personal.marketnote.user.port.in.result.UpdateUserTermResult;
import com.personal.marketnote.user.port.in.usecase.terms.GetTermsUseCase;
import com.personal.marketnote.user.port.in.usecase.terms.UpdateTermsUseCase;
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
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TermsController 약관 동의 철회")
class TermsControllerTest {

    @InjectMocks
    private TermsController controller;

    @Mock
    private GetTermsUseCase getTermsUseCase;

    @Mock
    private UpdateTermsUseCase updateTermsUseCase;

    private OAuth2AuthenticatedPrincipal principalOf(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/terms - 전체 약관 목록 조회")
    class GetAllTerms {

        @Test
        @DisplayName("전체 약관 조회 시 UseCase 결과를 응답으로 래핑하여 OK를 반환한다")
        void returnsAllTermsWrappedInResponse() {
            // given
            GetTermResult term = new GetTermResult(1L, "제 1조 서비스 이용약관 내용", true);
            when(getTermsUseCase.getAllTerms()).thenReturn(new GetTermsResult(List.of(term)));

            // when
            ResponseEntity<BaseResponse<GetTermsResponse>> response = controller.getAllTerms();

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getTermsUseCase).getAllTerms();
            verifyNoInteractions(updateTermsUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/terms/user - 회원 약관 동의 여부 조회")
    class GetUserTerms {

        @Test
        @DisplayName("인증된 회원 ID로 약관 동의 내역을 조회한다")
        void retrievesUserTermsByAuthenticatedUserId() {
            // given
            when(getTermsUseCase.getUserTerms(100L))
                    .thenReturn(new GetUserTermsResult(List.of()));

            // when
            ResponseEntity<BaseResponse<GetUserTermsResponse>> response = controller.getUserTerms(principalOf(100L));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getTermsUseCase).getUserTerms(100L);
            verifyNoInteractions(updateTermsUseCase);
        }

        @Test
        @DisplayName("인증 정보가 없으면 AuthenticationFailedException이 발생한다")
        void throwsWhenPrincipalIsNull() {
            // when & then
            assertThatThrownBy(() -> controller.getUserTerms(null))
                    .isInstanceOf(AuthenticationFailedException.class);
            verifyNoInteractions(getTermsUseCase, updateTermsUseCase);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/terms/user - 약관 동의 철회")
    class AcceptOrCancelTerms {

        @Test
        @DisplayName("약관 ID 목록을 Command로 매핑하여 UseCase에 위임한다")
        void mapsRequestToCommand() {
            // given
            AcceptOrCancelTermsRequest request = new AcceptOrCancelTermsRequest();
            ReflectionTestUtils.setField(request, "ids", List.of(1L, 2L, 3L));
            UpdateUserTermResult termResult = new UpdateUserTermResult(1L, "약관1", true, true);
            when(updateTermsUseCase.acceptOrCancelTerms(eq(100L), any(AcceptOrCancelTermsCommand.class)))
                    .thenReturn(new GetUserTermsResult(List.of(termResult)));

            // when
            ResponseEntity<BaseResponse<GetUserTermsResponse>> response = controller.acceptOrCancelTerms(
                    request, principalOf(100L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<AcceptOrCancelTermsCommand> captor = ArgumentCaptor.forClass(AcceptOrCancelTermsCommand.class);
            verify(updateTermsUseCase).acceptOrCancelTerms(eq(100L), captor.capture());
            assertThat(captor.getValue().getIds()).containsExactly(1L, 2L, 3L);
        }
    }
}
