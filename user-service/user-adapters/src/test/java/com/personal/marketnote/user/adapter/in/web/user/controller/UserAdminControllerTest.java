package com.personal.marketnote.user.adapter.in.web.user.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.user.request.ApplyUserPenaltyRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.ChangeUserStatusRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserInfoRequest;
import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserPenaltyCountRequest;
import com.personal.marketnote.user.adapter.in.web.user.response.ApplyUserPenaltyResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.ChangeUserStatusResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetLoginHistoriesResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUserInfoResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUserPenaltyHistoriesResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUserStatusHistoriesResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.GetUsersResponse;
import com.personal.marketnote.user.adapter.in.web.user.response.UpdateUserPenaltyCountResponse;
import com.personal.marketnote.user.domain.user.LoginHistorySortProperty;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySortProperty;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.domain.user.UserSortProperty;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusHistorySortProperty;
import com.personal.marketnote.user.port.in.command.ApplyUserPenaltyCommand;
import com.personal.marketnote.user.port.in.command.ChangeUserStatusCommand;
import com.personal.marketnote.user.port.in.command.UpdateUserInfoCommand;
import com.personal.marketnote.user.port.in.command.UpdateUserPenaltyCountCommand;
import com.personal.marketnote.user.port.in.result.AccountInfoResult;
import com.personal.marketnote.user.port.in.result.ApplyUserPenaltyResult;
import com.personal.marketnote.user.port.in.result.ChangeUserStatusResult;
import com.personal.marketnote.user.port.in.result.GetLoginHistoryResult;
import com.personal.marketnote.user.port.in.result.GetUserInfoResult;
import com.personal.marketnote.user.port.in.result.GetUserPenaltyHistoryResult;
import com.personal.marketnote.user.port.in.result.GetUserResult;
import com.personal.marketnote.user.port.in.result.GetUserStatusHistoryResult;
import com.personal.marketnote.user.port.in.result.UpdateUserPenaltyCountResult;
import com.personal.marketnote.user.port.in.usecase.user.ApplyUserPenaltyUseCase;
import com.personal.marketnote.user.port.in.usecase.user.ChangeUserStatusUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetLoginHistoryUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetUserPenaltyHistoryUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetUserStatusHistoryUseCase;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.UpdateUserPenaltyCountUseCase;
import com.personal.marketnote.user.port.in.usecase.user.UpdateUserUseCase;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAdminController 관리자 회원 관리")
class UserAdminControllerTest {

    @InjectMocks
    private UserAdminController controller;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private GetLoginHistoryUseCase getLoginHistoryUseCase;

    @Mock
    private ChangeUserStatusUseCase changeUserStatusUseCase;

    @Mock
    private ApplyUserPenaltyUseCase applyUserPenaltyUseCase;

    @Mock
    private UpdateUserPenaltyCountUseCase updateUserPenaltyCountUseCase;

    @Mock
    private GetUserPenaltyHistoryUseCase getUserPenaltyHistoryUseCase;

    @Mock
    private GetUserStatusHistoryUseCase getUserStatusHistoryUseCase;

