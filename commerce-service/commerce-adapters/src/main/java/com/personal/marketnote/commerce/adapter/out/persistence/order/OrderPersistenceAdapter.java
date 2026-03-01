package com.personal.marketnote.commerce.adapter.out.persistence.order;

import com.personal.marketnote.commerce.adapter.out.mapper.OrderJpaEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderProductJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.entity.OrderStatusHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.order.repository.OrderHistoryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.order.repository.OrderJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.order.repository.OrderProductJpaRepository;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.OrderProductNotFoundException;
import com.personal.marketnote.commerce.port.out.order.*;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@PersistenceAdapter
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements SaveOrderPort, FindOrderPort, FindOrderProductPort, UpdateOrderPort, UpdateOrderProductPort {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderProductJpaRepository orderProductJpaRepository;
    private final OrderHistoryJpaRepository orderHistoryJpaRepository;

    @Override
    public Order save(Order order) {
        OrderJpaEntity orderEntity = OrderJpaEntity.from(order);
        OrderJpaEntity savedOrderEntity = orderJpaRepository.save(orderEntity);

        if (FormatValidator.hasValue(order.getOrderProducts())) {
            order.getOrderProducts().forEach(orderProduct -> {
                OrderProductJpaEntity orderProductEntity = OrderProductJpaEntity.from(
                        orderProduct, savedOrderEntity
                );
                savedOrderEntity.addOrderProduct(orderProductEntity);
            });
        }

        orderHistoryJpaRepository.save(OrderStatusHistoryJpaEntity.from(savedOrderEntity));

        return OrderJpaEntityToDomainMapper.mapToDomain(savedOrderEntity).orElse(null);
    }

    @Override
    public Optional<Order> findById(Long id) {
        OrderStatusHistoryJpaEntity orderStatusInfo
                = orderHistoryJpaRepository.findTopByOrderJpaEntityIdOrderByIdDesc(id);
        OrderJpaEntity orderJpaEntity = findEntityById(id);

        if (FormatValidator.hasValue(orderStatusInfo)) {
            return OrderJpaEntityToDomainMapper.mapToDomainWithStatusInfo(orderJpaEntity, orderStatusInfo);
        }

        return OrderJpaEntityToDomainMapper.mapToDomain(orderJpaEntity);
    }

    @Override
    public List<Order> findByBuyerId(
            Long buyerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<OrderStatus> statuses
    ) {
        List<String> statusNames = FormatValidator.hasValue(statuses)
                ? statuses.stream().map(Enum::name).toList()
                : List.of();
        List<String> statusParam = statusNames.isEmpty() ? List.of("IGNORED") : statusNames;

        List<Long> orderIds = orderJpaRepository.findIdsByBuyerIdWithFilters(
                buyerId,
                startDate,
                endDate,
                statusParam,
                statusNames.size()
        );

        if (FormatValidator.hasNoValue(orderIds)) {
            return List.of();
        }

        List<OrderJpaEntity> entities = orderJpaRepository.findWithProductsByIds(orderIds);

        Map<Long, Integer> orderIndex = new HashMap<>();
        for (int i = 0; i < orderIds.size(); i++) {
            orderIndex.put(orderIds.get(i), i);
        }

        return entities.stream()
                .sorted(Comparator.comparingInt(
                        entity -> orderIndex.getOrDefault(entity.getId(), Integer.MAX_VALUE))
                )
                .map(OrderJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(order -> order.getOrderStatus() != OrderStatus.PAYMENT_PENDING)
                .toList();
    }

    @Override
    public List<Order> findAllWithFilters(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus orderStatus
    ) {
        String orderStatusParam = FormatValidator.hasValue(orderStatus)
                ? orderStatus.name()
                : "";

        List<Long> orderIds = orderJpaRepository.findIdsByAdminFilters(
                sellerId,
                startDate,
                endDate,
                orderStatusParam
        );

        if (FormatValidator.hasNoValue(orderIds)) {
            return List.of();
        }

        List<OrderJpaEntity> entities = orderJpaRepository.findWithProductsByIds(orderIds);

        Map<Long, Integer> orderIndex = new HashMap<>();
        for (int i = 0; i < orderIds.size(); i++) {
            orderIndex.put(orderIds.get(i), i);
        }

        return entities.stream()
                .sorted(Comparator.comparingInt(
                        entity -> orderIndex.getOrDefault(entity.getId(), Integer.MAX_VALUE))
                )
                .map(OrderJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public void update(Order order, OrderStatusHistory orderStatusHistory) throws OrderNotFoundException {
        OrderJpaEntity orderJpaEntity = findEntityById(order.getId());
        orderJpaEntity.updateFrom(order);
        orderHistoryJpaRepository.save(OrderStatusHistoryJpaEntity.from(orderStatusHistory, orderJpaEntity));
    }

    private OrderJpaEntity findEntityById(Long id) throws OrderNotFoundException {
        return orderJpaRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public Optional<OrderProduct> findByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId) {
        return OrderJpaEntityToDomainMapper.mapToOrderProductDomain(
                orderProductJpaRepository.findByOrderIdAndPricePolicyId(orderId, pricePolicyId).orElse(null)
        );
    }

    @Override
    public void update(OrderProduct orderProduct) throws OrderProductNotFoundException {
        OrderProductJpaEntity orderProductJpaEntity
                = findEntityByOrderIdAndPricePolicyId(orderProduct.getOrderId(), orderProduct.getPricePolicyId());
        orderProductJpaEntity.updateFrom(orderProduct);
    }

    private OrderProductJpaEntity findEntityByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId) throws OrderProductNotFoundException {
        return orderProductJpaRepository.findByOrderIdAndPricePolicyId(orderId, pricePolicyId)
                .orElseThrow(() -> new OrderProductNotFoundException(orderId, pricePolicyId));
    }
}
