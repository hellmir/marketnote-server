package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.out.product.result.ProductOptionInfoResult;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetBuyerOrderProductResult(
        Long orderId,
        String orderNumber,
        LocalDate orderDate,
        OrderStatus orderStatus,
        Long sellerId,
        Long productId,
        Long pricePolicyId,
        Long sharerId,
        Integer quantity,
        Long unitAmount,
        String imageUrl,
        String brandName,
        String productName,
        List<ProductOptionInfoResult> selectedOptions,
        Boolean isReviewed,
        LocalDateTime reviewDeadline
) {
    public static GetBuyerOrderProductResult from(
            Order order,
            OrderProduct orderProduct,
            ProductInfoResult productInfo
    ) {
        GetOrderProductResult orderProductResult = GetOrderProductResult.from(
                orderProduct,
                productInfo,
                order.getOrderStatus()
        );
        LocalDate resolvedOrderDate = FormatValidator.hasValue(order.getCreatedAt())
                ? order.getCreatedAt().toLocalDate()
                : LocalDate.now();

        return GetBuyerOrderProductResult.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(resolvedOrderDate)
                .orderStatus(orderProductResult.orderStatus())
                .sellerId(orderProductResult.sellerId())
                .productId(orderProductResult.productId())
                .pricePolicyId(orderProductResult.pricePolicyId())
                .sharerId(orderProductResult.sharerId())
                .quantity(orderProductResult.quantity())
                .unitAmount(orderProductResult.unitAmount())
                .imageUrl(orderProductResult.imageUrl())
                .brandName(orderProductResult.brandName())
                .productName(orderProductResult.productName())
                .selectedOptions(orderProductResult.selectedOptions())
                .isReviewed(orderProductResult.isReviewed())
                .reviewDeadline(orderProduct.calculateReviewDeadline())
                .build();
    }
}