    private OAuth2AuthenticatedPrincipal adminPrincipal(Long adminId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(adminId), Map.of("name", String.valueOf(adminId)), List.of()
        );
    }

    private GetUserResult buildUserResult(Long id) {
        return new GetUserResult(
                id,
                new AccountInfoResult(List.of()),
                "닉네임",
                "user@example.com",
                "홍길동",
                "010-1234-5678",
                "ABC123",
                "ROLE_BUYER",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0),
                "ACTIVE",
                false,
                100L,
                0,
                null
        );
    }

    private GetUserInfoResult buildUserInfoResult(Long id) {
        return new GetUserInfoResult(
                id,
                new AccountInfoResult(List.of()),
                "닉네임",
                "user@example.com",
                "홍길동",
                "010-1234-5678",
                "ABC123",
                "ROLE_BUYER",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0),
                "ACTIVE",
                false,
                100L,
                0,
                null
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users - 회원 목록 조회")
    class GetUsers {

        @Test
        @DisplayName("회원 목록을 조회하면 1-based 페이지 번호와 래핑된 페이지 메타데이터를 반환한다")
        void returnsUsersWithPageMetadata() {
            // given
            List<GetUserResult> content = List.of(buildUserResult(1L), buildUserResult(2L));
            Page<GetUserResult> page = new PageImpl<>(content, PageRequest.of(0, 10), 15);
            when(getUserUseCase.getAllStatusUsers(
                    10, 0, Sort.Direction.DESC, UserSortProperty.ORDER_NUM,
                    UserSearchTarget.EMAIL, "user@example.com"
            )).thenReturn(page);

            // when
            ResponseEntity<BaseResponse<GetUsersResponse>> response = controller.getUsers(
                    10, 1, Sort.Direction.DESC, UserSortProperty.ORDER_NUM,
                    UserSearchTarget.EMAIL, "user@example.com"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            GetUsersResponse usersResponse = response.getBody().getContent();
            assertThat(usersResponse.pageNumber()).isEqualTo(1);
            assertThat(usersResponse.pageSize()).isEqualTo(10);
            assertThat(usersResponse.totalCount()).isEqualTo(15);
            assertThat(usersResponse.hasNext()).isTrue();
            assertThat(usersResponse.users()).hasSize(2);
        }

        @Test
        @DisplayName("요청된 페이지 번호에서 1을 차감하여 0-based로 UseCase에 전달한다")
        void subtractsOneFromPageNumberForUseCase() {
            // given
            Page<GetUserResult> page = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
            when(getUserUseCase.getAllStatusUsers(
                    5, 2, Sort.Direction.ASC, UserSortProperty.ID,
                    UserSearchTarget.NICKNAME, null
            )).thenReturn(page);

            // when
            controller.getUsers(
                    5, 3, Sort.Direction.ASC, UserSortProperty.ID,
                    UserSearchTarget.NICKNAME, null
            );

            // then
            verify(getUserUseCase).getAllStatusUsers(
                    5, 2, Sort.Direction.ASC, UserSortProperty.ID,
                    UserSearchTarget.NICKNAME, null
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{id} - 회원 정보 조회")
    class GetUserInfo {

        @Test
        @DisplayName("회원 ID로 전체 상태 회원 정보를 조회하여 OK 응답을 반환한다")
        void returnsUserInfoForAllStatus() {
            // given
            when(getUserUseCase.getAllStatusUserInfo(99L)).thenReturn(buildUserInfoResult(99L));

            // when
            ResponseEntity<BaseResponse<GetUserInfoResponse>> response = controller.getUserInfo(99L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().userInfo().id()).isEqualTo(99L);
            verify(getUserUseCase).getAllStatusUserInfo(99L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id} - 회원 정보 수정")
    class UpdateUserInfo {

        @Test
        @DisplayName("관리자 여부 true와 요청을 Command로 매핑하여 UseCase에 위임한다")
        void delegatesToUpdateUseCaseAsAdmin() {
            // given
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            ReflectionTestUtils.setField(request, "isActive", Boolean.TRUE);
            ReflectionTestUtils.setField(request, "nickname", "newNick");
            ReflectionTestUtils.setField(request, "email", "new@example.com");

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.updateUserInfo(5L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdateUserInfoCommand> captor = ArgumentCaptor.forClass(UpdateUserInfoCommand.class);
            verify(updateUserUseCase).updateUserInfo(eq(true), eq(5L), captor.capture());
            UpdateUserInfoCommand captured = captor.getValue();
            assertThat(captured.nickname()).isEqualTo("newNick");
            assertThat(captured.email()).isEqualTo("new@example.com");
            assertThat(captured.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{userId}/login-histories - 로그인 이력 조회")
    class GetLoginHistories {

        @Test
        @DisplayName("로그인 이력을 조회하면 페이징 메타데이터와 함께 OK 응답을 반환한다")
        void returnsLoginHistoriesWithPaging() {
            // given
            GetLoginHistoryResult loginHistoryResult = new GetLoginHistoryResult(
                    1L, 10L, AuthVendor.NATIVE, "127.0.0.1", LocalDateTime.of(2025, 3, 1, 10, 0)
            );
            Page<GetLoginHistoryResult> page = new PageImpl<>(
                    List.of(loginHistoryResult), PageRequest.of(0, 20), 1
            );
            when(getLoginHistoryUseCase.getLoginHistories(
                    10L, 20, 0, Sort.Direction.DESC, LoginHistorySortProperty.ID
            )).thenReturn(page);

            // when
            ResponseEntity<BaseResponse<GetLoginHistoriesResponse>> response = controller.getLoginHistories(
                    10L, 20, 1, Sort.Direction.DESC, LoginHistorySortProperty.ID
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getLoginHistoryUseCase).getLoginHistories(
                    10L, 20, 0, Sort.Direction.DESC, LoginHistorySortProperty.ID
            );
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{userId}/status - 회원 상태 변경")
    class ChangeUserStatus {

        @Test
        @DisplayName("비활성화 요청 시 적용 종료 일시와 관리자 ID를 Command에 담아 위임한다")
        void deactivatesUserWithAdminContext() {
            // given
            LocalDateTime deactivatedUntil = LocalDateTime.of(2026, 12, 31, 23, 59);
            ChangeUserStatusRequest request = new ChangeUserStatusRequest(
                    UserStatusAction.DEACTIVATE, "운영 정책 위반", deactivatedUntil
            );
            ChangeUserStatusResult result = ChangeUserStatusResult.of(10L, "INACTIVE", deactivatedUntil, 1L);
            when(changeUserStatusUseCase.changeStatus(new ChangeUserStatusCommand(
                    10L, 1L, "DEACTIVATE", "운영 정책 위반", deactivatedUntil
            ))).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<ChangeUserStatusResponse>> response = controller.changeUserStatus(
                    10L, request, adminPrincipal(1L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            ChangeUserStatusResponse body = response.getBody().getContent();
            assertThat(body.userId()).isEqualTo(10L);
            assertThat(body.status()).isEqualTo("INACTIVE");
            assertThat(body.deactivatedUntil()).isEqualTo(deactivatedUntil);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/users/{userId}/penalties - 회원 패널티 부과")
    class ApplyUserPenalty {

        @Test
        @DisplayName("패널티 부과 결과를 응답으로 변환하고 관리자 ID를 Command에 담는다")
        void appliesPenaltyWithAdminId() {
            // given
            ApplyUserPenaltyRequest request = new ApplyUserPenaltyRequest("욕설 신고");
            when(applyUserPenaltyUseCase.applyPenalty(
                    new ApplyUserPenaltyCommand(10L, 1L, "욕설 신고")
            )).thenReturn(ApplyUserPenaltyResult.of(10L, 2, 55L));

            // when
            ResponseEntity<BaseResponse<ApplyUserPenaltyResponse>> response = controller.applyUserPenalty(
                    10L, request, adminPrincipal(1L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().penaltyCount()).isEqualTo(2);
            assertThat(response.getBody().getContent().historyId()).isEqualTo(55L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{userId}/penalties - 회원 패널티 횟수 수정")
    class UpdateUserPenaltyCount {

        @Test
        @DisplayName("패널티 횟수 수정 요청을 Command로 매핑하여 위임한다")
        void delegatesPenaltyCountUpdate() {
            // given
            UpdateUserPenaltyCountRequest request = new UpdateUserPenaltyCountRequest(5, "누적 조정");
            when(updateUserPenaltyCountUseCase.updatePenaltyCount(
                    new UpdateUserPenaltyCountCommand(10L, 1L, 5, "누적 조정")
            )).thenReturn(UpdateUserPenaltyCountResult.of(10L, 5, 77L));

            // when
            ResponseEntity<BaseResponse<UpdateUserPenaltyCountResponse>> response = controller.updateUserPenaltyCount(
                    10L, request, adminPrincipal(1L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().penaltyCount()).isEqualTo(5);
            assertThat(response.getBody().getContent().historyId()).isEqualTo(77L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{userId}/penalties - 회원 패널티 내역 조회")
    class GetUserPenaltyHistories {

        @Test
        @DisplayName("패널티 내역을 1-based 페이지 번호로 변환하여 UseCase에 위임한다")
        void convertsPageNumberAndReturnsPenaltyHistories() {
            // given
            GetUserPenaltyHistoryResult history = new GetUserPenaltyHistoryResult(
                    1L, 10L, 1, 2, "누적", 1L, LocalDateTime.of(2025, 3, 1, 10, 0)
            );
            Page<GetUserPenaltyHistoryResult> page = new PageImpl<>(
                    List.of(history), PageRequest.of(1, 20), 25
            );
            when(getUserPenaltyHistoryUseCase.getUserPenaltyHistories(
                    10L, 20, 1, Sort.Direction.DESC, UserPenaltyHistorySortProperty.ID
            )).thenReturn(page);

            // when
            ResponseEntity<BaseResponse<GetUserPenaltyHistoriesResponse>> response = controller.getUserPenaltyHistories(
                    10L, 20, 2, Sort.Direction.DESC, UserPenaltyHistorySortProperty.ID
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getUserPenaltyHistoryUseCase).getUserPenaltyHistories(
                    10L, 20, 1, Sort.Direction.DESC, UserPenaltyHistorySortProperty.ID
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/users/{userId}/status-histories - 회원 상태 변경 내역 조회")
    class GetUserStatusHistories {

        @Test
        @DisplayName("상태 변경 내역 조회 시 나머지 UseCase는 호출되지 않는다")
        void invokesOnlyStatusHistoryUseCase() {
            // given
            GetUserStatusHistoryResult history = new GetUserStatusHistoryResult(
                    1L, 10L, "DEACTIVATE", "정책 위반",
                    LocalDateTime.of(2026, 12, 31, 0, 0), 1L, LocalDateTime.of(2025, 3, 1, 10, 0)
            );
            Page<GetUserStatusHistoryResult> page = new PageImpl<>(
                    List.of(history), PageRequest.of(0, 20), 1
            );
            when(getUserStatusHistoryUseCase.getUserStatusHistories(
                    10L, 20, 0, Sort.Direction.DESC, UserStatusHistorySortProperty.ID
            )).thenReturn(page);

            // when
            ResponseEntity<BaseResponse<GetUserStatusHistoriesResponse>> response = controller.getUserStatusHistories(
                    10L, 20, 1, Sort.Direction.DESC, UserStatusHistorySortProperty.ID
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verifyNoInteractions(
                    getUserUseCase, updateUserUseCase, getLoginHistoryUseCase,
                    changeUserStatusUseCase, applyUserPenaltyUseCase,
                    updateUserPenaltyCountUseCase, getUserPenaltyHistoryUseCase
            );
        }
    }
}
