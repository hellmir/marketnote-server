package com.personal.marketnote.commerce.adapter.out.persistence.shipping;

import com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity.ShippingPolicyReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.shipping.repository.ShippingPolicyReadModelJpaRepository;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ShippingPolicyReadModelPersistenceAdapter.class})
@DisplayName("ShippingPolicyReadModelPersistenceAdapter 테스트")
class ShippingPolicyReadModelPersistenceAdapterTest {

    @Autowired
    private ShippingPolicyReadModelPersistenceAdapter adapter;

    @Autowired
    private ShippingPolicyReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("findBySellerIds")
    class FindBySellerIds {

        @Test
        @DisplayName("ACTIVE 상태의 배송비 정책을 판매자 ID별 맵으로 반환한다")
        void returnsActivePoliciesAsMap() {
            // given
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);
            adapter.upsert(20L, 2500L, 30000L, 4000L, 6000L);

            // when
            Map<Long, ShippingPolicyInfoResult> result = adapter.findBySellerIds(List.of(10L, 20L));

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(10L).sellerId()).isEqualTo(10L);
            assertThat(result.get(10L).shippingFee()).isEqualTo(3000L);
            assertThat(result.get(10L).freeShippingThreshold()).isEqualTo(20000L);
            assertThat(result.get(10L).jejuSurcharge()).isEqualTo(3000L);
            assertThat(result.get(10L).islandSurcharge()).isEqualTo(5000L);
            assertThat(result.get(20L).sellerId()).isEqualTo(20L);
            assertThat(result.get(20L).shippingFee()).isEqualTo(2500L);
            assertThat(result.get(20L).freeShippingThreshold()).isEqualTo(30000L);
            assertThat(result.get(20L).jejuSurcharge()).isEqualTo(4000L);
            assertThat(result.get(20L).islandSurcharge()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("빈 sellerIds 목록이면 빈 맵을 반환한다")
        void returnsEmptyMapForEmptySellerIds() {
            // when
            Map<Long, ShippingPolicyInfoResult> result = adapter.findBySellerIds(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null sellerIds이면 빈 맵을 반환한다")
        void returnsEmptyMapForNullSellerIds() {
            // when
            Map<Long, ShippingPolicyInfoResult> result = adapter.findBySellerIds(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("INACTIVE 상태의 배송비 정책은 조회하지 않는다")
        void excludesInactivePolicies() {
            // given
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);
            adapter.deactivateBySellerId(10L);

            // when
            Map<Long, ShippingPolicyInfoResult> result = adapter.findBySellerIds(List.of(10L));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 sellerId는 결과에 포함하지 않는다")
        void excludesNonExistentSellerIds() {
            // given
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);

            // when
            Map<Long, ShippingPolicyInfoResult> result = adapter.findBySellerIds(List.of(10L, 999L));

            // then
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(10L);
            assertThat(result).doesNotContainKey(999L);
        }
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 배송비 정책을 저장한다")
        void insertsNewPolicy() {
            // when
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);

            // then
            Optional<ShippingPolicyReadModelJpaEntity> entity = repository.findBySellerIdAndStatus(10L, EntityStatus.ACTIVE);
            assertThat(entity).isPresent();
            assertThat(entity.get().getSellerId()).isEqualTo(10L);
            assertThat(entity.get().getShippingFee()).isEqualTo(3000L);
            assertThat(entity.get().getFreeShippingThreshold()).isEqualTo(20000L);
            assertThat(entity.get().getJejuSurcharge()).isEqualTo(3000L);
            assertThat(entity.get().getIslandSurcharge()).isEqualTo(5000L);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 sellerId로 upsert 시 기존 데이터를 업데이트한다")
        void updatesExistingPolicy() {
            // given
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);

            // when
            adapter.upsert(10L, 5000L, 50000L, 7000L, 8000L);

            // then
            Optional<ShippingPolicyReadModelJpaEntity> entity = repository.findBySellerIdAndStatus(10L, EntityStatus.ACTIVE);
            assertThat(entity).isPresent();
            assertThat(entity.get().getShippingFee()).isEqualTo(5000L);
            assertThat(entity.get().getFreeShippingThreshold()).isEqualTo(50000L);
            assertThat(entity.get().getJejuSurcharge()).isEqualTo(7000L);
            assertThat(entity.get().getIslandSurcharge()).isEqualTo(8000L);
        }

        @Test
        @DisplayName("비활성화된 정책에 대해 upsert 시 다시 활성화된다")
        void reactivatesInactivePolicy() {
            // given
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);
            adapter.deactivateBySellerId(10L);

            // when
            adapter.upsert(10L, 5000L, 50000L, 7000L, 8000L);

            // then
            Optional<ShippingPolicyReadModelJpaEntity> entity = repository.findBySellerIdAndStatus(10L, EntityStatus.ACTIVE);
            assertThat(entity).isPresent();
            assertThat(entity.get().getShippingFee()).isEqualTo(5000L);
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("deactivateBySellerId")
    class DeactivateBySellerId {

        @Test
        @DisplayName("배송비 정책을 비활성화한다")
        void deactivatesPolicy() {
            // given
            adapter.upsert(10L, 3000L, 20000L, 3000L, 5000L);

            // when
            adapter.deactivateBySellerId(10L);

            // then
            List<ShippingPolicyReadModelJpaEntity> all = repository.findAll();
            assertThat(all).hasSize(1);
            assertThat(all.getFirst().getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 sellerId 비활성화 시 에러 없이 무시한다")
        void ignoresNonExistentSellerId() {
            // when & then — no exception
            adapter.deactivateBySellerId(999L);
        }
    }
}
