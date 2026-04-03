package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.out.product.result.ProductOptionInfoResult;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record GetOrderProductResult(
        Long sellerId,
        Long productId,
        Long pricePolicyId,
        UUID sharerKey,
        Integer quantity,
        Long unitAmount,
        String imageUrl,
        OrderStatus orderStatus,
        String brandName,
        String productName,
        List<ProductOptionInfoResult> selectedOptions,
        Boolean isReviewed
) {
    public static GetOrderProductResult from(
            OrderProduct orderProduct,
            ProductInfoResult productInfo,
            OrderStatus defaultOrderStatus
    ) {
        OrderStatus resolvedStatus = resolveOrderStatus(orderProduct, defaultOrderStatus);
        boolean orderedProductExists = FormatValidator.hasValue(productInfo);

        return GetOrderProductResult.builder()
                .sellerId(orderProduct.getSellerId())
                .productId(
                        orderedProductExists
                                ? productInfo.id()
                                : null
                )
                .pricePolicyId(orderProduct.getPricePolicyId())
                .sharerKey(orderProduct.getSharerKey())
                .quantity(orderProduct.getQuantity())
                .unitAmount(orderProduct.getUnitAmount())
                .imageUrl(orderProduct.getImageUrl())
                .orderStatus(resolvedStatus)
                .brandName(
                        orderedProductExists
                                ? productInfo.brandName()
                                : null
                )
                .productName(
                        orderedProductExists
                                ? productInfo.name()
                                : null
                )
                .selectedOptions(
                        orderedProductExists
                                ? productInfo.selectedOptions()
                                : new ArrayList<>()
                )
                .isReviewed(orderProduct.getIsReviewed())
                .build();
    }

    private static OrderStatus resolveOrderStatus(
            OrderProduct orderProduct,
            OrderStatus defaultOrderStatus
    ) {
        OrderStatus status = orderProduct.getOrderStatus();
        if (FormatValidator.hasNoValue(status) || status.isPending()) {
            return FormatValidator.hasValue(defaultOrderStatus)
                    ? defaultOrderStatus
                    : OrderStatus.PAYMENT_PENDING;
        }

        return status;
    }
}
