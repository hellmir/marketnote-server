package com.personal.marketnote.commerce.port.in.command.order;

public record UpdateOrderProductReviewStatusCommand(
        Long orderId,
        Long pricePolicyId,
        Boolean isReviewed
) {
    public static UpdateOrderProductReviewStatusCommand of(
            Long orderId,
            Long pricePolicyId,
            Boolean isReviewed
    ) {
        return new UpdateOrderProductReviewStatusCommand(orderId, pricePolicyId, isReviewed);
    }
}
