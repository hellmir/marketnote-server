package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.exception.illegalstate.SameUpdateTargetException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.exception.ReferredUserCodeAlreadyExistsException;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class User extends BaseDomain {
    private static final int WITHDRAWAL_COOLDOWN_DAYS = 30;

    private Long id;
    private UUID userKey;
    private String nickname;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String referenceCode;
    private String referredUserCode;
    private Role role;
    private List<UserAuthProvider> userAuthProviders;
    private List<UserTerms> userTerms;
    private LocalDateTime signedUpAt;
    private LocalDateTime lastLoggedInAt;
    private boolean withdrawalYn;
    private LocalDateTime withdrawnAt;
    private Long orderNum;

    public static User from(UserCreateState state) {
        if (state.isGuest()) {
            return createGuest(state);
        }

        AuthVendor targetAuthVendor = state.getAuthVendor();
        List<Terms> terms = FormatValidator.hasValue(state.getTerms())
                ? state.getTerms()
                : List.of();

        List<UserAuthProvider> userAuthProviders = new ArrayList<>(AuthVendor.size());
        AuthVendor[] allAuthVendors = AuthVendor.values();

        // 최초 회원 가입 시 모든 공급업체 튜플 추가
        for (AuthVendor authVendor : allAuthVendors) {
            UserAuthProvider userAuthProvider = UserAuthProvider.of(authVendor);
            userAuthProviders.add(userAuthProvider);

            // 실제 가입한 공급업체만 OIDC ID 삽입
            if (authVendor.isMe(targetAuthVendor)) {
                userAuthProvider.addOidcId(targetAuthVendor, state.getOidcId(), state.getEmail());
            }
        }

        User user = User.builder()
                .userKey(RandomCodeGenerator.generateUserKey())
                .userAuthProviders(userAuthProviders)
                .nickname(state.getNickname())
                .email(state.getEmail())
                .fullName(state.getFullName())
                .phoneNumber(state.getPhoneNumber())
                .referenceCode(state.getReferenceCode())
                .role(Role.getBuyer())
                .lastLoggedInAt(LocalDateTime.now())
                .build();

        // 일반 회원 가입인 경우 비밀번호 설정
        if (targetAuthVendor.isNative() && state.hasPassword()) {
            user.password = state.getEncodedPassword();
        }

        // 최초 회원 가입 시 모든 이용 약관 튜플 추가
        user.userTerms = terms.stream()
                .map(term -> UserTerms.from(
                        UserTermsCreateState.builder()
                                .user(user)
                                .terms(term)
                                .build()
                ))
                .collect(Collectors.toList());

        return user;
    }

    public static User from(UserSnapshotState state) {
        User user = User.builder()
                .id(state.getId())
                .userKey(state.getUserKey())
                .nickname(state.getNickname())
                .email(state.getEmail())
                .password(state.getPassword())
                .fullName(state.getFullName())
                .phoneNumber(state.getPhoneNumber())
                .referenceCode(state.getReferenceCode())
                .referredUserCode(state.getReferredUserCode())
                .role(state.getRole())
                .userAuthProviders(state.getUserAuthProviders())
                .userTerms(state.getUserTerms())
                .signedUpAt(state.getSignedUpAt())
                .lastLoggedInAt(state.getLastLoggedInAt())
                .withdrawalYn(Boolean.TRUE.equals(state.getWithdrawalYn()))
                .withdrawnAt(state.getWithdrawnAt())
                .orderNum(state.getOrderNum())
                .build();

        EntityStatus status = state.getStatus();
        if (status.isActive()) {
            user.activate();
            return user;
        }

        if (status.isInactive()) {
            user.deactivate();
            return user;
        }

        user.hide();
        return user;
    }

    public static User referenceOf(Long id) {
        return User.builder()
                .id(id)
                .role(Role.getGuest())
                .userAuthProviders(new ArrayList<>())
                .userTerms(new ArrayList<>())
                .build();
    }

    private static User createGuest(UserCreateState state) {
        return User.builder()
                .userAuthProviders(List.of(UserAuthProvider.of(state.getAuthVendor(), state.getOidcId())))
                .role(Role.getGuest())
                .build();
    }

    public boolean isGuest() {
        return role.isGuest();
    }

    public void acceptOrCancelTerms(List<Long> termsIds) {
        userTerms.stream()
                .filter(userTerms -> termsIds.contains(userTerms.getTerms().getId()))
                .forEach(UserTerms::acceptOrCancel);
    }

    public boolean isValidPassword(PasswordEncoder passwordEncoder, String targetPassword) {
        return passwordEncoder.matches(targetPassword, password);
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updatePassword(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public boolean isSelfReferral(String code) {
        return FormatValidator.hasValue(referenceCode) && referenceCode.equals(code);
    }

    public boolean isMutualReferralWith(User otherUser) {
        return FormatValidator.hasValue(this.referredUserCode)
                && FormatValidator.hasValue(otherUser.referenceCode)
                && this.referredUserCode.equals(otherUser.referenceCode);
    }

    public void registerReferredUserCode(String referredUserCode) {
        if (FormatValidator.hasValue(this.referredUserCode)) {
            throw new ReferredUserCodeAlreadyExistsException(SECOND_ERROR_CODE);
        }

        this.referredUserCode = referredUserCode;
    }

    public void removeReferredUserCode() {
        referredUserCode = null;
    }

    public void registerEmail(String email) {
        this.email = email;
    }

    public void addLoginAccountInfo(AuthVendor authVendor, String oidcId, String password, PasswordEncoder passwordEncoder) {
        userAuthProviders.forEach(
                userAuthProvider -> userAuthProvider.update(authVendor, oidcId)
        );

        if (FormatValidator.hasValue(password)) {
            this.password = passwordEncoder.encode(password);
        }
    }

    public void validateDifferentEmail(String email) {
        if (FormatValidator.equals(this.email, email)) {
            throw new SameUpdateTargetException(FIRST_ERROR_CODE, email);
        }
    }

    public void validateDifferentNickname(String nickname) {
        if (FormatValidator.equals(this.nickname, nickname)) {
            throw new SameUpdateTargetException(SECOND_ERROR_CODE, nickname);
        }
    }

    public void validateDifferentPhoneNumber(String phoneNumber) {
        if (FormatValidator.equals(this.phoneNumber, phoneNumber)) {
            throw new SameUpdateTargetException(THIRD_ERROR_CODE, phoneNumber);
        }
    }

    public boolean isRequiredTermsAgreed() {
        return userTerms.stream()
                .allMatch(UserTerms::isRequiredTermsAgreed);
    }

    public void withdraw(LocalDateTime now) {
        if (withdrawalYn) {
            return;
        }
        withdrawalYn = true;
        withdrawnAt = now;
        deactivate();
        userTerms.forEach(UserTerms::disagree);
    }

    public void cancelWithdrawal(LocalDateTime now) {
        withdrawalYn = false;
        withdrawnAt = null;
        activate();
        signedUpAt = now;
    }

    public boolean isWithdrawn() {
        return withdrawalYn;
    }

    public boolean canReSignUp(LocalDateTime now) {
        if (withdrawnAt == null) {
            return true;
        }
        return !now.isBefore(withdrawnAt.plusDays(WITHDRAWAL_COOLDOWN_DAYS));
    }

    public String getOidcIdByVendor(AuthVendor vendor) {
        return userAuthProviders.stream()
                .filter(v -> v.isVendor(vendor))
                .findFirst()
                .map(UserAuthProvider::getOidcId)
                .orElse(null);
    }

    public boolean hasAccount(AuthVendor vendor) {
        return userAuthProviders.stream()
                .anyMatch(v -> v.hasAccount(vendor));
    }

    public void removeOidcId(AuthVendor vendor) {
        userAuthProviders.stream()
                .filter(v -> v.isVendor(vendor))
                .forEach(UserAuthProvider::removeOidcId);
    }
}
