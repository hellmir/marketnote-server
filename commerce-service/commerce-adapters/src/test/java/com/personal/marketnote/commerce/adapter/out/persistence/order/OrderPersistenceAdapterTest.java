package com.personal.marketnote.commerce.adapter.out.persistence.order;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductId;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderStatusHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.repository.OrderHistoryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.order.repository.OrderJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.order.repository.OrderProductJpaRepository;
import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.OrderProductNotFoundException;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, OrderPersistenceAdapter.class})
@DisplayName("OrderPersistenceAdapter 통합 테스트")
class OrderPersistenceAdapterTest {

    @Autowired
    private OrderPersistenceAdapter adapter;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderProductJpaRepository orderProductJpaRepository;

    @Autowired
    private OrderHistoryJpaRepository orderHistoryJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        orderHistoryJpaRepository.deleteAll();
        orderProductJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private OrderJpaEntity persistOrder(Long buyerId, OrderStatus orderStatus) {
        entityManager.createNativeQuery("""
                INSERT INTO orders (buyer_id, order_key, order_number, order_status,
                    total_amount, paid_amount, coupon_amount, point_amount, shipping_fee,
                    delivery_recipient_name, delivery_recipient_phone_number,
                    delivery_zip_code, delivery_address, delivery_address_detail,
                    delivery_request_type, delivery_request_message,
                    created_at, modified_at)
                VALUES (:buyerId, :orderKey, :orderNumber, :orderStatus,
                    50000, 45000, 0, 0, 3000,
                    '홍길동', '010-1234-5678',
                    '06000', '서울시 강남구', '101호',
                    'LEAVE_AT_DOOR', '문 앞에 놔주세요',
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)
                .setParameter("buyerId", buyerId)
                .setParameter("orderKey", UUID.randomUUID().toString())
                .setParameter("orderNumber", "ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .setParameter("orderStatus", orderStatus.name())
                .executeUpdate();
        entityManager.flush();

        List<OrderJpaEntity> entities = orderJpaRepository.findAll();
        return entities.get(entities.size() - 1);
    }

    private OrderJpaEntity persistOrderWithProduct(Long buyerId, OrderStatus orderStatus, Long sellerId, Long pricePolicyId) {
        OrderJpaEntity orderEntity = persistOrder(buyerId, orderStatus);

        entityManager.createNativeQuery("""
                INSERT INTO order_product (order_id, price_policy_id, seller_id, order_product_key,
                    quantity, unit_amount, image_url, accumulated_point, order_status, review_yn,
                    created_at, modified_at)
                VALUES (:orderId, :pricePolicyId, :sellerId, :orderProductKey,
                    2, 25000, 'https://example.com/image.jpg', 500, :orderStatus, false,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)
                .setParameter("orderId", orderEntity.getId())
                .setParameter("pricePolicyId", pricePolicyId)
                .setParameter("sellerId", sellerId)
                .setParameter("orderProductKey", UUID.randomUUID().toString())
                .setParameter("orderStatus", orderStatus.name())
                .executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO order_status_history (order_id, order_status, reason_category, reason,
                    created_at, modified_at)
                VALUES (:orderId, :orderStatus, 'ETC', :reason,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)
                .setParameter("orderId", orderEntity.getId())
                .setParameter("orderStatus", orderStatus.name())
                .setParameter("reason", orderStatus.getDescription())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return orderJpaRepository.findById(orderEntity.getId()).orElseThrow();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("주문을 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSaveOrderAndReturnWithId() {
            // given
            entityManager.createNativeQuery(
                    "ALTER TABLE order_status_history ALTER COLUMN reason_category SET DEFAULT 'ETC'"
            ).executeUpdate();

            OrderAmount amount = OrderAmount.of(50000L, 45000L, 0L, 0L, 3000L);
            ShippingAddress shippingAddress = ShippingAddress.of(
                    "홍길동", "010-1234-5678", "06000", "서울시 강남구", "101호",
                    DeliveryRequestType.LEAVE_AT_DOOR, "문 앞에 놔주세요"
            );

            Order order = Order.from(
                    OrderCreateState.builder()
                            .buyerId(1L)
                            .amount(amount)
                            .shippingAddress(shippingAddress)
                            .orderProductStates(List.of(
                                    OrderProductCreateState.builder()
                                            .sellerId(10L)
                                            .pricePolicyId(100L)
                                            .quantity(2)
                                            .unitAmount(25000L)
                                            .imageUrl("https://example.com/image.jpg")
                                            .accumulatedPoint(500L)
                                            .build()
                            ))
                            .build()
            );

            // when
            Order saved = adapter.save(order);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getBuyerId()).isEqualTo(1L);
            assertThat(saved.getOrderKey()).isNotNull();
            assertThat(saved.getOrderNumber()).isNotNull();
            assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
            assertThat(saved.getAmount().getTotalAmount().getValue()).isEqualTo(50000L);
            assertThat(saved.getAmount().getPaidAmount()).isEqualTo(45000L);
            assertThat(saved.getOrderProducts()).hasSize(1);
        }

        @Test
        @DisplayName("주문 저장 시 주문 상태 이력이 함께 생성된다")
        void shouldCreateOrderStatusHistory() {
            // given
            entityManager.createNativeQuery(
                    "ALTER TABLE order_status_history ALTER COLUMN reason_category SET DEFAULT 'ETC'"
            ).executeUpdate();

            Order order = Order.from(
                    OrderCreateState.builder()
                            .buyerId(1L)
                            .amount(OrderAmount.of(50000L, 45000L, 0L, 0L, 3000L))
                            .shippingAddress(ShippingAddress.of(
                                    "홍길동", "010-1234-5678", "06000", "서울시 강남구", "101호",
                                    DeliveryRequestType.LEAVE_AT_DOOR, null
                            ))
                            .orderProductStates(List.of(
                                    OrderProductCreateState.builder()
                                            .sellerId(10L).pricePolicyId(100L).quantity(1)
                                            .unitAmount(25000L).accumulatedPoint(500L).build()
                            ))
                            .build()
            );

            // when
            Order saved = adapter.save(order);

            // then
            assertThat(orderHistoryJpaRepository.findAllByOrderJpaEntityIdOrderByCreatedAtAsc(saved.getId()))
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("ID로 주문을 조회하면 주문 상태 이력 정보가 포함된다")
        void shouldFindOrderByIdWithStatusInfo() {
            // given
            OrderJpaEntity orderEntity = persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);

            // when
            Optional<Order> found = adapter.findById(orderEntity.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(orderEntity.getId());
            assertThat(found.get().getBuyerId()).isEqualTo(1L);
            assertThat(found.get().getOrderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(found.get().getStatusChangeReasonCategory()).isEqualTo(OrderStatusReasonCategory.ETC);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 OrderNotFoundException이 발생한다")
        void shouldThrowWhenNotFound() {
            // when & then
            assertThatThrownBy(() -> adapter.findById(999L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByBuyerId")
    class FindByBuyerId {

        @Test
        @DisplayName("구매자 ID로 주문 목록을 조회한다")
        void shouldFindOrdersByBuyerId() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 200L);
            persistOrderWithProduct(2L, OrderStatus.PAID, 10L, 300L);

            // when
            List<Order> orders = adapter.findByBuyerId(1L, null, null, null);

            // then
            assertThat(orders).hasSize(2);
            assertThat(orders).allMatch(order -> order.getBuyerId().equals(1L));
        }

        @Test
        @DisplayName("PAYMENT_PENDING 상태의 주문은 제외된다")
        void shouldExcludePendingOrders() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAYMENT_PENDING, 10L, 100L);

            // when
            List<Order> orders = adapter.findByBuyerId(1L, null, null, null);

            // then
            assertThat(orders).isEmpty();
        }

        @Test
        @DisplayName("상태 필터를 적용하여 주문을 조회한다")
        void shouldFilterByStatuses() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);
            persistOrderWithProduct(1L, OrderStatus.SHIPPING, 10L, 200L);

            // when
            List<Order> orders = adapter.findByBuyerId(1L, null, null, List.of(OrderStatus.PAID));

            // then
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("날짜 범위 필터를 적용하여 주문을 조회한다")
        void shouldFilterByDateRange() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);

            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            // when
            List<Order> orders = adapter.findByBuyerId(1L, startDate, endDate, null);

            // then
            assertThat(orders).hasSize(1);
        }

        @Test
        @DisplayName("조건에 맞는 주문이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoMatch() {
            // when
            List<Order> orders = adapter.findByBuyerId(999L, null, null, null);

            // then
            assertThat(orders).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllWithFilters")
    class FindAllWithFilters {

        @Test
        @DisplayName("판매자 ID로 주문을 조회한다")
        void shouldFindOrdersBySellerId() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);

            // when
            List<Order> orders = adapter.findAllWithFilters(10L, null, null, null);

            // then
            assertThat(orders).hasSize(1);
        }

        @Test
        @DisplayName("주문 상태로 필터링하여 조회한다")
        void shouldFilterByOrderStatus() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);
            persistOrderWithProduct(1L, OrderStatus.SHIPPING, 10L, 200L);

