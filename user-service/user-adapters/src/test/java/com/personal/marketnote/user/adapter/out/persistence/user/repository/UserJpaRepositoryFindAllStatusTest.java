package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.authentication.entity.RoleJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserJpaEntity;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.Nickname;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserAuthProvider;
import com.personal.marketnote.user.domain.user.UserSnapshotState;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
class UserJpaRepositoryFindAllStatusTest {
    @Autowired
    private UserJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // 순수 null 체크 — em.find() 반환 Entity에 FormatValidator.hasValue(Object) 사용 시 toString() 평가로 의미 왜곡 가능
        if (em.find(RoleJpaEntity.class, "ROLE_BUYER") == null) {
            em.persist(RoleJpaEntity.from(Role.getBuyer()));
            em.flush();
        }
    }

    @Test
    @DisplayName("findAllStatusUserByAuthVendorAndOidcId는 ACTIVE 회원을 조회한다")
    void findAllStatusReturnsActiveUser() {
        Long savedId = persistUser(
                "activeUser", "010-1111-2222", "active@test.com",
                AuthVendor.KAKAO, "oidc-active", EntityStatus.ACTIVE
        );

        Optional<UserJpaEntity> result = repository.findAllStatusUserByAuthVendorAndOidcId(
                AuthVendor.KAKAO, "oidc-active"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
        assertThat(result.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("findAllStatusUserByAuthVendorAndOidcId는 INACTIVE 회원도 조회한다")
    void findAllStatusReturnsInactiveUser() {
        Long savedId = persistUser(
                "inactUser", "010-1111-3333", "inactive@test.com",
                AuthVendor.KAKAO, "oidc-inactive", EntityStatus.INACTIVE
        );

        Optional<UserJpaEntity> result = repository.findAllStatusUserByAuthVendorAndOidcId(
                AuthVendor.KAKAO, "oidc-inactive"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
        assertThat(result.get().getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    @DisplayName("findAllStatusUserByAuthVendorAndOidcId는 UNEXPOSED 회원도 조회한다")
    void findAllStatusReturnsUnexposedUser() {
        Long savedId = persistUser(
                "hiddenUser", "010-1111-4444", "hidden@test.com",
                AuthVendor.KAKAO, "oidc-hidden", EntityStatus.UNEXPOSED
        );

        Optional<UserJpaEntity> result = repository.findAllStatusUserByAuthVendorAndOidcId(
                AuthVendor.KAKAO, "oidc-hidden"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedId);
        assertThat(result.get().getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
    }

    @Test
    @DisplayName("기존 findByAuthVendorAndOidcId는 여전히 INACTIVE 회원을 조회하지 않는다")
    void existingQueryStillExcludesInactiveUser() {
        persistUser(
                "legacyUser", "010-1111-5555", "legacy@test.com",
                AuthVendor.KAKAO, "oidc-legacy", EntityStatus.INACTIVE
        );

        Optional<UserJpaEntity> result = repository.findByAuthVendorAndOidcId(
                AuthVendor.KAKAO, "oidc-legacy"
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllStatusUserByAuthVendorAndOidcId는 일치하는 회원이 없으면 empty를 반환한다")
    void findAllStatusReturnsEmptyWhenNoMatch() {
        Optional<UserJpaEntity> result = repository.findAllStatusUserByAuthVendorAndOidcId(
                AuthVendor.KAKAO, "oidc-missing"
        );

        assertThat(result).isEmpty();
    }

    @Nested
    @DisplayName("findAllStatusUsersByPage")
    class FindAllStatusUsersByPage {
        @Test
        @DisplayName("searchKeyword가 빈 문자열(\"\")이면 sentinel 매칭되어 전체 활성 사용자를 반환한다")
        void returnsAllActiveUsersWhenKeywordEmpty() {
            // given
            persistUser(
                    "sentOne", "010-2000-0001", "sentinel1@test.com",
                    AuthVendor.KAKAO, "oidc-sentinel-1", EntityStatus.ACTIVE
            );
            persistUser(
                    "sentTwo", "010-2000-0002", "sentinel2@test.com",
                    AuthVendor.KAKAO, "oidc-sentinel-2", EntityStatus.ACTIVE
            );
            // INACTIVE 사용자: 결과에 포함되지 않아야 함
            persistUser(
                    "sentOff", "010-2000-0003", "sentinel3@test.com",
                    AuthVendor.KAKAO, "oidc-sentinel-3", EntityStatus.INACTIVE
            );

            // when
            Page<UserJpaEntity> result = repository.findAllStatusUsersByPage(
                    PageRequest.of(0, 10), false, false, false, false, false, ""
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(UserJpaEntity::getStatus)
                    .containsOnly(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("searchKeyword가 nickname을 포함하면 byNickname=true일 때 필터링된 사용자만 반환한다")
        void filtersByNicknameWhenByNicknameTrue() {
            // given
            persistUser(
                    "alphaNick", "010-2100-0001", "alpha@test.com",
                    AuthVendor.KAKAO, "oidc-alpha", EntityStatus.ACTIVE
            );
            persistUser(
                    "betaNick", "010-2100-0002", "beta@test.com",
                    AuthVendor.KAKAO, "oidc-beta", EntityStatus.ACTIVE
            );

            // when
            Page<UserJpaEntity> result = repository.findAllStatusUsersByPage(
                    PageRequest.of(0, 10), false, true, false, false, false, "alpha"
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNickname()).isEqualTo("alphaNick");
        }

        @Test
        @DisplayName("searchKeyword는 있으나 모든 by* 플래그가 false이면 빈 결과를 반환한다")
        void returnsEmptyWhenKeywordPresentButAllFlagsFalse() {
            // given
            persistUser(
                    "flagOff", "010-2200-0001", "flagoff@test.com",
                    AuthVendor.KAKAO, "oidc-flagoff", EntityStatus.ACTIVE
            );

            // when
            Page<UserJpaEntity> result = repository.findAllStatusUsersByPage(
                    PageRequest.of(0, 10), false, false, false, false, false, "flagOff"
            );

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("searchKeyword 대소문자와 무관하게 nickname LIKE 매칭이 동작한다")
        void matchesNicknameCaseInsensitively() {
            // given
            persistUser(
                    "MixNick", "010-2300-0001", "mixed@test.com",
                    AuthVendor.KAKAO, "oidc-mixed", EntityStatus.ACTIVE
            );

            // when
            Page<UserJpaEntity> result = repository.findAllStatusUsersByPage(
                    PageRequest.of(0, 10), false, true, false, false, false, "mixnick"
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNickname()).isEqualTo("MixNick");
        }
    }

    private Long persistUser(
            String nickname, String phoneNumber, String email,
            AuthVendor vendor, String oidcId, EntityStatus targetStatus
    ) {
        User user = User.from(
                UserSnapshotState.builder()
                        .userKey(UUID.randomUUID())
                        .nickname(Nickname.of(nickname))
                        .email(email)
                        .password("encoded-password")
                        .fullName("테스트")
                        .phoneNumber(phoneNumber)
                        .role(Role.getBuyer())
                        .userAuthProviders(List.of(UserAuthProvider.of(vendor, oidcId)))
                        .userTerms(List.of())
                        .signedUpAt(LocalDateTime.now())
                        .lastLoggedInAt(LocalDateTime.now())
                        .status(EntityStatus.ACTIVE)
                        .withdrawalYn(false)
                        .penaltyCount(0)
                        .build()
        );

        UserJpaEntity entity = UserJpaEntity.from(user);
        UserJpaEntity saved = repository.save(entity);
        saved.setIdToOrderNum();
        em.flush();

        if (targetStatus == EntityStatus.INACTIVE) {
            em.createNativeQuery("UPDATE users SET status = 'INACTIVE' WHERE id = :id")
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        if (targetStatus == EntityStatus.UNEXPOSED) {
            em.createNativeQuery("UPDATE users SET status = 'UNEXPOSED' WHERE id = :id")
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        em.flush();
        em.clear();

        return saved.getId();
    }
}
