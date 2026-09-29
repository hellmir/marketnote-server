package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.authentication.entity.RoleJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.LoginHistoryJpaEntity;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
class LoginHistoryJpaRepositoryTest {
    @Autowired
    private LoginHistoryJpaRepository loginHistoryJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @PersistenceContext
    private EntityManager em;

    private UserJpaEntity savedUser;

    @BeforeEach
    void setUp() {
        if (em.find(RoleJpaEntity.class, "ROLE_BUYER") == null) {
            em.persist(RoleJpaEntity.from(Role.getBuyer()));
            em.flush();
        }

        User user = User.from(
                UserSnapshotState.builder()
                        .userKey(UUID.randomUUID())
                        .nickname(Nickname.of("테스트유저"))
                        .email("login@test.com")
                        .password("encoded-password")
                        .fullName("테스트")
                        .phoneNumber("010-9999-0001")
                        .role(Role.getBuyer())
                        .userAuthProviders(List.of(UserAuthProvider.of(AuthVendor.KAKAO, "oidc-login")))
                        .userTerms(List.of())
                        .signedUpAt(LocalDateTime.now())
                        .lastLoggedInAt(LocalDateTime.now())
                        .status(EntityStatus.ACTIVE)
                        .withdrawalYn(false)
                        .penaltyCount(0)
                        .build()
        );
        savedUser = userJpaRepository.save(UserJpaEntity.from(user));
        savedUser.setIdToOrderNum();
        em.flush();
        em.clear();

        savedUser = userJpaRepository.findById(savedUser.getId()).orElseThrow();
    }

    private void persistLoginHistory(UserJpaEntity userEntity, String ipAddress) {
        LoginHistoryJpaEntity entity = LoginHistoryJpaEntity.of(userEntity, AuthVendor.KAKAO, ipAddress);
        loginHistoryJpaRepository.save(entity);
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findLoginHistoriesByUserId")
    class FindLoginHistoriesByUserId {
        @Test
        @DisplayName("사용자 ID로 로그인 이력을 페이징 조회한다")
        void returnsLoginHistoriesByUserId() {
            // given
            persistLoginHistory(savedUser, "192.168.1.1");
            persistLoginHistory(savedUser, "192.168.1.2");

            // when
            Page<LoginHistoryJpaEntity> result = loginHistoryJpaRepository.findLoginHistoriesByUserId(
                    PageRequest.of(0, 10), savedUser.getId()
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("다른 사용자의 로그인 이력은 조회하지 않는다")
        void doesNotReturnOtherUserHistories() {
            // given
            persistLoginHistory(savedUser, "192.168.1.1");

            // when
            Page<LoginHistoryJpaEntity> result = loginHistoryJpaRepository.findLoginHistoriesByUserId(
                    PageRequest.of(0, 10), 999999L
            );

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findLoginHistoriesByPage")
    class FindLoginHistoriesByPage {
        @Test
        @DisplayName("IP 주소로 검색한다")
        void searchesByIpAddress() {
            // given
            persistLoginHistory(savedUser, "10.0.0.1");
            persistLoginHistory(savedUser, "192.168.1.1");

            // when
            Page<LoginHistoryJpaEntity> result = loginHistoryJpaRepository.findLoginHistoriesByPage(
                    PageRequest.of(0, 10), false, true, "10.0.0"
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getIpAddress()).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("검색 키워드가 빈 문자열이면 전체를 조회한다")
        void returnsAllWhenKeywordEmpty() {
            // given
            persistLoginHistory(savedUser, "10.0.0.1");

            // when
            Page<LoginHistoryJpaEntity> result = loginHistoryJpaRepository.findLoginHistoriesByPage(
                    PageRequest.of(0, 10), false, false, ""
            );

            // then
            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("사용자 ID로 검색한다")
        void searchesByUserId() {
            // given
            persistLoginHistory(savedUser, "10.0.0.1");

            // when
            Page<LoginHistoryJpaEntity> result = loginHistoryJpaRepository.findLoginHistoriesByPage(
                    PageRequest.of(0, 10), true, false, String.valueOf(savedUser.getId())
            );

            // then
            assertThat(result.getContent()).isNotEmpty();
        }
    }
}
