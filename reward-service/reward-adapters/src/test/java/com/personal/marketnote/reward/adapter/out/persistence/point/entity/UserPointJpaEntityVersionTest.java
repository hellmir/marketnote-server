package com.personal.marketnote.reward.adapter.out.persistence.point.entity;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuditConfig.class, QuerydslConfig.class})
@DisplayName("UserPointJpaEntity @Version 테스트")
class UserPointJpaEntityVersionTest {

    @PersistenceContext
    private EntityManager em;

    private UserPointJpaEntity persistUserPoint(Long userId, String userKey, long amount) {
        UserPoint domain = UserPoint.from(UserPointSnapshotState.builder()
                .userId(userId)
                .userKey(userKey)
                .amount(PointAmount.of(amount))
                .addExpectedAmount(PointAmount.of(0L))
                .expireExpectedAmount(PointAmount.of(0L))
                .build());
        UserPointJpaEntity entity = UserPointJpaEntity.from(domain);
        em.persist(entity);
        em.flush();
        return entity;
    }

    @Test
    @DisplayName("신규 저장 시 version은 0으로 초기화된다")
    void shouldInitializeVersionToZeroOnInsert() {
        UserPointJpaEntity saved = persistUserPoint(1L, "user-key-1", 5_000L);
        assertThat(saved.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("갱신 시 version이 증가한다")
    void shouldIncrementVersionOnUpdate() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        em.clear();

        UserPointJpaEntity entity = em.find(UserPointJpaEntity.class, 1L);
        UserPoint changed = entity.toDomain().withAmount(7_000L);
        em.merge(UserPointJpaEntity.from(changed));
        em.flush();
        em.clear();

        UserPointJpaEntity reloaded = em.find(UserPointJpaEntity.class, 1L);
        assertThat(reloaded.getVersion()).isEqualTo(1L);
        assertThat(reloaded.getAmount()).isEqualTo(7_000L);
    }

    @Test
    @DisplayName("version 필드는 @Version으로 매핑되어 엔티티 상태와 함께 조회된다")
    void shouldLoadVersionField() {
        persistUserPoint(1L, "user-key-1", 5_000L);
        em.clear();

        UserPointJpaEntity reloaded = em.find(UserPointJpaEntity.class, 1L);

        assertThat(reloaded.getVersion()).isNotNull();
    }
}
