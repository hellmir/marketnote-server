package com.personal.marketnote.community.adapter.in.web.like.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.community.adapter.in.web.like.request.UpsertLikeRequest;
import com.personal.marketnote.community.adapter.in.web.like.response.UpsertLikeResponse;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.port.in.command.like.UpsertLikeCommand;
import com.personal.marketnote.community.port.in.result.like.UpsertLikeResult;
import com.personal.marketnote.community.port.in.usecase.like.UpsertLikeUseCase;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeController")
class LikeControllerTest {

    @Mock
    private UpsertLikeUseCase upsertLikeUseCase;

    @InjectMocks
    private LikeController controller;

    @Nested
    @DisplayName("upsertLike")
    class UpsertLike {

        @Test
        @DisplayName("신규 좋아요 등록이면 201 CREATED와 isLiked=true를 반환한다")
        void returnsCreatedWhenNewLike() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(10L);
            UpsertLikeRequest request = new UpsertLikeRequest(LikeTargetType.REVIEW, 100L, true);
            when(upsertLikeUseCase.upsertLike(any(UpsertLikeCommand.class)))
                    .thenReturn(new UpsertLikeResult(true, true));

            ResponseEntity<BaseResponse<UpsertLikeResponse>> response = controller.upsertLike(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isLiked()).isTrue();
        }

        @Test
        @DisplayName("기존 좋아요 토글(취소)이면 200 OK와 isLiked=false를 반환한다")
        void returnsOkWhenRevertingLike() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(10L);
            UpsertLikeRequest request = new UpsertLikeRequest(LikeTargetType.BOARD, 200L, false);
            when(upsertLikeUseCase.upsertLike(any(UpsertLikeCommand.class)))
                    .thenReturn(new UpsertLikeResult(false, false));

            ResponseEntity<BaseResponse<UpsertLikeResponse>> response = controller.upsertLike(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().isLiked()).isFalse();
        }

        @Test
        @DisplayName("요청과 userId로 Command를 조립해 UseCase에 위임한다")
        void delegatesWithMappedCommand() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal(42L);
            UpsertLikeRequest request = new UpsertLikeRequest(LikeTargetType.REVIEW, 300L, true);
            ArgumentCaptor<UpsertLikeCommand> captor = ArgumentCaptor.forClass(UpsertLikeCommand.class);
            when(upsertLikeUseCase.upsertLike(captor.capture()))
                    .thenReturn(new UpsertLikeResult(true, true));

            controller.upsertLike(request, principal);

            verify(upsertLikeUseCase).upsertLike(captor.getValue());
            UpsertLikeCommand command = captor.getValue();
            assertThat(command.targetType()).isEqualTo(LikeTargetType.REVIEW);
            assertThat(command.targetId()).isEqualTo(300L);
            assertThat(command.isLiked()).isTrue();
            assertThat(command.userId()).isEqualTo(42L);
        }
    }

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }
}
