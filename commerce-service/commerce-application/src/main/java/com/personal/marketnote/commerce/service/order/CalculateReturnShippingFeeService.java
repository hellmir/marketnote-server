package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.returnshipping.*;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.ReasonCategoryNoValueException;
import com.personal.marketnote.commerce.exception.ShippingPolicyNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CalculateReturnShippingFeeUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class CalculateReturnShippingFeeService implements CalculateReturnShippingFeeUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;

    @Override
    public CalculateReturnShippingFeeResult calculateReturnShippingFee(CalculateReturnShippingFeeCommand command) {
        validateReasonCategory(command);

        FaultType faultType = command.reasonCategory().getFaultType();
        Order order = getOrderUseCase.getOrder(command.orderId());
        List<OrderProduct> orderProducts = order.getOrderProducts();

        Set<Long> returnPricePolicyIdSet = resolveReturnPricePolicyIdSet(command, orderProducts);
        ReturnType returnType = resolveReturnType(returnPricePolicyIdSet, orderProducts);

        List<Long> sellerIds = extractDistinctSellerIds(orderProducts);
        Map<Long, Long> initialShippingFeeBySellerIdMap = buildInitialShippingFeeMap(command.orderId());
        Map<Long, ShippingPolicyInfoResult> shippingPolicyMap = findShippingPolicyBySellerIdsPort.findBySellerIds(sellerIds);

        long totalReturnShippingFee = calculateTotalReturnShippingFee(
                faultType, returnType, orderProducts, returnPricePolicyIdSet,
                initialShippingFeeBySellerIdMap, shippingPolicyMap
        );

        return CalculateReturnShippingFeeResult.builder()
                .returnShippingFee(totalReturnShippingFee)
                .build();
    }

    private void validateReasonCategory(CalculateReturnShippingFeeCommand command) {
        if (FormatValidator.hasNoValue(command.reasonCategory())) {
            throw new ReasonCategoryNoValueException();
        }
        if (!command.reasonCategory().isReturnReason()) {
            throw new InvalidReasonCategoryException(command.reasonCategory());
        }
    }

    private Set<Long> resolveReturnPricePolicyIdSet(CalculateReturnShippingFeeCommand command, List<OrderProduct> orderProducts) {
        Set<Long> validPricePolicyIds = orderProducts.stream()
                .map(OrderProduct::getPricePolicyId)
                .collect(Collectors.toSet());

        if (FormatValidator.hasNoValue(command.returnPricePolicyIds())) {
            return validPricePolicyIds;
        }

        Set<Long> requestedIds = new HashSet<>(command.returnPricePolicyIds());
        requestedIds.retainAll(validPricePolicyIds);
        return requestedIds;
    }

    private ReturnType resolveReturnType(Set<Long> returnPricePolicyIdSet, List<OrderProduct> orderProducts) {
        if (returnPricePolicyIdSet.size() >= orderProducts.size()) {
            return ReturnType.FULL_RETURN;
        }
        return ReturnType.PARTIAL_RETURN;
    }

    private List<Long> extractDistinctSellerIds(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(OrderProduct::getSellerId)
                .distinct()
                .toList();
    }

    private Map<Long, Long> buildInitialShippingFeeMap(Long orderId) {
        List<PaymentAllocation> allocations = findPaymentAllocationPort.findByOrderId(orderId);
        return allocations.stream()
                .collect(Collectors.toMap(
                        PaymentAllocation::getSellerId,
                        PaymentAllocation::getShippingFee,
                        (existing, replacement) -> existing
                ));
    }

    private long calculateTotalReturnShippingFee(
            FaultType faultType,
            ReturnType returnType,
            List<OrderProduct> orderProducts,
            Set<Long> returnPricePolicyIdSet,
            Map<Long, Long> initialShippingFeeBySellerIdMap,
            Map<Long, ShippingPolicyInfoResult> shippingPolicyMap
    ) {
        Map<Long, List<OrderProduct>> productsBySeller = orderProducts.stream()
                .collect(Collectors.groupingBy(OrderProduct::getSellerId));

        long totalFee = 0L;
        for (Map.Entry<Long, List<OrderProduct>> entry : productsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<OrderProduct> sellerProducts = entry.getValue();

            boolean hasReturnTarget = sellerProducts.stream()
                    .anyMatch(product -> returnPricePolicyIdSet.contains(product.getPricePolicyId()));
            if (!hasReturnTarget) {
                continue;
            }

            long initialShippingFee = initialShippingFeeBySellerIdMap.getOrDefault(sellerId, 0L);
            InitialShippingType initialShippingType = InitialShippingType.from(initialShippingFee);

            ShippingPolicyInfoResult policy = shippingPolicyMap.get(sellerId);
            if (FormatValidator.hasNoValue(policy)) {
                throw new ShippingPolicyNotFoundException(sellerId);
            }
            long oneWayFee = policy.shippingFee();
            long freeShippingThreshold = policy.freeShippingThreshold();

            ReturnType sellerReturnType = resolveSellerReturnType(sellerProducts, returnPricePolicyIdSet, returnType);
            long remainingAmount = calculateRemainingAmount(sellerProducts, returnPricePolicyIdSet);

            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    faultType, initialShippingType, sellerReturnType,
                    remainingAmount, freeShippingThreshold, oneWayFee
            );

            totalFee = Math.addExact(totalFee, ReturnShippingFeeCalculator.calculate(context));
        }
        return totalFee;
    }

    private ReturnType resolveSellerReturnType(
            List<OrderProduct> sellerProducts,
            Set<Long> returnPricePolicyIdSet,
            ReturnType orderReturnType
    ) {
        if (orderReturnType.isFullReturn()) {
            return ReturnType.FULL_RETURN;
        }

        boolean allReturned = sellerProducts.stream()
                .allMatch(product -> returnPricePolicyIdSet.contains(product.getPricePolicyId()));
        if (allReturned) {
            return ReturnType.FULL_RETURN;
        }
        return ReturnType.PARTIAL_RETURN;
    }

    private long calculateRemainingAmount(List<OrderProduct> sellerProducts, Set<Long> returnPricePolicyIdSet) {
        return sellerProducts.stream()
                .filter(product -> !returnPricePolicyIdSet.contains(product.getPricePolicyId()))
                .mapToLong(product -> Math.multiplyExact(product.getUnitAmount(), product.getQuantity().longValue()))
                .reduce(0L, Math::addExact);
    }
}
