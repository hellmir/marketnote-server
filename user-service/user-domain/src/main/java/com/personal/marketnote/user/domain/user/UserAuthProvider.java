package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.email.Email;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserAuthProvider {
    private User user;
    private AuthVendor authVendor;
    private String oidcId;

    static UserAuthProvider of(AuthVendor authVendor) {
        return UserAuthProvider.builder()
                .authVendor(authVendor)
                .build();
    }

    public static UserAuthProvider of(AuthVendor authVendor, String oidcId) {
        return UserAuthProvider.builder()
                .authVendor(authVendor)
                .oidcId(oidcId)
                .build();
    }

    public void addUser(User user) {
        if (FormatValidator.hasNoValue(this.user)) {
            this.user = user;
        }
    }

    void update(AuthVendor authVendor, String oidcId) {
        if (isMe(authVendor)) {
            addOidcId(authVendor, oidcId, toEmailValue(user.getEmail()));
        }
    }

    private static String toEmailValue(Email email) {
        if (FormatValidator.hasNoValue(email)) {
            return null;
        }
        return email.getValue();
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
