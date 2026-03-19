package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.OrderProductNotFoundException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderProductsQuery;
import com.personal.marketnote.commerce.port.in.result.order.*;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderProductPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.time.LocalDate.now;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
@Slf4j
public class GetOrderService implements GetOrderUseCase {
    private final FindOrderPort findOrderPort;
    private final FindOrderProductPort findOrderProductPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Override
    public GetOrderResult getOrderAndOrderProducts(Long id) {
        return getOrderAndOrderProductsInternal(getOrder(id));
    }

    @Override
    public GetOrderResult getOrderAndOrderProducts(Long id, Long buyerId) {
        Order order = getOrderAndVerifyOwner(id, buyerId);

        return getOrderAndOrderProductsInternal(order);
    }

    private GetOrderResult getOrderAndOrderProductsInternal(Order order) {
        Map<Long, ProductInfoResult> orderedProductsByPricePolicyId = Optional.ofNullable(
                        findProductByPricePolicyPort.findByPricePolicyIds(
                                order.getOrderProducts()
                                        .stream()
                                        .map(OrderProduct::getPricePolicyId)
                                        .toList()
                        )
                )
                .orElse(Map.of());

        return GetOrderResult.from(order, orderedProductsByPricePolicyId);
    }

    @Override
    public Order getOrder(Long id) {
        return findOrderPort.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public GetBuyerOrdersResult getBuyerOrderHistory(GetBuyerOrderHistoryQuery query) {
        BuyerOrdersAndProductsResult buyerOrdersAndProductsResult = findBuyerOrders(query, true);

        if (FormatValidator.hasNoValue(buyerOrdersAndProductsResult.orders())) {
            return GetBuyerOrdersResult.of(List.of(), Map.of());
        }

        return GetBuyerOrdersResult.of(
                buyerOrdersAndProductsResult.orders(),
                buyerOrdersAndProductsResult.orderedProductsByPricePolicyId()
        );
    }

    @Override
    public GetOrderCountResult getBuyerOrderCount(GetBuyerOrderHistoryQuery query) {
        BuyerOrdersAndProductsResult buyerOrdersAndProductsResult = findBuyerOrders(query, false);

        return GetOrderCountResult.of(buyerOrdersAndProductsResult.orders().size());
    }

    @Override
    public GetBuyerOrderProductsResult getBuyerOrderProducts(GetBuyerOrderProductsQuery query) {
        List<Order> orders = findOrderPort.findByBuyerId(query.buyerId(), null, null, List.of());

        if (FormatValidator.hasNoValue(orders)) {
            return GetBuyerOrderProductsResult.of(List.of());
        }

        Predicate<OrderProduct> orderProductFilter = orderProduct
                -> orderProduct.isConfirmed()
                && query.matchesReviewStatus(orderProduct.getIsReviewed());
        Map<Long, ProductInfoResult> orderedProductsByPricePolicyId
                = findOrderedProductsByPricePolicyId(orders, orderProductFilter);

        List<GetBuyerOrderProductResult> orderProducts = orders.stream()
                .flatMap(order -> order.getOrderProducts().stream()
                        .filter(orderProductFilter)
                        .map(orderProduct -> GetBuyerOrderProductResult.from(
                                order,
                                orderProduct,
                                orderedProductsByPricePolicyId.get(orderProduct.getPricePolicyId())
                        ))
                )
                .toList();

        return GetBuyerOrderProductsResult.of(orderProducts);
    }

    @Override
    public GetOrderKeyResult getOrderKey(Long id) {
        Order order = getOrder(id);

        return GetOrderKeyResult.from(order);
    }

    @Override
    public GetOrderKeyResult getOrderKey(Long id, Long buyerId) {
        Order order = getOrderAndVerifyOwner(id, buyerId);

        return GetOrderKeyResult.from(order);
    }

    @Override
    public OrderProduct getOrderProduct(Long orderId, Long pricePolicyId) {
        return findOrderProductPort.findByOrderIdAndPricePolicyId(orderId, pricePolicyId)
                .orElseThrow(() -> new OrderProductNotFoundException(orderId, pricePolicyId));
    }

    private Order getOrderAndVerifyOwner(Long id, Long buyerId) {
        Order order = getOrder(id);

        if (!order.getBuyerId().equals(buyerId)) {
            log.warn("주문 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}", id, order.getBuyerId(), buyerId);
            throw new UnauthorizedOrderAccessException();
        }

        return order;
    }

    private BuyerOrdersAndProductsResult findBuyerOrders(GetBuyerOrderHistoryQuery query, boolean includeProductDetails) {
        LocalDate today = now();
        List<Order> orders = findOrderPort.findByBuyerId(
                query.buyerId(),
                query.calculateStartDate(today),
                query.calculateEndDate(today),
                query.resolveStatuses()
        );

        if (FormatValidator.hasNoValue(orders)) {
            return BuyerOrdersAndProductsResult.empty();
        }

        String productNameKeyword = query.resolvedProductName();
        boolean needProductDetails = includeProductDetails || FormatValidator.hasValue(productNameKeyword);

        Map<Long, ProductInfoResult> orderedProductsByPricePolicyId = needProductDetails
                ? findOrderedProductsByPricePolicyId(orders)
                : Map.of();

        List<Order> filteredOrders = filterOrdersByProductName(
                orders,
                orderedProductsByPricePolicyId,
                productNameKeyword
        );

        return BuyerOrdersAndProductsResult.of(filteredOrders, orderedProductsByPricePolicyId);
    }

    private Map<Long, ProductInfoResult> findOrderedProductsByPricePolicyId(List<Order> orders) {
        return findOrderedProductsByPricePolicyId(orders, orderProduct -> true);
    }

    private Map<Long, ProductInfoResult> findOrderedProductsByPricePolicyId(
            List<Order> orders,
            Predicate<OrderProduct> orderProductFilter
    ) {
        List<Long> pricePolicyIds = orders.stream()
                .flatMap(order -> order.getOrderProducts().stream())
                .filter(orderProductFilter)
                .map(OrderProduct::getPricePolicyId)
                .filter(FormatValidator::hasValue)
                .distinct()
                .toList();

        if (FormatValidator.hasNoValue(pricePolicyIds)) {
            return Map.of();
        }

        return Optional.ofNullable(findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds))
                .orElse(Map.of());
    }

    private List<Order> filterOrdersByProductName(
            List<Order> orders,
            Map<Long, ProductInfoResult> orderedProductsByPricePolicyId,
            String productNameKeyword
    ) {
        if (FormatValidator.hasNoValue(productNameKeyword)) {
            return orders;
        }

        if (FormatValidator.hasNoValue(orderedProductsByPricePolicyId)) {
            return List.of();
        }

        String keyword = productNameKeyword.toLowerCase();

        return orders.stream()
                .filter(order -> order.getOrderProducts().stream()
                        .anyMatch(orderProduct -> {
                            ProductInfoResult productInfo
                                    = orderedProductsByPricePolicyId.get(orderProduct.getPricePolicyId());

                            return FormatValidator.hasValue(productInfo)
                                    && FormatValidator.hasValue(productInfo.name())
                                    && FormatValidator.containsKeyword(productInfo.name(), keyword);
                        })
                )
                .toList();
    }
}
