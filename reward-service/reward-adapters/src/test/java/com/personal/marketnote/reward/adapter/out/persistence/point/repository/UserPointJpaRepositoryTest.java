package com.personal.marketnote.reward.adapter.out.persistence.point.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointJpaEntity;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("UserPointJpaRepository 테스트")
class UserPointJpaRepositoryTest {

    @Autowired
    private UserPointJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private void persistUserPoint(Long userId, String userKey, long amount) {
        UserPoint domain = UserPoint.from(UserPointSnapshotState.builder()
                .userId(userId)
                .userKey(userKey)
                .amount(PointAmount.of(amount))
                .addExpectedAmount(PointAmount.of(0L))
                .expireExpectedAmount(PointAmount.of(0L))
                .build());
        em.persist(UserPointJpaEntity.from(domain));
        em.flush();
    }

    @Test
    @DisplayName("existsByUserId는 사용자 ID 존재 여부를 반환한다")
    void shouldExistsByUserId() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        assertThat(repository.existsByUserId(1L)).isTrue();
        assertThat(repository.existsByUserId(999L)).isFalse();
    }

    @Test
    @DisplayName("existsByUserKey는 user key 존재 여부를 반환한다")
    void shouldExistsByUserKey() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        assertThat(repository.existsByUserKey("user-key-1")).isTrue();
        assertThat(repository.existsByUserKey("user-key-x")).isFalse();
    }

    @Test
    @DisplayName("findByUserId는 사용자 ID로 포인트 엔티티를 조회한다")
    void shouldFindByUserId() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        em.clear();
        Optional<UserPointJpaEntity> found = repository.findByUserId(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(5_000L);
    }

    @Test
    @DisplayName("findByUserKey는 user key로 포인트 엔티티를 조회한다")
    void shouldFindByUserKey() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        em.clear();
        Optional<UserPointJpaEntity> found = repository.findByUserKey("user-key-1");
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findWithLockingByUserId는 비관적 잠금으로 포인트 엔티티를 조회한다")
    void shouldFindWithLockingByUserId() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        em.clear();
        Optional<UserPointJpaEntity> found = repository.findWithLockingByUserId(1L);
        assertThat(found).isPresent();
    }
}
