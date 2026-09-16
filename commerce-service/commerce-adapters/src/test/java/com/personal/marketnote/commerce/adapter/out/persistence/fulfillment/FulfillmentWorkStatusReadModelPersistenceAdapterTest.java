package com.personal.marketnote.commerce.adapter.out.persistence.fulfillment;

import com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.entity.FulfillmentWorkStatusReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.fulfillment.repository.FulfillmentWorkStatusReadModelJpaRepository;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, FulfillmentWorkStatusReadModelPersistenceAdapter.class})
class FulfillmentWorkStatusReadModelPersistenceAdapterTest {

    @Autowired
    private FulfillmentWorkStatusReadModelPersistenceAdapter adapter;

    @Autowired
    private FulfillmentWorkStatusReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("존재하지 않는 orderId로 upsert하면 새 엔티티가 생성된다")
        void insertsNewEntity() {
            // when
            adapter.upsert(100L, "PICKING");

            // then
            assertThat(repository.count()).isEqualTo(1);
            FulfillmentWorkStatusReadModelJpaEntity saved = repository.findByOrderId(100L).orElseThrow();
            assertThat(saved.getOrderId()).isEqualTo(100L);
            assertThat(saved.getWorkStatus()).isEqualTo("PICKING");
            assertThat(saved.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("이미 존재하는 orderId로 upsert하면 기존 엔티티가 갱신된다")
        void updatesExistingEntity() {
            // given
            adapter.upsert(100L, "PICKING");

            // when
            adapter.upsert(100L, "RELEASED");

            // then
            assertThat(repository.count()).isEqualTo(1);
            FulfillmentWorkStatusReadModelJpaEntity saved = repository.findByOrderId(100L).orElseThrow();
            assertThat(saved.getWorkStatus()).isEqualTo("RELEASED");
        }
    }

    @Nested
    @DisplayName("getWorkStatus")
    class GetWorkStatus {

        @Test
        @DisplayName("존재하는 orderId로 조회하면 작업 상태를 반환한다")
        void returnsWorkStatusWhenFound() {
            // given
            adapter.upsert(100L, "PACKING");

            // when
            String workStatus = adapter.getWorkStatus(100L);

            // then
            assertThat(workStatus).isEqualTo("PACKING");
        }

        @Test
        @DisplayName("존재하지 않는 orderId로 조회하면 NOT_REGISTERED를 반환한다")
        void returnsNotRegisteredWhenNotFound() {
            // when
            String workStatus = adapter.getWorkStatus(999L);

            // then
            assertThat(workStatus).isEqualTo("NOT_REGISTERED");
        }

        @Test
        @DisplayName("비활성 상태의 엔티티는 NOT_REGISTERED를 반환한다")
        void returnsNotRegisteredWhenInactive() {
            // given
            adapter.upsert(100L, "PICKING");
            FulfillmentWorkStatusReadModelJpaEntity entity = repository.findByOrderId(100L).orElseThrow();
            ReflectionTestUtils.setField(entity, "status", EntityStatus.INACTIVE);
            repository.saveAndFlush(entity);

            // when
            String workStatus = adapter.getWorkStatus(100L);

            // then
            assertThat(workStatus).isEqualTo("NOT_REGISTERED");
        }
    }
}
