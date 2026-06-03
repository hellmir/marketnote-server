package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.result.WithdrawResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.WithdrawUseCase;
import com.personal.marketnote.user.port.out.oauth.Oauth2AccountUnlinkPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.service.exception.UnlinkOauth2AccountFailedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

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
    public WithdrawResult withdrawUser(Long id, Map<AuthVendor, String> vendorCredentials) {
        User user = getUserUseCase.getAllStatusUser(id);
        user.withdraw();

        Map<AuthVendor, Boolean> disconnectResults = new EnumMap<>(AuthVendor.class);

        for (AuthVendor vendor : AuthVendor.values()) {
            if (vendor.isNative()) {
                continue;
            }

            boolean isDisconnected = unlinkVendorAccount(user, vendor, vendorCredentials);
            disconnectResults.put(vendor, isDisconnected);
        }

        updateUserPort.update(user);

        return new WithdrawResult(disconnectResults);
    }

    private boolean unlinkVendorAccount(User user, AuthVendor vendor, Map<AuthVendor, String> vendorCredentials) {
        String credential = resolveCredential(user, vendor, vendorCredentials);
        if (FormatValidator.hasNoValue(credential)) {
            return true;
        }

        boolean isDisconnected = tryUnlinkAccount(vendor, credential);
        user.removeOidcId(vendor);
        return isDisconnected;
    }

    private String resolveCredential(User user, AuthVendor vendor, Map<AuthVendor, String> vendorCredentials) {
        // 외부에서 제공된 자격 증명이 있으면 우선 사용 (예: 구글 액세스 토큰)
        if (FormatValidator.hasValue(vendorCredentials)) {
            String externalCredential = vendorCredentials.get(vendor);
            if (FormatValidator.hasValue(externalCredential)) {
                return externalCredential;
            }
        }

        // 외부 자격 증명이 없으면 사용자의 OIDC ID 사용
        return user.getOidcIdByVendor(vendor);
    }

    private boolean tryUnlinkAccount(AuthVendor vendor, String credential) {
        try {
            oauth2AccountUnlinkPort.unlinkAccount(vendor, credential);
            return true;
        } catch (UnlinkOauth2AccountFailedException e) {
            log.error("{} 계정 연결 해제에 실패했습니다.", vendor.name(), e);
            return false;
        }
    }
}
