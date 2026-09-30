package com.personal.marketnote.user.adapter.out.mapper;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.authentication.entity.RoleJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.TermsJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserOauth2VendorJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserTermsJpaEntity;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.Nickname;
import com.personal.marketnote.user.domain.user.ReferenceCode;
import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserAuthProvider;
import com.personal.marketnote.user.domain.user.UserTerms;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserJpaEntityToDomainMapperTest {

    @Nested
    @DisplayName("mapToDomain")
    class MapToDomainTest {

        @Test
        @DisplayName("null 입력 시 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenInputIsNull() {
            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("모든 필드가 있는 UserJpaEntity를 User 도메인으로 변환한다")
        void shouldMapAllFieldsToDomain() {
            // given
            Long id = 1L;
            UUID userKey = UUID.randomUUID();
            String nickname = "테스트닉네임";
            String email = "test@example.com";
            String password = "encoded-password";
            String fullName = "홍길동";
            String phoneNumber = "010-0001-0001";
            String referenceCode = "ABC123";
            String referredUserCode = "DEF456";
            LocalDateTime signedUpAt = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime lastLoggedInAt = LocalDateTime.of(2026, 4, 14, 10, 0);
            Boolean withdrawalYn = false;
            Long orderNum = 100L;
            int penaltyCount = 0;

            RoleJpaEntity roleJpaEntity = mock(RoleJpaEntity.class);
            when(roleJpaEntity.getId()).thenReturn("ROLE_BUYER");
            when(roleJpaEntity.getName()).thenReturn("구매자");

            UserOauth2VendorJpaEntity vendorEntity = mock(UserOauth2VendorJpaEntity.class);
            when(vendorEntity.getAuthVendor()).thenReturn(AuthVendor.KAKAO);
            when(vendorEntity.getOidcId()).thenReturn("kakao-oidc-123");

            UserJpaEntity userJpaEntity = mock(UserJpaEntity.class);
            when(userJpaEntity.getId()).thenReturn(id);
            when(userJpaEntity.getUserKey()).thenReturn(userKey);
            when(userJpaEntity.getNickname()).thenReturn(nickname);
            when(userJpaEntity.getEmail()).thenReturn(email);
            when(userJpaEntity.getPassword()).thenReturn(password);
            when(userJpaEntity.getFullName()).thenReturn(fullName);
            when(userJpaEntity.getPhoneNumber()).thenReturn(phoneNumber);
            when(userJpaEntity.getReferenceCode()).thenReturn(referenceCode);
            when(userJpaEntity.getReferredUserCode()).thenReturn(referredUserCode);
            when(userJpaEntity.getRoleJpaEntity()).thenReturn(roleJpaEntity);
            when(userJpaEntity.getUserOauth2VendorsJpaEntities()).thenReturn(List.of(vendorEntity));
            when(userJpaEntity.getUserTermsJpaEntities()).thenReturn(List.of());
            when(userJpaEntity.getSignedUpAt()).thenReturn(signedUpAt);
            when(userJpaEntity.getLastLoggedInAt()).thenReturn(lastLoggedInAt);
            when(userJpaEntity.getStatus()).thenReturn(EntityStatus.ACTIVE);
            when(userJpaEntity.getWithdrawalYn()).thenReturn(withdrawalYn);
            when(userJpaEntity.getWithdrawnAt()).thenReturn(null);
            when(userJpaEntity.getOrderNum()).thenReturn(orderNum);
            when(userJpaEntity.getPenaltyCount()).thenReturn(penaltyCount);
            when(userJpaEntity.getDeactivatedUntil()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            User user = result.get();
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUserKey()).isEqualTo(userKey);
            assertThat(user.getNickname()).isEqualTo(Nickname.of(nickname));
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getFullName()).isEqualTo(fullName);
            assertThat(user.getReferenceCode()).isEqualTo(ReferenceCode.of(referenceCode));
            assertThat(user.getReferredUserCode()).isEqualTo(ReferenceCode.of(referredUserCode));
            assertThat(user.getRole().getId()).isEqualTo("ROLE_BUYER");
            assertThat(user.getRole().getName()).isEqualTo("구매자");
            assertThat(user.getSignedUpAt()).isEqualTo(signedUpAt);
            assertThat(user.getLastLoggedInAt()).isEqualTo(lastLoggedInAt);
            assertThat(user.getOrderNum()).isEqualTo(orderNum);
            assertThat(user.getPenaltyCount()).isEqualTo(penaltyCount);
        }

        @Test
        @DisplayName("nickname이 null이면 Nickname을 null로 변환한다")
        void shouldMapNullNicknameToNull() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getNickname()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getNickname()).isNull();
        }

        @Test
        @DisplayName("referenceCode가 null이면 ReferenceCode를 null로 변환한다")
        void shouldMapNullReferenceCodeToNull() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getReferenceCode()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getReferenceCode()).isNull();
        }

        @Test
        @DisplayName("referredUserCode가 null이면 ReferenceCode를 null로 변환한다")
        void shouldMapNullReferredUserCodeToNull() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getReferredUserCode()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getReferredUserCode()).isNull();
        }

        @Test
        @DisplayName("roleJpaEntity가 null이면 role을 null로 변환한다")
        void shouldMapNullRoleToNull() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMockWithoutRole();
            when(userJpaEntity.getRoleJpaEntity()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRole()).isNull();
        }

        @Test
        @DisplayName("OAuth2 벤더 목록이 null이면 빈 리스트로 변환한다")
        void shouldMapNullVendorsToEmptyList() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getUserOauth2VendorsJpaEntities()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserAuthProviders()).isEmpty();
        }

        @Test
        @DisplayName("OAuth2 벤더 목록을 UserAuthProvider 도메인으로 변환하고 양방향 관계를 설정한다")
        void shouldMapVendorsAndSetBidirectionalRelationship() {
            // given
            UserOauth2VendorJpaEntity kakaoVendor = mock(UserOauth2VendorJpaEntity.class);
            when(kakaoVendor.getAuthVendor()).thenReturn(AuthVendor.KAKAO);
            when(kakaoVendor.getOidcId()).thenReturn("kakao-oidc-123");

            UserOauth2VendorJpaEntity googleVendor = mock(UserOauth2VendorJpaEntity.class);
            when(googleVendor.getAuthVendor()).thenReturn(AuthVendor.GOOGLE);
            when(googleVendor.getOidcId()).thenReturn("google-oidc-456");

            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getUserOauth2VendorsJpaEntities()).thenReturn(List.of(kakaoVendor, googleVendor));

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            User user = result.get();
            List<UserAuthProvider> providers = user.getUserAuthProviders();
            assertThat(providers).hasSize(2);
            assertThat(providers.get(0).getAuthVendor()).isEqualTo(AuthVendor.KAKAO);
            assertThat(providers.get(0).getOidcId()).isEqualTo("kakao-oidc-123");
            assertThat(providers.get(0).getUser()).isSameAs(user);
            assertThat(providers.get(1).getAuthVendor()).isEqualTo(AuthVendor.GOOGLE);
            assertThat(providers.get(1).getOidcId()).isEqualTo("google-oidc-456");
            assertThat(providers.get(1).getUser()).isSameAs(user);
        }

        @Test
        @DisplayName("약관 목록이 null이면 빈 리스트로 변환한다")
        void shouldMapNullUserTermsToEmptyList() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getUserTermsJpaEntities()).thenReturn(null);

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserTerms()).isEmpty();
        }

        @Test
        @DisplayName("약관 목록을 UserTerms 도메인으로 변환한다")
        void shouldMapUserTermsToDomain() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

            TermsJpaEntity termsJpaEntity = mock(TermsJpaEntity.class);
            when(termsJpaEntity.getId()).thenReturn(10L);
            when(termsJpaEntity.getContent()).thenReturn("서비스 이용약관");
            when(termsJpaEntity.getRequiredYn()).thenReturn(true);

            UserTermsJpaEntity userTermsJpaEntity = mock(UserTermsJpaEntity.class);
            when(userTermsJpaEntity.getTermsJpaEntity()).thenReturn(termsJpaEntity);
            when(userTermsJpaEntity.getAgreementYn()).thenReturn(true);
            when(userTermsJpaEntity.getCreatedAt()).thenReturn(createdAt);
            when(userTermsJpaEntity.getModifiedAt()).thenReturn(modifiedAt);
            when(userTermsJpaEntity.getStatus()).thenReturn(EntityStatus.ACTIVE);

            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getUserTermsJpaEntities()).thenReturn(List.of(userTermsJpaEntity));

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            List<UserTerms> userTermsList = result.get().getUserTerms();
            assertThat(userTermsList).hasSize(1);

            UserTerms userTerms = userTermsList.get(0);
            assertThat(userTerms.getAgreementYn()).isTrue();
            assertThat(userTerms.getCreatedAt()).isEqualTo(createdAt);
            assertThat(userTerms.getModifiedAt()).isEqualTo(modifiedAt);

            Terms terms = userTerms.getTerms();
            assertThat(terms.getId()).isEqualTo(10L);
            assertThat(terms.getContent()).isEqualTo("서비스 이용약관");
            assertThat(terms.getRequiredYn()).isTrue();
        }

        @Test
        @DisplayName("빈 문자열 nickname은 null로 변환한다")
        void shouldMapEmptyNicknameToNull() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getNickname()).thenReturn("");

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getNickname()).isNull();
        }

        @Test
        @DisplayName("빈 문자열 referenceCode는 null로 변환한다")
        void shouldMapEmptyReferenceCodeToNull() {
            // given
            UserJpaEntity userJpaEntity = createUserJpaEntityMock();
            when(userJpaEntity.getReferenceCode()).thenReturn("");

            // when
            Optional<User> result = UserJpaEntityToDomainMapper.mapToDomain(userJpaEntity);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getReferenceCode()).isNull();
        }

        private UserJpaEntity createUserJpaEntityMock() {
            RoleJpaEntity roleJpaEntity = mock(RoleJpaEntity.class);
            when(roleJpaEntity.getId()).thenReturn("ROLE_BUYER");
            when(roleJpaEntity.getName()).thenReturn("구매자");

            UserJpaEntity userJpaEntity = createBaseUserJpaEntityMock();
            when(userJpaEntity.getRoleJpaEntity()).thenReturn(roleJpaEntity);
            return userJpaEntity;
        }

        private UserJpaEntity createUserJpaEntityMockWithoutRole() {
            UserJpaEntity userJpaEntity = createBaseUserJpaEntityMock();
            when(userJpaEntity.getRoleJpaEntity()).thenReturn(null);
            return userJpaEntity;
        }

        private UserJpaEntity createBaseUserJpaEntityMock() {
            UserJpaEntity userJpaEntity = mock(UserJpaEntity.class);
            when(userJpaEntity.getId()).thenReturn(1L);
            when(userJpaEntity.getUserKey()).thenReturn(UUID.randomUUID());
            when(userJpaEntity.getNickname()).thenReturn("테스트닉네임");
            when(userJpaEntity.getEmail()).thenReturn("test@example.com");
            when(userJpaEntity.getPassword()).thenReturn("encoded-password");
            when(userJpaEntity.getFullName()).thenReturn("홍길동");
            when(userJpaEntity.getPhoneNumber()).thenReturn("010-0001-0001");
            when(userJpaEntity.getReferenceCode()).thenReturn("ABC123");
            when(userJpaEntity.getReferredUserCode()).thenReturn("DEF456");
            when(userJpaEntity.getUserOauth2VendorsJpaEntities()).thenReturn(List.of());
            when(userJpaEntity.getUserTermsJpaEntities()).thenReturn(List.of());
            when(userJpaEntity.getSignedUpAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
            when(userJpaEntity.getLastLoggedInAt()).thenReturn(LocalDateTime.of(2026, 4, 14, 10, 0));
            when(userJpaEntity.getStatus()).thenReturn(EntityStatus.ACTIVE);
            when(userJpaEntity.getWithdrawalYn()).thenReturn(false);
            when(userJpaEntity.getWithdrawnAt()).thenReturn(null);
            when(userJpaEntity.getOrderNum()).thenReturn(100L);
            when(userJpaEntity.getPenaltyCount()).thenReturn(0);
            when(userJpaEntity.getDeactivatedUntil()).thenReturn(null);
            return userJpaEntity;
        }
    }

    @Nested
    @DisplayName("mapToTermsDomain")
    class MapToTermsDomainTest {

        @Test
        @DisplayName("null 입력 시 빈 Optional을 반환한다")
        void shouldReturnEmptyOptionalWhenInputIsNull() {
            // when
            Optional<Terms> result = UserJpaEntityToDomainMapper.mapToTermsDomain(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("TermsJpaEntity를 Terms 도메인으로 변환한다")
        void shouldMapTermsJpaEntityToDomain() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime modifiedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

            TermsJpaEntity termsJpaEntity = mock(TermsJpaEntity.class);
            when(termsJpaEntity.getId()).thenReturn(1L);
            when(termsJpaEntity.getContent()).thenReturn("개인정보 처리방침");
            when(termsJpaEntity.getRequiredYn()).thenReturn(true);
            when(termsJpaEntity.getCreatedAt()).thenReturn(createdAt);
            when(termsJpaEntity.getModifiedAt()).thenReturn(modifiedAt);
            when(termsJpaEntity.getStatus()).thenReturn(EntityStatus.ACTIVE);

            // when
            Optional<Terms> result = UserJpaEntityToDomainMapper.mapToTermsDomain(termsJpaEntity);

            // then
            assertThat(result).isPresent();
            Terms terms = result.get();
            assertThat(terms.getId()).isEqualTo(1L);
            assertThat(terms.getContent()).isEqualTo("개인정보 처리방침");
            assertThat(terms.getRequiredYn()).isTrue();
            assertThat(terms.getCreatedAt()).isEqualTo(createdAt);
            assertThat(terms.getModifiedAt()).isEqualTo(modifiedAt);
        }
    }
}
