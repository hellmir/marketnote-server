package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.authentication.entity.RoleJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserJpaEntity;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserAuthProvider;
import com.personal.marketnote.user.domain.user.UserSnapshotState;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
        if (em.find(RoleJpaEntity.class, "ROLE_BUYER") == null) {
            em.persist(RoleJpaEntity.from(Role.getBuyer()));
            em.flush();
        }
    }

    @Test
    @DisplayName("findAllStatusUserByAuthVendorAndOidcId는 ACTIVE 회원을 조회한다")
    void findAllStatusReturnsActiveUser() {
        Long savedId = persistUser(
                "active-user", "010-1111-2222", "active@test.com",
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
                "inactive-user", "010-1111-3333", "inactive@test.com",
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
                "hidden-user", "010-1111-4444", "hidden@test.com",
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
                "legacy-filter", "010-1111-5555", "legacy@test.com",
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

    private Long persistUser(
            String nickname, String phoneNumber, String email,
            AuthVendor vendor, String oidcId, EntityStatus targetStatus
    ) {
        User user = User.from(
                UserSnapshotState.builder()
                        .userKey(UUID.randomUUID())
                        .nickname(nickname)
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
