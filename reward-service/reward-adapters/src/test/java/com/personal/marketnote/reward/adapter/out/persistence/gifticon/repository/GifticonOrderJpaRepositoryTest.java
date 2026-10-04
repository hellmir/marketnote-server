package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonOrderJpaEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderCreateState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("GifticonOrderJpaRepository 테스트")
class GifticonOrderJpaRepositoryTest {

    @Autowired
    private GifticonOrderJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private GifticonOrderJpaEntity persistOrder(Long userId, String trId, GifticonOrderStatus status, LocalDate validEndDate) {
        GifticonOrder domain = GifticonOrder.from(GifticonOrderCreateState.builder()
                .userId(userId)
                .goodsCode("G001")
                .goodsName("테스트 상품")
                .brandName("스타벅스")
                .productImageUrl("http://img.test.com/p.jpg")
                .trId(trId)
                .cashPrice(10_000L)
                .build());
        if (status != GifticonOrderStatus.PENDING) {
            domain.issue("http://img.test.com/c.jpg", "1234-5678-9012", "ORD" + trId, validEndDate);
            if (status == GifticonOrderStatus.CANCELLED) {
                domain.cancel();
            }
        }
        GifticonOrderJpaEntity entity = GifticonOrderJpaEntity.from(domain);
        em.persist(entity);
        em.flush();
        return entity;
    }

    @Test
    @DisplayName("findByTrId는 trId로 주문을 조회한다")
    void shouldFindByTrId() {
        persistOrder(1L, "TR001", GifticonOrderStatus.PENDING, null);
        em.clear();
        assertThat(repository.findByTrId("TR001")).isPresent();
    }

    @Test
    @DisplayName("existsByTrId는 trId 존재 여부를 반환한다")
    void shouldExistsByTrId() {
        persistOrder(1L, "TR002", GifticonOrderStatus.PENDING, null);
        assertThat(repository.existsByTrId("TR002")).isTrue();
        assertThat(repository.existsByTrId("TR-NONE")).isFalse();
    }

    @Test
    @DisplayName("findByUserIdAndStatusesOrderByCreatedAtDesc는 cursor=-1이면 모든 주문을 createdAt desc 정렬로 반환한다")
    void shouldReturnAllOrdersByCreatedAtDescWhenCursorIsMinusOne() {
        persistOrder(1L, "TR-A", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(10));
        persistOrder(1L, "TR-B", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(20));
        em.clear();

        List<GifticonOrderJpaEntity> result = repository.findByUserIdAndStatusesOrderByCreatedAtDesc(
                1L, List.of(GifticonOrderStatus.ISSUED), -1L, PageRequest.of(0, 10));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByUserIdAndStatusesOrderByCreatedAtDesc는 cursor 이전 주문만 반환한다")
    void shouldReturnOrdersBeforeCursor() {
        GifticonOrderJpaEntity first = persistOrder(1L, "TR-X", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(10));
        persistOrder(1L, "TR-Y", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(20));
        em.clear();

        List<GifticonOrderJpaEntity> result = repository.findByUserIdAndStatusesOrderByCreatedAtDesc(
                1L, List.of(GifticonOrderStatus.ISSUED), first.getId() + 100, PageRequest.of(0, 10));

        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("findByUserIdAndStatusesOrderByValidEndDateAsc는 validEndDate 오름차순으로 반환한다")
    void shouldOrderByValidEndDateAsc() {
        persistOrder(1L, "TR-1", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(20));
        persistOrder(1L, "TR-2", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(5));
        persistOrder(1L, "TR-3", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(10));
        em.clear();

        List<GifticonOrderJpaEntity> result = repository.findByUserIdAndStatusesOrderByValidEndDateAsc(
                1L, List.of(GifticonOrderStatus.ISSUED), PageRequest.of(0, 10));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTrId()).isEqualTo("TR-2");
        assertThat(result.get(2).getTrId()).isEqualTo("TR-1");
    }

    @Test
    @DisplayName("countByUserIdAndOrderStatusIn은 사용자별 상태별 주문 수를 반환한다")
    void shouldCountByUserIdAndStatuses() {
        persistOrder(1L, "TR-COUNT-1", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(10));
        persistOrder(1L, "TR-COUNT-2", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(20));
        em.clear();

        long count = repository.countByUserIdAndOrderStatusIn(1L, List.of(GifticonOrderStatus.ISSUED));
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByIdAndUserId는 본인 주문만 조회한다")
    void shouldFindByIdAndUserId() {
        GifticonOrderJpaEntity entity = persistOrder(1L, "TR-OWN", GifticonOrderStatus.PENDING, null);
        em.clear();

        assertThat(repository.findByIdAndUserId(entity.getId(), 1L)).isPresent();
        assertThat(repository.findByIdAndUserId(entity.getId(), 999L)).isEmpty();
    }

    @Test
    @DisplayName("findAllByOrderStatus는 특정 상태의 모든 주문을 반환한다")
    void shouldFindAllByOrderStatus() {
        persistOrder(1L, "TR-PEND-1", GifticonOrderStatus.PENDING, null);
        persistOrder(1L, "TR-PEND-2", GifticonOrderStatus.PENDING, null);
        persistOrder(1L, "TR-ISSUED-1", GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(10));
        em.clear();

        List<GifticonOrderJpaEntity> pending = repository.findAllByOrderStatus(GifticonOrderStatus.PENDING);
        assertThat(pending).hasSize(2);
    }
}