            // when
            List<Order> orders = adapter.findAllWithFilters(null, null, null, OrderStatus.PAID);

            // then
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("날짜 범위로 필터링하여 조회한다")
        void shouldFilterByDateRange() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);

            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            // when
            List<Order> orders = adapter.findAllWithFilters(null, startDate, endDate, null);

            // then
            assertThat(orders).hasSize(1);
        }

        @Test
        @DisplayName("PAYMENT_PENDING 상태의 주문은 조회에서 제외된다")
        void shouldExcludePaymentPendingOrders() {
            // given
            persistOrderWithProduct(1L, OrderStatus.PAYMENT_PENDING, 10L, 100L);

            // when
            List<Order> orders = adapter.findAllWithFilters(null, null, null, null);

            // then
            assertThat(orders).isEmpty();
        }

        @Test
        @DisplayName("조건에 맞는 주문이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoMatch() {
            // when
            List<Order> orders = adapter.findAllWithFilters(999L, null, null, null);

            // then
            assertThat(orders).isEmpty();
        }
    }

    @Nested
    @DisplayName("update (Order)")
    class UpdateOrder {

        @Test
        @DisplayName("주문 상태를 업데이트하고 상태 이력을 저장한다")
        void shouldUpdateOrderStatusAndSaveHistory() {
            // given
            OrderJpaEntity orderEntity = persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);
            OrderProductJpaEntity productEntity = orderProductJpaRepository
                    .findByOrderIdAndPricePolicyId(orderEntity.getId(), 100L).orElseThrow();

            Order updatedOrder = Order.from(
                    OrderSnapshotState.builder()
                            .id(orderEntity.getId())
                            .buyerId(1L)
                            .orderKey(orderEntity.getOrderKey())
                            .orderNumber(OrderNumber.of(orderEntity.getOrderNumber()))
                            .orderStatus(OrderStatus.PREPARING)
                            .amount(OrderAmount.of(50000L, 45000L, 0L, 0L, 3000L))
                            .shippingAddress(ShippingAddress.of(
                                    "홍길동", "010-1234-5678", "06000", "서울시 강남구", "101호",
                                    DeliveryRequestType.LEAVE_AT_DOOR, "문 앞에 놔주세요"
                            ))
                            .orderProductStates(List.of(
                                    OrderProductSnapshotState.builder()
                                            .orderId(orderEntity.getId())
                                            .sellerId(10L)
                                            .pricePolicyId(100L)
                                            .orderProductKey(productEntity.getOrderProductKey())
                                            .quantity(2)
                                            .unitAmount(25000L)
                                            .accumulatedPoint(500L)
                                            .orderStatus(OrderStatus.PREPARING)
                                            .isReviewed(false)
                                            .build()
                            ))
                            .build()
            );

            OrderStatusHistory history = OrderStatusHistory.from(
                    OrderStatusHistoryCreateState.builder()
                            .orderId(orderEntity.getId())
                            .orderStatus(OrderStatus.PREPARING)
                            .reasonCategory(OrderStatusReasonCategory.ETC)
                            .reason("상품 준비중으로 변경")
                            .build()
            );

            // when
            adapter.update(updatedOrder, history);
            entityManager.flush();
            entityManager.clear();

            // then
            Order found = adapter.findById(orderEntity.getId()).orElseThrow();
            assertThat(found.getOrderStatus()).isEqualTo(OrderStatus.PREPARING);

            List<OrderStatusHistory> histories = adapter.findAllByOrderId(orderEntity.getId());
            assertThat(histories).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 주문을 업데이트하면 OrderNotFoundException이 발생한다")
        void shouldThrowWhenOrderNotFound() {
            // given
            Order nonExistent = Order.from(
                    OrderSnapshotState.builder()
                            .id(999L)
                            .buyerId(1L)
                            .orderKey(UUID.randomUUID())
                            .orderNumber(OrderNumber.of("ORD-TEST-999"))
                            .orderStatus(OrderStatus.PAID)
                            .amount(OrderAmount.of(50000L, 45000L, 0L, 0L, 3000L))
                            .shippingAddress(ShippingAddress.of(
                                    "홍길동", "010-1234-5678", "06000", "서울시", "101호",
                                    DeliveryRequestType.LEAVE_AT_DOOR, null
                            ))
                            .orderProductStates(List.of())
                            .build()
            );

            OrderStatusHistory history = OrderStatusHistory.from(
                    OrderStatusHistoryCreateState.builder()
                            .orderId(999L)
                            .orderStatus(OrderStatus.PAID)
                            .reasonCategory(OrderStatusReasonCategory.ETC)
                            .reason("테스트")
                            .build()
            );

            // when & then
            assertThatThrownBy(() -> adapter.update(nonExistent, history))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByOrderIdAndPricePolicyId")
    class FindByOrderIdAndPricePolicyId {

        @Test
        @DisplayName("주문 ID와 가격정책 ID로 주문 상품을 조회한다")
        void shouldFindOrderProduct() {
            // given
            OrderJpaEntity orderEntity = persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);

            // when
            Optional<OrderProduct> found = adapter.findByOrderIdAndPricePolicyId(orderEntity.getId(), 100L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getPricePolicyId()).isEqualTo(100L);
            assertThat(found.get().getSellerId()).isEqualTo(10L);
            assertThat(found.get().getQuantity().getValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("존재하지 않는 주문 상품 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<OrderProduct> found = adapter.findByOrderIdAndPricePolicyId(999L, 100L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("update (OrderProduct)")
    class UpdateOrderProduct {

        @Test
        @DisplayName("주문 상품의 수량과 상태를 업데이트한다")
        void shouldUpdateOrderProduct() {
            // given
            OrderJpaEntity orderEntity = persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);
            OrderProductJpaEntity productEntity = orderProductJpaRepository
                    .findByOrderIdAndPricePolicyId(orderEntity.getId(), 100L).orElseThrow();

            OrderProduct updatedProduct = OrderProduct.from(
                    OrderProductSnapshotState.builder()
                            .orderId(orderEntity.getId())
                            .sellerId(10L)
                            .pricePolicyId(100L)
                            .orderProductKey(productEntity.getOrderProductKey())
                            .quantity(3)
                            .unitAmount(25000L)
                            .accumulatedPoint(500L)
                            .orderStatus(OrderStatus.PREPARING)
                            .isReviewed(false)
                            .build()
            );

            // when
            adapter.update(updatedProduct);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<OrderProduct> found = adapter.findByOrderIdAndPricePolicyId(orderEntity.getId(), 100L);
            assertThat(found).isPresent();
            assertThat(found.get().getQuantity().getValue()).isEqualTo(3);
            assertThat(found.get().getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("존재하지 않는 주문 상품을 업데이트하면 OrderProductNotFoundException이 발생한다")
        void shouldThrowWhenOrderProductNotFound() {
            // given
            OrderProduct nonExistent = OrderProduct.from(
                    OrderProductSnapshotState.builder()
                            .orderId(999L)
                            .sellerId(10L)
                            .pricePolicyId(100L)
                            .orderProductKey(UUID.randomUUID())
                            .quantity(1)
                            .unitAmount(25000L)
                            .accumulatedPoint(500L)
                            .orderStatus(OrderStatus.PAID)
                            .build()
            );

            // when & then
            assertThatThrownBy(() -> adapter.update(nonExistent))
                    .isInstanceOf(OrderProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAllByOrderId (OrderStatusHistory)")
    class FindAllByOrderId {

        @Test
        @DisplayName("주문 ID로 상태 이력을 시간순으로 조회한다")
        void shouldFindStatusHistoriesOrderedByCreatedAt() {
            // given
            OrderJpaEntity orderEntity = persistOrderWithProduct(1L, OrderStatus.PAID, 10L, 100L);
            OrderProductJpaEntity productEntity = orderProductJpaRepository
                    .findByOrderIdAndPricePolicyId(orderEntity.getId(), 100L).orElseThrow();

            Order updatedOrder = Order.from(
                    OrderSnapshotState.builder()
                            .id(orderEntity.getId())
                            .buyerId(1L)
                            .orderKey(orderEntity.getOrderKey())
                            .orderNumber(OrderNumber.of(orderEntity.getOrderNumber()))
                            .orderStatus(OrderStatus.PREPARING)
                            .amount(OrderAmount.of(50000L, 45000L, 0L, 0L, 3000L))
                            .shippingAddress(ShippingAddress.of(
                                    "홍길동", "010-1234-5678", "06000", "서울시 강남구", "101호",
                                    DeliveryRequestType.LEAVE_AT_DOOR, null
                            ))
                            .orderProductStates(List.of(
                                    OrderProductSnapshotState.builder()
                                            .orderId(orderEntity.getId())
                                            .sellerId(10L)
                                            .pricePolicyId(100L)
                                            .orderProductKey(productEntity.getOrderProductKey())
                                            .quantity(2)
                                            .unitAmount(25000L)
                                            .accumulatedPoint(500L)
                                            .orderStatus(OrderStatus.PREPARING)
                                            .isReviewed(false)
                                            .build()
                            ))
                            .build()
            );

            OrderStatusHistory history = OrderStatusHistory.from(
                    OrderStatusHistoryCreateState.builder()
                            .orderId(orderEntity.getId())
                            .orderStatus(OrderStatus.PREPARING)
                            .reasonCategory(OrderStatusReasonCategory.ETC)
                            .reason("상품 준비중")
                            .build()
            );

            adapter.update(updatedOrder, history);
            entityManager.flush();
            entityManager.clear();

            // when
            List<OrderStatusHistory> histories = adapter.findAllByOrderId(orderEntity.getId());

            // then
            assertThat(histories).hasSize(2);
            assertThat(histories.get(0).getOrderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(histories.get(1).getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("상태 이력이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoHistory() {
            // when
            List<OrderStatusHistory> histories = adapter.findAllByOrderId(999L);

            // then
            assertThat(histories).isEmpty();
        }
    }

    @Nested
    @DisplayName("findOrderIdsEligibleForAutoConfirm")
    class FindOrderIdsEligibleForAutoConfirm {

        @Test
        @DisplayName("배송완료 상태이며 기준일 이전에 배송된 주문 ID를 조회한다")
        void shouldFindEligibleOrderIds() {
            // given
            OrderJpaEntity orderEntity = persistOrder(1L, OrderStatus.DELIVERED);

            entityManager.createNativeQuery("""
                    INSERT INTO order_product (order_id, price_policy_id, seller_id, order_product_key,
                        quantity, unit_amount, accumulated_point, order_status, review_yn,
                        delivered_at, created_at, modified_at)
                    VALUES (:orderId, 100, 10, :orderProductKey,
                        1, 25000, 500, 'DELIVERED', false,
                        :deliveredAt, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """)
                    .setParameter("orderId", orderEntity.getId())
                    .setParameter("orderProductKey", UUID.randomUUID().toString())
                    .setParameter("deliveredAt", LocalDateTime.of(2026, 4, 1, 10, 0))
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            // when
            List<Long> eligibleIds = adapter.findOrderIdsEligibleForAutoConfirm(
                    LocalDateTime.of(2026, 4, 10, 0, 0)
            );

            // then
            assertThat(eligibleIds).contains(orderEntity.getId());
        }

        @Test
        @DisplayName("배송일이 기준일 이후이면 조회되지 않는다")
        void shouldNotFindOrdersDeliveredAfterCutoff() {
            // given
            OrderJpaEntity orderEntity = persistOrder(1L, OrderStatus.DELIVERED);

            entityManager.createNativeQuery("""
                    INSERT INTO order_product (order_id, price_policy_id, seller_id, order_product_key,
                        quantity, unit_amount, accumulated_point, order_status, review_yn,
                        delivered_at, created_at, modified_at)
                    VALUES (:orderId, 100, 10, :orderProductKey,
                        1, 25000, 500, 'DELIVERED', false,
                        :deliveredAt, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """)
                    .setParameter("orderId", orderEntity.getId())
                    .setParameter("orderProductKey", UUID.randomUUID().toString())
                    .setParameter("deliveredAt", LocalDateTime.of(2026, 4, 15, 10, 0))
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            // when
            List<Long> eligibleIds = adapter.findOrderIdsEligibleForAutoConfirm(
                    LocalDateTime.of(2026, 4, 10, 0, 0)
            );

            // then
            assertThat(eligibleIds).doesNotContain(orderEntity.getId());
        }

        @Test
        @DisplayName("조건에 맞는 주문이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoEligible() {
            // when
            List<Long> eligibleIds = adapter.findOrderIdsEligibleForAutoConfirm(
                    LocalDateTime.of(2020, 1, 1, 0, 0)
            );

            // then
            assertThat(eligibleIds).isEmpty();
        }
    }
}
