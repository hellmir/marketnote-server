package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.result.WithdrawResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.WithdrawUseCase;
import com.personal.marketnote.user.port.out.oauth.Oauth2AccountUnlinkPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED, timeout = 180)
public class WithdrawService implements WithdrawUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;
    private final Oauth2AccountUnlinkPort oauth2AccountUnlinkPort;

    private final Logger log = LoggerFactory.getLogger(WithdrawService.class);

    @Override
    public WithdrawResult withdrawUser(Long id, String googleAccessToken) {
        User user = getUserUseCase.getAllStatusUser(id);
        user.withdraw();

        boolean isKakaoDisconnected = true;
        String kakaoOidcId = user.getKakaoOidcId();
        if (FormatValidator.hasValue(kakaoOidcId)) {
            try {
                oauth2AccountUnlinkPort.unlinkKakaoAccount(user.getKakaoOidcId());
            } catch (UnlinkOauth2AccountFailedException e) {
                isKakaoDisconnected = false;
                log.error("카카오 계정 연결 해제에 실패했습니다. oidcId={}", kakaoOidcId, e);
            }

            user.removeKakaoOidcId();
        }

        boolean isGoogleDisconnected = true;
        if (user.hasGoogleAccount()) {
            isGoogleDisconnected = false;
            // 현재 구글 로그인 상태인 경우 구글 로그인 연결 해제 요청
            if (FormatValidator.hasValue(googleAccessToken)) {
                try {
                    oauth2AccountUnlinkPort.unlinkGoogleAccount(googleAccessToken);
                    isGoogleDisconnected = true;
                } catch (UnlinkOauth2AccountFailedException e) {
                    log.error("구글 계정 연결 해제에 실패했습니다.", e);
                }
            }

            user.removeGoogleOidcId();
        }

        boolean isAppleDisconnected = true;
        if (user.hasAppleAccount()) {
            user.removeAppleOidcId();
            isAppleDisconnected = false;
        }
        updateUserPort.update(user);

        return new WithdrawResult(isKakaoDisconnected, isGoogleDisconnected, isAppleDisconnected);
    }
}
