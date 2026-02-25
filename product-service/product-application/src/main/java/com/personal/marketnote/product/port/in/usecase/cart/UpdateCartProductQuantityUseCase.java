package com.personal.marketnote.product.port.in.usecase.cart;

import com.personal.marketnote.product.port.in.command.UpdateCartProductQuantityCommand;

/**
 * 장바구니 상품 수량 변경 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 장바구니 상품 수량 변경 기능을 제공합니다.
 */
public interface UpdateCartProductQuantityUseCase {
    /**
     * @param command 장바구니 상품 수량 변경 커맨드
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 장바구니 상품 수량을 변경합니다.
     */
    void updateCartProductQuantity(UpdateCartProductQuantityCommand command);
}
