package com.personal.marketnote.reward.adapter.in.web.point;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.ratelimit.RateLimited;
import com.personal.marketnote.reward.adapter.in.web.point.request.*;
import com.personal.marketnote.reward.adapter.in.web.point.response.*;
import com.personal.marketnote.reward.domain.point.*;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.ClaimReferralBonusResult;
import com.personal.marketnote.reward.port.in.result.point.GetReferralStatusResult;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;
import com.personal.marketnote.reward.port.in.usecase.point.*;
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

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("getUserPointHistories 메서드에 @RateLimited(key=user-point-histories, maxRequests=60, windowSeconds=60)이 부착되어 있다")
    void getUserPointHistoriesHasRateLimited() throws NoSuchMethodException {
        Method method = PointController.class.getMethod(
            "getUserPointHistories",
            Long.class, OAuth2AuthenticatedPrincipal.class, UserPointHistoryFilter.class,
            LocalDate.class, LocalDate.class, Long.class, int.class
        );

        RateLimited rateLimited = method.getAnnotation(RateLimited.class);

        assertThat(rateLimited).isNotNull();
        assertThat(rateLimited.key()).isEqualTo("user-point-histories");
        assertThat(rateLimited.maxRequests()).isEqualTo(60);
        assertThat(rateLimited.windowSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("registerUserPoint는 RegisterUserPointUseCase에 userId/userKey Command를 위임하고 201을 반환한다")
    void registerUserPointDelegatesWithCommandAndReturnsCreated() {
        org.mockito.ArgumentCaptor<RegisterUserPointCommand> captor =
            org.mockito.ArgumentCaptor.forClass(RegisterUserPointCommand.class);

        ResponseEntity<BaseResponse<Void>> response = controller.registerUserPoint(999L, "user-key-abc");

        verify(registerUserPointUseCase).register(captor.capture());
        RegisterUserPointCommand command = captor.getValue();
        assertThat(command.userId()).isEqualTo(999L);
        assertThat(command.userKey()).isEqualTo("user-key-abc");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("getMyPoint는 인증 주체의 userId로 조회한 포인트 정보를 반환한다")
    void getMyPointReturnsPrincipalUserInfo() {
        OAuth2AuthenticatedPrincipal principal = buildPrincipal("123", "ROLE_BUYER");
        UserPoint userPoint = sampleUserPoint(123L, 1000L, 200L);
        given(getUserPointUseCase.getUserPoint(123L)).willReturn(userPoint);

        ResponseEntity<BaseResponse<GetMyPointReponse>> response = controller.getMyPoint(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
    }

    @Test
    @DisplayName("getUserPointById는 회원 ID로 조회한 포인트 정보를 반환한다")
    void getUserPointByIdReturnsUserPoint() {
        UserPoint userPoint = sampleUserPoint(999L, 5000L, 0L);
        given(getUserPointUseCase.getUserPoint(999L)).willReturn(userPoint);

        ResponseEntity<BaseResponse<GetUserPointByIdResponse>> response = controller.getUserPointById(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(getUserPointUseCase).getUserPoint(999L);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotNull();
    }

    @Test
    @DisplayName("modifyPendingPoint는 적립 예정 포인트 수정 결과를 반환한다")
    void modifyPendingPointReturnsUpdatedResult() {
        ModifyPendingPointRequest request = new ModifyPendingPointRequest(
            UserPointChangeType.ACCRUAL,
            100L,
            UserPointSourceType.ORDER,
            1L,
            "주문 적립"
        );
        UpdateUserPointResult result = UpdateUserPointResult.from(sampleUserPoint(999L, 0L, 100L));
        given(modifyPendingPointUseCase.modifyPending(any())).willReturn(result);

        ResponseEntity<BaseResponse<UpdateUserPointResponse>> response = controller.modifyPendingPoint(999L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(modifyPendingPointUseCase).modifyPending(any());
    }

    @Test
    @DisplayName("confirmPendingPoint는 적립 예정 포인트 확정 결과를 반환한다")
    void confirmPendingPointReturnsConfirmedResult() {
        ConfirmPendingPointRequest request = new ConfirmPendingPointRequest(
            UserPointSourceType.ORDER,
            1L,
            "확정 사유"
        );
        UpdateUserPointResult result = UpdateUserPointResult.from(sampleUserPoint(999L, 100L, 0L));
        given(confirmPendingPointUseCase.confirmPending(any())).willReturn(result);

        ResponseEntity<BaseResponse<UpdateUserPointResponse>> response = controller.confirmPendingPoint(999L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(confirmPendingPointUseCase).confirmPending(any());
    }

    @Test
    @DisplayName("cancelPendingPoint는 적립 예정 포인트 취소 결과를 반환한다")
    void cancelPendingPointReturnsCancelledResult() {
        CancelPendingPointRequest request = new CancelPendingPointRequest(
            UserPointSourceType.ORDER,
            1L,
            "취소 사유"
        );
        UpdateUserPointResult result = UpdateUserPointResult.from(sampleUserPoint(999L, 0L, 0L));
        given(cancelPendingPointUseCase.cancelPending(any())).willReturn(result);

        ResponseEntity<BaseResponse<UpdateUserPointResponse>> response = controller.cancelPendingPoint(999L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cancelPendingPointUseCase).cancelPending(any());
    }

    @Test
    @DisplayName("modifyUserPoint는 회원 포인트 수정 결과를 반환한다")
    void modifyUserPointReturnsUpdatedResult() {
        ModifyUserPointRequest request = new ModifyUserPointRequest(
            UserPointChangeType.DEDUCTION,
            50L,
            UserPointSourceType.USER,
            1L,
            "수동 차감"
        );
        UpdateUserPointResult result = UpdateUserPointResult.from(sampleUserPoint(999L, 950L, 0L));
        given(modifyUserPointUseCase.modify(any())).willReturn(result);

        ResponseEntity<BaseResponse<UpdateUserPointResponse>> response = controller.modifyUserPoint(999L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(modifyUserPointUseCase).modify(any());
    }

    @Test
    @DisplayName("getReferralStatus는 인증 주체의 친구 초대 현황을 반환한다")
    void getReferralStatusReturnsStatus() {
        OAuth2AuthenticatedPrincipal principal = buildPrincipal("123", "ROLE_BUYER");
        GetReferralStatusResult result = GetReferralStatusResult.of(3L, 0L, Map.of());
        given(getReferralStatusUseCase.getReferralStatus(123L)).willReturn(result);

        ResponseEntity<BaseResponse<GetReferralStatusResponse>> response = controller.getReferralStatus(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(getReferralStatusUseCase).getReferralStatus(123L);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("claimReferralBonus는 인증 주체와 요청 티어로 보너스 클레임 결과를 반환한다")
    void claimReferralBonusReturnsResult() {
        OAuth2AuthenticatedPrincipal principal = buildPrincipal("123", "ROLE_BUYER");
        ClaimReferralBonusRequest request = new ClaimReferralBonusRequest(ReferralBonusTier.TIER_1);
        ClaimReferralBonusResult result = ClaimReferralBonusResult.from(ReferralBonusTier.TIER_1);
        given(claimReferralBonusUseCase.claim(any())).willReturn(result);

        ResponseEntity<BaseResponse<ClaimReferralBonusResponse>> response =
            controller.claimReferralBonus(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(claimReferralBonusUseCase).claim(any());
    }

    private UserPoint sampleUserPoint(long userId, long amount, long addExpected) {
        return UserPoint.from(
            UserPointSnapshotState.builder()
                .userId(userId)
                .userKey("user-key-" + userId)
                .amount(PointAmount.of(amount))
                .addExpectedAmount(PointAmount.of(addExpected))
                .expireExpectedAmount(PointAmount.of(0L))
                .build()
        );
    }
}
