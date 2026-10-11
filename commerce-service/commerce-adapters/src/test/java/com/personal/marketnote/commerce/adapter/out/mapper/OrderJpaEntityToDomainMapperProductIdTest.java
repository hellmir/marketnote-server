package com.personal.marketnote.commerce.adapter.out.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductId;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductJpaEntity;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderJpaEntityToDomainMapperProductIdTest {

    @Test
    @DisplayName("OrderProductJpaEntity에서 OrderProduct 도메인으로 매핑 시 productId가 보존된다")
    void shouldMapProductIdFromEntityToDomain() {
        // given
        OrderProductJpaEntity entity = createOrderProductJpaEntity(42L);

        // when
        Optional<OrderProduct> result = OrderJpaEntityToDomainMapper.mapToOrderProductDomain(entity);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("OrderProduct 도메인에서 OrderProductJpaEntity로 매핑 시 productId가 보존된다")
    void shouldMapProductIdFromDomainToEntity() {
        // given
        OrderProduct orderProduct = createOrderProductDomain(42L);
        OrderJpaEntity orderJpaEntity = createOrderJpaEntity(1L);

        // when
        OrderProductJpaEntity entity = OrderProductJpaEntity.from(orderProduct, orderJpaEntity);

        // then
        assertThat(entity.getProductId()).isEqualTo(42L);
    }

    private OrderProductJpaEntity createOrderProductJpaEntity(Long productId) {
        OrderProductJpaEntity entity = instantiateProtected(OrderProductJpaEntity.class);
        ReflectionTestUtils.setField(entity, "id", new OrderProductId(100L, 1L));
        ReflectionTestUtils.setField(entity, "productId", productId);
        ReflectionTestUtils.setField(entity, "sellerId", 10L);
        ReflectionTestUtils.setField(entity, "orderProductKey", UUID.randomUUID());
        ReflectionTestUtils.setField(entity, "quantity", 1);
        ReflectionTestUtils.setField(entity, "unitAmount", 50000L);
        ReflectionTestUtils.setField(entity, "accumulatedPoint", 0L);
        ReflectionTestUtils.setField(entity, "orderStatus", OrderStatus.PAYMENT_PENDING);
        ReflectionTestUtils.setField(entity, "isReviewed", false);
        return entity;
    }

    private OrderProduct createOrderProductDomain(Long productId) {
        com.personal.marketnote.commerce.domain.order.OrderProductCreateState state =
                com.personal.marketnote.commerce.domain.order.OrderProductCreateState.builder()
                        .productId(productId)
                        .sellerId(10L)
                        .pricePolicyId(100L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .accumulatedPoint(0L)
                        .build();
        return OrderProduct.from(state);
    }

    private OrderJpaEntity createOrderJpaEntity(Long id) {
        OrderJpaEntity entity = instantiateProtected(OrderJpaEntity.class);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private static <T> T instantiateProtected(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
