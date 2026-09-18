package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthProviderTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_OIDC_ID = "kakao-oidc-12345";

    private User createUserWithEmail(String email) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .email(email)
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .build());
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("인증 공급자와 OIDC ID로 UserAuthProvider를 생성한다")
        void shouldCreateWithAuthVendorAndOidcId() {
            // when
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);

            // then
            assertThat(provider.getAuthVendor()).isEqualTo(AuthVendor.KAKAO);
            assertThat(provider.getOidcId()).isEqualTo(TEST_OIDC_ID);
        }

        @Test
        @DisplayName("인증 공급자만으로 생성하면 oidcId는 null이다")
        void shouldCreateWithAuthVendorOnlyAndNullOidcId() {
            // when
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.GOOGLE);

            // then
            assertThat(provider.getAuthVendor()).isEqualTo(AuthVendor.GOOGLE);
            assertThat(provider.getOidcId()).isNull();
        }
    }

    @Nested
    @DisplayName("addUser")
    class AddUser {

        @Test
        @DisplayName("user가 없으면 User를 설정한다")
        void shouldSetUserWhenNotAlreadySet() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);
            User user = createUserWithEmail(TEST_EMAIL);

            // when
            provider.addUser(user);

            // then
            assertThat(provider.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("user가 이미 있으면 변경하지 않는다")
        void shouldNotChangeUserWhenAlreadySet() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);
            User firstUser = createUserWithEmail(TEST_EMAIL);
            User secondUser = createUserWithEmail("other@example.com");
            provider.addUser(firstUser);

            // when
            provider.addUser(secondUser);

            // then
            assertThat(provider.getUser()).isEqualTo(firstUser);
        }
    }

    @Nested
    @DisplayName("addOidcId")
    class AddOidcId {

        @Test
        @DisplayName("NATIVE 벤더이면 이메일을 oidcId로 설정한다")
        void shouldSetEmailAsOidcIdForNativeVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.NATIVE);

            // when
            provider.addOidcId(AuthVendor.NATIVE, "ignored-oidc-id", TEST_EMAIL);

            // then
            assertThat(provider.getOidcId()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("소셜 벤더이면 oidcId를 그대로 설정한다")
        void shouldSetOidcIdDirectlyForSocialVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO);

            // when
            provider.addOidcId(AuthVendor.KAKAO, TEST_OIDC_ID, TEST_EMAIL);

            // then
            assertThat(provider.getOidcId()).isEqualTo(TEST_OIDC_ID);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("같은 벤더이면 oidcId를 업데이트한다")
        void shouldUpdateOidcIdWhenSameVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO);
            User user = createUserWithEmail(TEST_EMAIL);
            provider.addUser(user);

            // when
            provider.update(AuthVendor.KAKAO, TEST_OIDC_ID);

            // then
            assertThat(provider.getOidcId()).isEqualTo(TEST_OIDC_ID);
        }

        @Test
        @DisplayName("다른 벤더이면 oidcId를 변경하지 않는다")
        void shouldNotUpdateOidcIdWhenDifferentVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO);
            User user = createUserWithEmail(TEST_EMAIL);
            provider.addUser(user);

            // when
            provider.update(AuthVendor.GOOGLE, "google-oidc-id");

            // then
            assertThat(provider.getOidcId()).isNull();
        }
    }

    @Nested
    @DisplayName("isVendor")
    class IsVendor {

        @Test
        @DisplayName("같은 벤더이면 true를 반환한다")
        void shouldReturnTrueForSameVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);

            // expect
            assertThat(provider.isVendor(AuthVendor.KAKAO)).isTrue();
        }

        @Test
        @DisplayName("다른 벤더이면 false를 반환한다")
        void shouldReturnFalseForDifferentVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);

            // expect
            assertThat(provider.isVendor(AuthVendor.GOOGLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasAccount")
    class HasAccount {

        @Test
        @DisplayName("같은 벤더이고 oidcId가 있으면 true를 반환한다")
        void shouldReturnTrueWhenSameVendorAndOidcIdExists() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);

            // expect
            assertThat(provider.hasAccount(AuthVendor.KAKAO)).isTrue();
        }

        @Test
        @DisplayName("같은 벤더이지만 oidcId가 없으면 false를 반환한다")
        void shouldReturnFalseWhenSameVendorButNoOidcId() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO);

            // expect
            assertThat(provider.hasAccount(AuthVendor.KAKAO)).isFalse();
        }

        @Test
        @DisplayName("다른 벤더이면 false를 반환한다")
        void shouldReturnFalseForDifferentVendor() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);

            // expect
            assertThat(provider.hasAccount(AuthVendor.GOOGLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("removeOidcId")
    class RemoveOidcId {

        @Test
        @DisplayName("oidcId를 null로 초기화한다")
        void shouldSetOidcIdToNull() {
            // given
            UserAuthProvider provider = UserAuthProvider.of(AuthVendor.KAKAO, TEST_OIDC_ID);

            // when
            provider.removeOidcId();

            // then
            assertThat(provider.getOidcId()).isNull();
        }
    }
}
