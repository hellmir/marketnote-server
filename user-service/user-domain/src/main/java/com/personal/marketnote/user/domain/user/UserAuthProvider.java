package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UserAuthProvider {
    private User user;
    private final AuthVendor authVendor;
    private String oidcId;

    private UserAuthProvider(AuthVendor authVendor) {
        this.authVendor = authVendor;
    }

    private UserAuthProvider(AuthVendor authVendor, String oidcId) {
        this.authVendor = authVendor;
        this.oidcId = oidcId;
    }

    static UserAuthProvider of(AuthVendor authVendor) {
        return new UserAuthProvider(authVendor);
    }

    public static UserAuthProvider of(
            AuthVendor authVendor,
            String oidcId
    ) {
        return new UserAuthProvider(authVendor, oidcId);
    }

    public void addUser(User user) {
        if (FormatValidator.hasNoValue(this.user)) {
            this.user = user;
        }
    }

    void update(AuthVendor authVendor, String oidcId) {
        if (isMe(authVendor)) {
            addOidcId(authVendor, oidcId, user.getEmail());
        }
    }

    private boolean isMe(AuthVendor authVendor) {
        return this.authVendor.isMe(authVendor);
    }

    void addOidcId(AuthVendor authVendor, String oidcId, String email) {
        // 일반 회원인 경우 회원 이메일 주소를 oidcId로 사용
        if (authVendor.isNative()) {
            this.oidcId = email;
            return;
        }

        this.oidcId = oidcId;
    }

    public boolean isVendor(AuthVendor vendor) {
        return this.authVendor.isMe(vendor);
    }

    public boolean hasAccount(AuthVendor vendor) {
        return isVendor(vendor) && FormatValidator.hasValue(oidcId);
    }

    public void removeOidcId() {
        oidcId = null;
    }
}
