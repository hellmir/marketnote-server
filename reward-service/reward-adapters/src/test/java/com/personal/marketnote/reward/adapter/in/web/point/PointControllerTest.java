package com.personal.marketnote.reward.adapter.in.web.point;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.point.response.GetUserPointHistoryResponse;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;
import com.personal.marketnote.reward.port.in.usecase.point.CancelPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ClaimReferralBonusUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ConfirmPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetReferralStatusUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointHistoryUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyPendingPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.RegisterUserPointUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointController 권한 검증 테스트")
class PointControllerTest {

    @Mock
    private RegisterUserPointUseCase registerUserPointUseCase;
    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;
    @Mock
    private ModifyPendingPointUseCase modifyPendingPointUseCase;
    @Mock
    private ConfirmPendingPointUseCase confirmPendingPointUseCase;
    @Mock
    private CancelPendingPointUseCase cancelPendingPointUseCase;
    @Mock
    private GetUserPointUseCase getUserPointUseCase;
    @Mock
    private GetUserPointHistoryUseCase getUserPointHistoryUseCase;
    @Mock
    private GetReferralStatusUseCase getReferralStatusUseCase;
    @Mock
    private ClaimReferralBonusUseCase claimReferralBonusUseCase;

    @InjectMocks
    private PointController controller;

    private OAuth2AuthenticatedPrincipal buildPrincipal(String userId, String roleName) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                userId,
                Map.of("name", userId),
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }

    @Test
    @DisplayName("관리자 권한이면 다른 사용자의 포인트 내역도 조회할 수 있다")
    void shouldAllowAdminToViewOtherUserHistory() {
        // given
        OAuth2AuthenticatedPrincipal admin = buildPrincipal("100", "ROLE_ADMIN");
        given(getUserPointHistoryUseCase.getUserPointHistories(any()))
                .willReturn(GetUserPointHistoryResult.from(0L, false, -1L, List.of()));

        // when
        ResponseEntity<BaseResponse<GetUserPointHistoryResponse>> response = controller.getUserPointHistories(
                999L, admin, UserPointHistoryFilter.ALL, null, null, null, 20
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("본인이면 자신의 포인트 내역을 조회할 수 있다")
    void shouldAllowOwnerToViewOwnHistory() {
        // given
        OAuth2AuthenticatedPrincipal owner = buildPrincipal("999", "ROLE_BUYER");
        given(getUserPointHistoryUseCase.getUserPointHistories(any()))
                .willReturn(GetUserPointHistoryResult.from(0L, false, -1L, List.of()));

        // when
        ResponseEntity<BaseResponse<GetUserPointHistoryResponse>> response = controller.getUserPointHistories(
                999L, owner, UserPointHistoryFilter.ALL, null, null, null, 20
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("본인이 아니고 관리자도 아니면 AccessDeniedException을 던진다")
    void shouldThrowAccessDeniedWhenNotOwnerAndNotAdmin() {
        OAuth2AuthenticatedPrincipal other = buildPrincipal("100", "ROLE_BUYER");

        assertThatThrownBy(() -> controller.getUserPointHistories(
                999L, other, UserPointHistoryFilter.ALL, null, null, null, 20
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("판매자 권한이라도 본인이 아니면 AccessDeniedException을 던진다")
    void shouldDenySellerForOtherUser() {
        OAuth2AuthenticatedPrincipal seller = buildPrincipal("100", "ROLE_SELLER");

        assertThatThrownBy(() -> controller.getUserPointHistories(
                999L, seller, UserPointHistoryFilter.ALL, null, null, null, 20
        )).isInstanceOf(AccessDeniedException.class);
    }
}
