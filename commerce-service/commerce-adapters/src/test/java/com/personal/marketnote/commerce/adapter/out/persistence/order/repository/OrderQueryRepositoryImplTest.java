package com.personal.marketnote.commerce.adapter.out.persistence.order.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, QuerydslConfig.class, OrderQueryRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderQueryRepositoryImplTest {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Long DEFAULT_BUYER_ID = 10L;
    private static final Long OTHER_BUYER_ID = 20L;
    private static final Long DEFAULT_SELLER_ID = 100L;
    private static final Long OTHER_SELLER_ID = 200L;

    @Nested
    @DisplayName("findIdsByBuyerIdWithFilters - 구매자 주문 ID 조회")
    class FindIdsByBuyerIdWithFilters {

        @Test
        @DisplayName("필터가 모두 비어있으면 해당 buyerId의 PAYMENT_PENDING/FAILED 제외 전체 주문 ID가 id DESC로 반환된다")
        void returnsAllNonPendingNonFailedOrdersWhenFiltersEmpty() {
            Long first = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            Long second = saveOrder(DEFAULT_BUYER_ID, OrderStatus.DELIVERED, LocalDateTime.now());
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAYMENT_PENDING, LocalDateTime.now());
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.FAILED, LocalDateTime.now());
            saveOrder(OTHER_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByBuyerIdWithFilters(
                    DEFAULT_BUYER_ID, null, null, List.of("IGNORED"), 0
            );

            assertThat(results).containsExactly(second, first);
        }

        @Test
        @DisplayName("startDate 필터만 있으면 createdAt >= startDate 주문만 반환한다")
        void filtersByStartDateOnly() {
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
            Long past = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, base.minusDays(1));
            Long recent = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, base.plusDays(1));
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByBuyerIdWithFilters(
                    DEFAULT_BUYER_ID, base, null, List.of("IGNORED"), 0
            );

            assertThat(results).containsExactly(recent);
            assertThat(results).doesNotContain(past);
        }

        @Test
        @DisplayName("endDate 필터만 있으면 createdAt < endDate 주문만 반환한다")
        void filtersByEndDateOnly() {
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
            Long past = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, base.minusDays(1));
            Long future = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, base.plusDays(1));
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByBuyerIdWithFilters(
                    DEFAULT_BUYER_ID, null, base, List.of("IGNORED"), 0
            );

            assertThat(results).containsExactly(past);
            assertThat(results).doesNotContain(future);
        }

        @Test
        @DisplayName("startDate/endDate 범위 필터는 [start, end) 구간 주문만 반환한다")
        void filtersByStartAndEndDate() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 2, 1, 0, 0);
            Long before = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, start.minusSeconds(1));
            Long inRange = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, start.plusDays(5));
            Long atEnd = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, end);
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByBuyerIdWithFilters(
                    DEFAULT_BUYER_ID, start, end, List.of("IGNORED"), 0
            );

            assertThat(results).containsExactly(inRange);
            assertThat(results).doesNotContain(before, atEnd);
        }

        @Test
        @DisplayName("statusSize > 0이면 statuses IN 조건이 적용되어 일치 주문만 반환한다")
        void filtersByStatusesIn() {
            Long paid = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            Long delivered = saveOrder(DEFAULT_BUYER_ID, OrderStatus.DELIVERED, LocalDateTime.now());
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.CANCELLED, LocalDateTime.now());
            flushAndClear();

            List<String> statuses = List.of(OrderStatus.PAID.name(), OrderStatus.DELIVERED.name());
            List<Long> results = orderJpaRepository.findIdsByBuyerIdWithFilters(
                    DEFAULT_BUYER_ID, null, null, statuses, statuses.size()
            );

            assertThat(results).containsExactlyInAnyOrder(paid, delivered);
        }

        @Test
        @DisplayName("주문 ID는 DESC 정렬로 반환된다")
        void ordersByIdDesc() {
            Long first = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            Long second = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            Long third = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByBuyerIdWithFilters(
                    DEFAULT_BUYER_ID, null, null, List.of("IGNORED"), 0
            );

            assertThat(results).containsExactly(third, second, first);
        }
    }

    @Nested
    @DisplayName("findIdsByAdminFilters - 관리자 주문 ID 조회")
    class FindIdsByAdminFilters {

        @Test
        @DisplayName("모든 필터가 비어있으면 PAYMENT_PENDING 제외 전체 주문 ID가 id DESC로 반환된다")
        void returnsAllNonPendingWhenFiltersEmpty() {
            Long paid = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            Long delivered = saveOrder(OTHER_BUYER_ID, OrderStatus.DELIVERED, LocalDateTime.now());
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAYMENT_PENDING, LocalDateTime.now());
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByAdminFilters(null, null, null, "");

            assertThat(results).containsExactly(delivered, paid);
        }

        @Test
        @DisplayName("sellerId 필터가 있으면 해당 seller의 상품을 포함한 주문만 반환한다")
        void filtersBySellerId() {
            Long matchingOrderId = saveOrderWithProduct(
                    DEFAULT_BUYER_ID, OrderStatus.PAID, DEFAULT_SELLER_ID, LocalDateTime.now()
            );
            saveOrderWithProduct(
                    DEFAULT_BUYER_ID, OrderStatus.PAID, OTHER_SELLER_ID, LocalDateTime.now()
            );
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByAdminFilters(DEFAULT_SELLER_ID, null, null, "");

            assertThat(results).containsExactly(matchingOrderId);
        }

        @Test
        @DisplayName("startDate/endDate 범위 필터는 [start, end) 구간 주문만 반환한다")
        void filtersByDateRange() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 2, 1, 0, 0);
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, start.minusSeconds(1));
            Long inRange = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, start.plusDays(5));
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, end);
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByAdminFilters(null, start, end, "");

            assertThat(results).containsExactly(inRange);
        }

        @Test
        @DisplayName("orderStatus 필터가 주어지면 해당 상태 주문만 반환한다")
        void filtersByOrderStatus() {
            Long paid = saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.DELIVERED, LocalDateTime.now());
            flushAndClear();

            List<Long> results = orderJpaRepository.findIdsByAdminFilters(
                    null, null, null, OrderStatus.PAID.name()
            );

            assertThat(results).containsExactly(paid);
        }
    }

    @Nested
    @DisplayName("findWithProductsByIds - ID 목록으로 주문+상품 조회")
    class FindWithProductsByIds {

        @Test
        @DisplayName("ids에 포함된 주문을 LEFT JOIN FETCH로 조회한다")
        void returnsOrdersInIds() {
            Long first = saveOrderWithProduct(
                    DEFAULT_BUYER_ID, OrderStatus.PAID, DEFAULT_SELLER_ID, LocalDateTime.now()
            );
            Long second = saveOrderWithProduct(
                    OTHER_BUYER_ID, OrderStatus.DELIVERED, OTHER_SELLER_ID, LocalDateTime.now()
            );
            flushAndClear();

            List<OrderJpaEntity> results = orderJpaRepository.findWithProductsByIds(List.of(first, second));

            assertThat(results)
                    .extracting(OrderJpaEntity::getId)
                    .containsExactlyInAnyOrder(first, second);
            assertThat(results)
                    .allSatisfy(order -> assertThat(order.getOrderProductJpaEntities()).hasSize(1));
        }

        @Test
        @DisplayName("ids가 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyWhenIdsEmpty() {
            List<OrderJpaEntity> results = orderJpaRepository.findWithProductsByIds(List.of());

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("ids에 없는 주문 ID를 주면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoMatch() {
            saveOrder(DEFAULT_BUYER_ID, OrderStatus.PAID, LocalDateTime.now());
            flushAndClear();

            List<OrderJpaEntity> results = orderJpaRepository.findWithProductsByIds(List.of(99999L));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findOrderIdsEligibleForAutoConfirm - 자동 구매 확정 대상 조회")
    class FindOrderIdsEligibleForAutoConfirm {

        @Test
        @DisplayName("주문/상품 모두 DELIVERED이고 deliveredAt <= deliveredBefore인 주문 ID가 id ASC로 반환된다")
        void returnsDeliveredOrdersWithinThreshold() {
            LocalDateTime threshold = LocalDateTime.of(2026, 4, 1, 0, 0);
            Long first = saveDeliveredOrder(DEFAULT_BUYER_ID, DEFAULT_SELLER_ID, threshold.minusDays(3));
            Long second = saveDeliveredOrder(DEFAULT_BUYER_ID, DEFAULT_SELLER_ID, threshold.minusDays(1));
            flushAndClear();

            List<Long> results = orderJpaRepository.findOrderIdsEligibleForAutoConfirm(threshold);

            assertThat(results).containsExactly(first, second);
        }

        @Test
        @DisplayName("deliveredAt > deliveredBefore 주문은 제외한다")
        void excludesOrdersDeliveredAfterThreshold() {
            LocalDateTime threshold = LocalDateTime.of(2026, 4, 1, 0, 0);
            Long eligible = saveDeliveredOrder(DEFAULT_BUYER_ID, DEFAULT_SELLER_ID, threshold.minusDays(1));
            saveDeliveredOrder(DEFAULT_BUYER_ID, DEFAULT_SELLER_ID, threshold.plusDays(1));
            flushAndClear();

            List<Long> results = orderJpaRepository.findOrderIdsEligibleForAutoConfirm(threshold);

            assertThat(results).containsExactly(eligible);
        }

        @Test
        @DisplayName("주문이 DELIVERED가 아니면 제외한다")
        void excludesOrdersNotDelivered() {
            LocalDateTime threshold = LocalDateTime.of(2026, 4, 1, 0, 0);
            saveOrderAndProduct(
                    DEFAULT_BUYER_ID, OrderStatus.SHIPPING, DEFAULT_SELLER_ID,
                    LocalDateTime.now(), OrderStatus.DELIVERED, threshold.minusDays(1)
            );
            flushAndClear();

            List<Long> results = orderJpaRepository.findOrderIdsEligibleForAutoConfirm(threshold);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("상품이 DELIVERED가 아니면 제외한다")
        void excludesOrdersWithProductNotDelivered() {
            LocalDateTime threshold = LocalDateTime.of(2026, 4, 1, 0, 0);
            saveOrderAndProduct(
                    DEFAULT_BUYER_ID, OrderStatus.DELIVERED, DEFAULT_SELLER_ID,
                    LocalDateTime.now(), OrderStatus.SHIPPING, threshold.minusDays(1)
            );
            flushAndClear();

            List<Long> results = orderJpaRepository.findOrderIdsEligibleForAutoConfirm(threshold);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("deliveredAt이 null이면 제외한다")
        void excludesOrdersWithNullDeliveredAt() {
            LocalDateTime threshold = LocalDateTime.of(2026, 4, 1, 0, 0);
            saveOrderAndProduct(
                    DEFAULT_BUYER_ID, OrderStatus.DELIVERED, DEFAULT_SELLER_ID,
                    LocalDateTime.now(), OrderStatus.DELIVERED, null
            );
            flushAndClear();

            List<Long> results = orderJpaRepository.findOrderIdsEligibleForAutoConfirm(threshold);

            assertThat(results).isEmpty();
        }
    }

    private Long saveOrder(Long buyerId, OrderStatus status, LocalDateTime createdAt) {
        return insertOrderRow(buyerId, status, createdAt);
    }

    private Long saveOrderWithProduct(Long buyerId, OrderStatus status, Long sellerId, LocalDateTime createdAt) {
        Long orderId = insertOrderRow(buyerId, status, createdAt);
        insertOrderProductRow(orderId, sellerId, status, null);
        return orderId;
    }

    private Long saveDeliveredOrder(Long buyerId, Long sellerId, LocalDateTime deliveredAt) {
        Long orderId = insertOrderRow(buyerId, OrderStatus.DELIVERED, LocalDateTime.now());
        insertOrderProductRow(orderId, sellerId, OrderStatus.DELIVERED, deliveredAt);
        return orderId;
    }

    private Long saveOrderAndProduct(
            Long buyerId,
            OrderStatus orderStatus,
            Long sellerId,
            LocalDateTime createdAt,
            OrderStatus productStatus,
            LocalDateTime deliveredAt
    ) {
        Long orderId = insertOrderRow(buyerId, orderStatus, createdAt);
        insertOrderProductRow(orderId, sellerId, productStatus, deliveredAt);
        return orderId;
    }

    private Long insertOrderRow(Long buyerId, OrderStatus status, LocalDateTime createdAt) {
        String sql = """
                INSERT INTO orders (
                    buyer_id, order_key, order_number, order_status,
                    total_amount, paid_amount, shipping_region_type, created_at, modified_at
                ) VALUES (
                    :buyerId, :orderKey, :orderNumber, :orderStatus,
                    :totalAmount, :paidAmount, :shippingRegionType, :createdAt, :modifiedAt
                )
                """;
        UUID orderKey = UUID.randomUUID();
        entityManager.createNativeQuery(sql)
                .setParameter("buyerId", buyerId)
                .setParameter("orderKey", orderKey)
                .setParameter("orderNumber", "ORD-" + System.nanoTime())
                .setParameter("orderStatus", status.name())
                .setParameter("totalAmount", 10000L)
                .setParameter("paidAmount", 10000L)
                .setParameter("shippingRegionType", "NORMAL")
                .setParameter("createdAt", createdAt)
                .setParameter("modifiedAt", createdAt)
                .executeUpdate();

        Number id = (Number) entityManager.createNativeQuery(
                "SELECT id FROM orders WHERE order_key = :orderKey"
        ).setParameter("orderKey", orderKey).getSingleResult();
        return id.longValue();
    }

    private void insertOrderProductRow(
            Long orderId, Long sellerId, OrderStatus status, LocalDateTime deliveredAt
    ) {
        String sql = """
                INSERT INTO order_product (
                    price_policy_id, order_id, seller_id, order_product_key,
                    quantity, unit_amount, order_status, review_yn, delivered_at,
                    created_at, modified_at
                ) VALUES (
                    :pricePolicyId, :orderId, :sellerId, :orderProductKey,
                    :quantity, :unitAmount, :orderStatus, FALSE, :deliveredAt,
                    :createdAt, :modifiedAt
                )
                """;
        LocalDateTime now = LocalDateTime.now();
        entityManager.createNativeQuery(sql)
                .setParameter("pricePolicyId", System.nanoTime())
                .setParameter("orderId", orderId)
                .setParameter("sellerId", sellerId)
                .setParameter("orderProductKey", UUID.randomUUID())
                .setParameter("quantity", 1)
                .setParameter("unitAmount", 10000L)
                .setParameter("orderStatus", status.name())
                .setParameter("deliveredAt", deliveredAt)
                .setParameter("createdAt", now)
                .setParameter("modifiedAt", now)
                .executeUpdate();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
