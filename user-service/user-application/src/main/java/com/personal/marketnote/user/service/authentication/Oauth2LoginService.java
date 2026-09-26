package com.personal.marketnote.user.service.authentication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.OauthTokenNoValueException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.UserNotActiveException;
import com.personal.marketnote.user.port.in.result.LoginResult;
import com.personal.marketnote.user.port.in.usecase.authentication.Oauth2LoginUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, timeout = 180)
public class Oauth2LoginService implements Oauth2LoginUseCase {
    private final TokenSupport tokenSupport;
    private final FindUserPort findUserPort;

    @Override
    public LoginResult loginByOAuth2(String code, String redirectUri, AuthVendor authVendor)
            throws UnsupportedCodeException {
        GrantedTokenInfo grantedTokenInfo = tokenSupport.grantToken(code, redirectUri, authVendor);
        Optional<User> user = findUserPort.findAllStatusUserByAuthVendorAndOidcId(grantedTokenInfo.authVendor(), grantedTokenInfo.id());

        if (user.isPresent()) {
            User signedUpUser = user.get();

            if (!signedUpUser.isActive()) {
                throw new UserNotActiveException(FIRST_ERROR_CODE, grantedTokenInfo.authVendor());
            }

            return LoginResult.of(
                    false, grantedTokenInfo.accessToken(), grantedTokenInfo.refreshToken(), signedUpUser.getNickname()
            );
        }

        OAuth2UserInfo userInfo = grantedTokenInfo.userInfo();

        if (FormatValidator.hasNoValue(userInfo)) {
            throw new OauthTokenNoValueException();
        }

        return LoginResult.of(true, grantedTokenInfo.accessToken(), grantedTokenInfo.refreshToken(), userInfo.name());
    }
}


