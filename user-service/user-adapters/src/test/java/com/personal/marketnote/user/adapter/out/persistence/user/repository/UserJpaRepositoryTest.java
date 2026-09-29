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
class UserJpaRepositoryTest {
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

    private Long persistUser(String nickname, String phoneNumber, String email, String referenceCode, EntityStatus targetStatus) {
        return persistUserWithVendor(nickname, phoneNumber, email, referenceCode, AuthVendor.KAKAO, "oidc-" + nickname, targetStatus);
    }

    private Long persistUserWithVendor(
            String nickname, String phoneNumber, String email, String referenceCode,
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
                        .referenceCode(referenceCode != null ? com.personal.marketnote.user.domain.user.ReferenceCode.of(referenceCode) : null)
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

    @Nested
    @DisplayName("existsByNickname")
    class ExistsByNickname {
        @Test
        @DisplayName("ACTIVE 회원의 닉네임이 존재하면 true를 반환한다")
        void returnsTrueWhenActiveNicknameExists() {
            persistUser("testNick", "010-0001-0001", "nick@test.com", null, EntityStatus.ACTIVE);

            assertThat(repository.existsByNickname("testNick")).isTrue();
        }

        @Test
        @DisplayName("INACTIVE 회원의 닉네임은 false를 반환한다")
        void returnsFalseForInactiveUser() {
            persistUser("비활성닉", "010-0001-0002", "inactive@test.com", null, EntityStatus.INACTIVE);

            assertThat(repository.existsByNickname("비활성닉")).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 닉네임은 false를 반환한다")
        void returnsFalseWhenNotExists() {
            assertThat(repository.existsByNickname("nonExistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {
        @Test
        @DisplayName("이메일이 존재하면 true를 반환한다")
        void returnsTrueWhenEmailExists() {
            persistUser("emailUser", "010-0002-0001", "exists@test.com", null, EntityStatus.ACTIVE);

            assertThat(repository.existsByEmail("exists@test.com")).isTrue();
        }

        @Test
        @DisplayName("이메일이 존재하지 않으면 false를 반환한다")
        void returnsFalseWhenNotExists() {
            assertThat(repository.existsByEmail("no@test.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByPhoneNumber")
    class ExistsByPhoneNumber {
        @Test
        @DisplayName("ACTIVE 회원의 전화번호가 존재하면 true를 반환한다")
        void returnsTrueWhenActivePhoneExists() {
            persistUser("phoneUser", "010-0003-0001", "phone@test.com", null, EntityStatus.ACTIVE);

            assertThat(repository.existsByPhoneNumber("010-0003-0001")).isTrue();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {
        @Test
        @DisplayName("ACTIVE 회원을 이메일로 조회한다")
        void returnsActiveUserByEmail() {
            Long id = persistUser("findEmail", "010-0004-0001", "find@test.com", null, EntityStatus.ACTIVE);

            Optional<UserJpaEntity> result = repository.findByEmail("find@test.com");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("INACTIVE 회원은 조회하지 않는다")
        void doesNotReturnInactiveUser() {
            persistUser("비활성이메일", "010-0004-0002", "findInactive@test.com", null, EntityStatus.INACTIVE);

            Optional<UserJpaEntity> result = repository.findByEmail("findInactive@test.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPhoneNumber")
    class FindByPhoneNumber {
        @Test
        @DisplayName("ACTIVE 회원을 전화번호로 조회한다")
        void returnsActiveUserByPhone() {
            Long id = persistUser("findPhone", "010-0005-0001", "findphone@test.com", null, EntityStatus.ACTIVE);

            Optional<UserJpaEntity> result = repository.findByPhoneNumber("010-0005-0001");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("findByReferenceCode")
    class FindByReferenceCode {
        @Test
        @DisplayName("ACTIVE 회원을 초대 코드로 조회한다")
        void returnsActiveUserByRefCode() {
            Long id = persistUser("refUser", "010-0006-0001", "ref@test.com", "ABC123", EntityStatus.ACTIVE);

            Optional<UserJpaEntity> result = repository.findByReferenceCode("ABC123");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("존재하지 않는 초대 코드는 빈 Optional을 반환한다")
        void returnsEmptyForMissingCode() {
            assertThat(repository.findByReferenceCode("MISSING")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllStatusUserById")
    class FindAllStatusUserById {
        @Test
        @DisplayName("orderNum으로 모든 상태의 회원을 조회한다")
        void returnsUserByOrderNum() {
            Long id = persistUser("allStatus", "010-0007-0001", "allstatus@test.com", null, EntityStatus.ACTIVE);

            Optional<UserJpaEntity> result = repository.findAllStatusUserById(id);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("INACTIVE 회원도 조회한다")
        void returnsInactiveUser() {
            Long id = persistUser("inactAll", "010-0007-0002", "inactall@test.com", null, EntityStatus.INACTIVE);

            Optional<UserJpaEntity> result = repository.findAllStatusUserById(id);

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findAllStatusUserByEmail")
    class FindAllStatusUserByEmail {
        @Test
        @DisplayName("이메일로 모든 상태의 회원을 조회한다")
        void returnsUserByEmail() {
            persistUser("allEmail", "010-0008-0001", "allemail@test.com", null, EntityStatus.INACTIVE);

            Optional<UserJpaEntity> result = repository.findAllStatusUserByEmail("allemail@test.com");

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findAllStatusUsers")
    class FindAllStatusUsers {
        @Test
        @DisplayName("모든 상태의 회원 목록을 조회한다")
        void returnsAllUsers() {
            persistUser("user1", "010-0009-0001", "user1@test.com", null, EntityStatus.ACTIVE);
            persistUser("user2", "010-0009-0002", "user2@test.com", null, EntityStatus.INACTIVE);

            List<UserJpaEntity> result = repository.findAllStatusUsers();

            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findAllStatusUsersByPage")
    class FindAllStatusUsersByPage {
        @Test
        @DisplayName("닉네임 검색으로 페이징 조회한다")
        void searchesByNickname() {
            persistUser("searchNick", "010-0010-0001", "search@test.com", null, EntityStatus.ACTIVE);

            Page<UserJpaEntity> result = repository.findAllStatusUsersByPage(
                    PageRequest.of(0, 10), false, true, false, false, false, "searchNick"
            );

            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).getNickname()).isEqualTo("searchNick");
        }

        @Test
        @DisplayName("검색 키워드가 빈 문자열이면 전체를 조회한다")
        void returnsAllWhenKeywordEmpty() {
            persistUser("anyUser", "010-0010-0002", "any@test.com", null, EntityStatus.ACTIVE);

            Page<UserJpaEntity> result = repository.findAllStatusUsersByPage(
                    PageRequest.of(0, 10), false, false, false, false, false, ""
            );

            assertThat(result.getContent()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByDeactivatedUntilExpired")
    class FindAllByDeactivatedUntilExpired {
        @Test
        @DisplayName("비활성화 기간이 만료된 INACTIVE 회원을 조회한다")
        void returnsExpiredDeactivatedUsers() {
            Long id = persistUser("deactUser", "010-0011-0001", "deact@test.com", null, EntityStatus.INACTIVE);
            em.createNativeQuery("UPDATE users SET deactivated_until = :until WHERE id = :id")
                    .setParameter("until", LocalDateTime.of(2026, 4, 1, 0, 0))
                    .setParameter("id", id)
                    .executeUpdate();
            em.flush();
            em.clear();

            List<UserJpaEntity> result = repository.findAllByDeactivatedUntilExpired(
                    LocalDateTime.of(2026, 4, 14, 12, 0)
            );

            assertThat(result).isNotEmpty();
            assertThat(result.stream().anyMatch(u -> u.getId().equals(id))).isTrue();
        }

        @Test
        @DisplayName("비활성화 기간이 아직 만료되지 않은 회원은 조회하지 않는다")
        void doesNotReturnNotExpiredUsers() {
            Long id = persistUser("notExpired", "010-0011-0002", "notexpired@test.com", null, EntityStatus.INACTIVE);
            em.createNativeQuery("UPDATE users SET deactivated_until = :until WHERE id = :id")
                    .setParameter("until", LocalDateTime.of(2026, 12, 31, 23, 59))
                    .setParameter("id", id)
                    .executeUpdate();
            em.flush();
            em.clear();

            List<UserJpaEntity> result = repository.findAllByDeactivatedUntilExpired(
                    LocalDateTime.of(2026, 4, 14, 12, 0)
            );

            assertThat(result.stream().noneMatch(u -> u.getId().equals(id))).isTrue();
        }
    }
}
