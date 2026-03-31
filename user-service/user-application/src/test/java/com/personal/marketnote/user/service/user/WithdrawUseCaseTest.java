package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.result.WithdrawResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.oauth.Oauth2AccountUnlinkPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawUseCaseTest {
    @Mock
    private GetUserUseCase getUserUseCase;
    @Mock
    private UpdateUserPort updateUserPort;
    @Mock
    private Oauth2AccountUnlinkPort oauth2AccountUnlinkPort;

    @InjectMocks
    private WithdrawService withdrawService;

    @Test
    @DisplayName("회원 탈퇴 요청 시 소셜 계정이 없으면 모두 연결 해제된 상태로 반환한다")
    void withdrawUser_withoutSocialAccounts_returnsAllTrue() {
        // given
        Long id = 1L;
        User user = mock(User.class);

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, Map.of());

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.KAKAO)).isTrue();
        assertThat(disconnectResults.get(AuthVendor.GOOGLE)).isTrue();
        assertThat(disconnectResults.get(AuthVendor.APPLE)).isTrue();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(user, never()).removeOidcId(any(AuthVendor.class));
        verify(updateUserPort).update(user);
        verifyNoMoreInteractions(getUserUseCase, updateUserPort);
        verifyNoInteractions(oauth2AccountUnlinkPort);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 카카오 계정이 있으면 연결 해제 후 결과를 반환한다")
    void withdrawUser_kakaoUnlinkSuccess_returnsKakaoTrue() throws Exception {
        // given
        Long id = 2L;
        String kakaoOidcId = "kakao-oidc";
        User user = mock(User.class);

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);
        when(user.getOidcIdByVendor(AuthVendor.KAKAO)).thenReturn(kakaoOidcId);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, Map.of());

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.KAKAO)).isTrue();
        assertThat(disconnectResults.get(AuthVendor.GOOGLE)).isTrue();
        assertThat(disconnectResults.get(AuthVendor.APPLE)).isTrue();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.KAKAO, kakaoOidcId);
        verify(user).removeOidcId(AuthVendor.KAKAO);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 카카오 연결 해제에 실패해도 예외를 전파하지 않는다")
    void withdrawUser_kakaoUnlinkFails_returnsKakaoFalse() throws Exception {
        // given
        Long id = 3L;
        String kakaoOidcId = "kakao-oidc";
        User user = mock(User.class);
        UnlinkOauth2AccountFailedException exception = new UnlinkOauth2AccountFailedException("fail");

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);
        when(user.getOidcIdByVendor(AuthVendor.KAKAO)).thenReturn(kakaoOidcId);
        doThrow(exception).when(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.KAKAO, kakaoOidcId);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, Map.of());

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.KAKAO)).isFalse();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.KAKAO, kakaoOidcId);
        verify(user).removeOidcId(AuthVendor.KAKAO);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 구글 계정이 있고 토큰이 있으면 연결 해제 후 결과를 반환한다")
    void withdrawUser_googleUnlinkSuccess_returnsGoogleTrue() throws Exception {
        // given
        Long id = 4L;
        String googleAccessToken = "google-token";
        User user = mock(User.class);
        Map<AuthVendor, String> vendorCredentials = Map.of(AuthVendor.GOOGLE, googleAccessToken);

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, vendorCredentials);

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.GOOGLE)).isTrue();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.GOOGLE, googleAccessToken);
        verify(user).removeOidcId(AuthVendor.GOOGLE);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 구글 계정이 있지만 토큰이 없으면 OIDC ID로 연결 해제를 시도한다")
    void withdrawUser_googleNoToken_usesOidcId() throws Exception {
        // given
        Long id = 5L;
        String googleOidcId = "google-oidc";
        User user = mock(User.class);

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);
        when(user.getOidcIdByVendor(AuthVendor.GOOGLE)).thenReturn(googleOidcId);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, Map.of());

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.GOOGLE)).isTrue();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.GOOGLE, googleOidcId);
        verify(user).removeOidcId(AuthVendor.GOOGLE);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 구글 연결 해제에 실패해도 예외를 전파하지 않는다")
    void withdrawUser_googleUnlinkFails_returnsGoogleFalse() throws Exception {
        // given
        Long id = 6L;
        String googleAccessToken = "google-token";
        User user = mock(User.class);
        Map<AuthVendor, String> vendorCredentials = Map.of(AuthVendor.GOOGLE, googleAccessToken);
        UnlinkOauth2AccountFailedException exception = new UnlinkOauth2AccountFailedException("fail");

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);
        doThrow(exception).when(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.GOOGLE, googleAccessToken);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, vendorCredentials);

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.GOOGLE)).isFalse();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.GOOGLE, googleAccessToken);
        verify(user).removeOidcId(AuthVendor.GOOGLE);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 애플 계정이 있으면 OIDC ID를 제거하고 연결 해제를 시도한다")
    void withdrawUser_appleAccount_attemptsUnlink() throws Exception {
        // given
        Long id = 7L;
        String appleOidcId = "apple-oidc";
        User user = mock(User.class);

        when(getUserUseCase.getAllStatusUser(id)).thenReturn(user);
        when(user.getOidcIdByVendor(AuthVendor.APPLE)).thenReturn(appleOidcId);

        // when
        WithdrawResult result = withdrawService.withdrawUser(id, Map.of());

        // then
        Map<AuthVendor, Boolean> disconnectResults = result.disconnectResults();
        assertThat(disconnectResults.get(AuthVendor.APPLE)).isTrue();

        verify(getUserUseCase).getAllStatusUser(id);
        verify(user).withdraw();
        verify(oauth2AccountUnlinkPort).unlinkAccount(AuthVendor.APPLE, appleOidcId);
        verify(user).removeOidcId(AuthVendor.APPLE);
        verify(updateUserPort).update(user);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 회원 조회에 실패하면 예외를 던진다")
    void withdrawUser_userNotFound_throws() {
        // given
        Long id = 8L;
        UserNotFoundException exception = new UserNotFoundException("not found");

        when(getUserUseCase.getAllStatusUser(id)).thenThrow(exception);

        // expect
        assertThatThrownBy(() -> withdrawService.withdrawUser(id, Map.of(AuthVendor.GOOGLE, "token")))
                .isSameAs(exception);

        verify(getUserUseCase).getAllStatusUser(id);
        verifyNoMoreInteractions(getUserUseCase);
        verifyNoInteractions(updateUserPort, oauth2AccountUnlinkPort);
    }
}
