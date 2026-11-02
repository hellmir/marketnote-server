package com.personal.marketnote.product.port.in.usecase.cart;

import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;

/**
 * 장바구니 상품 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 장바구니 상품 삭제 기능을 제공합니다.
 */
public interface DeleteCartProductUseCase {
    /**
     * @param command 장바구니 상품 삭제 커맨드
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 장바구니 상품을 삭제합니다.
     */
    void deleteCartProducts(DeleteCartProductCommand command);

    /**
     * @param userId 회원 ID
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 장바구니를 비웁니다.
     */
    void deleteAllCartProducts(Long userId);
}
