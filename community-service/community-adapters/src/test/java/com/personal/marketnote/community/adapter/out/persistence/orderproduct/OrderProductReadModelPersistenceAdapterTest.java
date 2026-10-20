package com.personal.marketnote.community.adapter.out.persistence.orderproduct;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.orderproduct.entity.OrderProductReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.orderproduct.repository.OrderProductReadModelJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, QuerydslConfig.class, OrderProductReadModelPersistenceAdapter.class})
class OrderProductReadModelPersistenceAdapterTest {

    @Autowired
    private OrderProductReadModelPersistenceAdapter adapter;

    @Autowired
    private OrderProductReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 주문 상품 ReadModel을 저장한다")
        void insertsNewOrderProduct() {
            // when
            adapter.upsert(10L, 100L, 1L, 12345L);

            // then
            Optional<OrderProductReadModelJpaEntity> entity =
                    repository.findByOrderIdAndPricePolicyId(10L, 100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getOrderId()).isEqualTo(10L);
            assertThat(entity.get().getPricePolicyId()).isEqualTo(100L);
            assertThat(entity.get().getBuyerId()).isEqualTo(1L);
            assertThat(entity.get().getUnitAmount()).isEqualTo(12345L);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 (orderId, pricePolicyId)로 upsert 시 기존 데이터의 unitAmount를 업데이트한다")
        void updatesExistingOrderProduct() {
            // given
            adapter.upsert(10L, 100L, 1L, 10000L);

            // when
            adapter.upsert(10L, 100L, 1L, 20000L);

            // then
            Optional<OrderProductReadModelJpaEntity> entity =
                    repository.findByOrderIdAndPricePolicyId(10L, 100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getUnitAmount()).isEqualTo(20000L);
            assertThat(repository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("비활성화 상태의 ReadModel에 upsert 시 다시 활성화된다")
        void reactivatesInactiveOrderProduct() {
            // given
            adapter.upsert(10L, 100L, 1L, 10000L);
            repository.findByOrderIdAndPricePolicyId(10L, 100L)
                    .ifPresent(OrderProductReadModelJpaEntity::markInactive);

            // when
            adapter.upsert(10L, 100L, 1L, 10000L);

            // then
            Optional<OrderProductReadModelJpaEntity> entity =
                    repository.findByOrderIdAndPricePolicyId(10L, 100L);
            assertThat(entity).isPresent();
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일 orderId에 서로 다른 pricePolicyId는 별도 레코드로 저장된다")
        void storesMultiplePricePoliciesPerOrder() {
            // when
            adapter.upsert(10L, 100L, 1L, 10000L);
            adapter.upsert(10L, 200L, 1L, 20000L);

            // then
            assertThat(repository.findAll()).hasSize(2);
            assertThat(repository.findByOrderIdAndPricePolicyId(10L, 100L)).isPresent();
            assertThat(repository.findByOrderIdAndPricePolicyId(10L, 200L)).isPresent();
        }
    }

    @Nested
    @DisplayName("findUnitAmountByOrderIdAndPricePolicyId")
    class FindUnitAmount {

        @Test
        @DisplayName("ACTIVE ReadModel이 존재하면 unitAmount를 반환한다")
        void returnsUnitAmountWhenActive() {
            // given
            adapter.upsert(10L, 100L, 1L, 12345L);

            // when
            Optional<Long> unitAmount = adapter.findUnitAmountByOrderIdAndPricePolicyId(10L, 100L);

            // then
            assertThat(unitAmount).contains(12345L);
        }

        @Test
        @DisplayName("ReadModel이 존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenAbsent() {
            // when
            Optional<Long> unitAmount = adapter.findUnitAmountByOrderIdAndPricePolicyId(99L, 999L);

            // then
            assertThat(unitAmount).isEmpty();
        }

        @Test
        @DisplayName("INACTIVE 상태의 ReadModel은 조회되지 않는다")
        void returnsEmptyWhenInactive() {
            // given
            adapter.upsert(10L, 100L, 1L, 12345L);
            repository.findByOrderIdAndPricePolicyId(10L, 100L)
                    .ifPresent(OrderProductReadModelJpaEntity::markInactive);

            // when
            Optional<Long> unitAmount = adapter.findUnitAmountByOrderIdAndPricePolicyId(10L, 100L);

            // then
            assertThat(unitAmount).isEmpty();
        }
    }
}
