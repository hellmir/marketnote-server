package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderHistoryQuery;
import com.personal.marketnote.commerce.port.in.command.order.GetBuyerOrderProductsQuery;
import com.personal.marketnote.commerce.port.in.result.order.*;

/**
 * 주문 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 주문 조회 관련 기능을 제공합니다.
 */
public interface GetOrderUseCase {
    /**
     * @param id 주문 ID
     * @return 주문 조회 결과 {@link GetOrderResult}
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 주문과 주문 상품 정보를 조회합니다. (내부용, 소유자 검증 없음)
     */
    GetOrderResult getOrderAndOrderProducts(Long id);

    /**
     * @param id      주문 ID
     * @param buyerId 요청 구매자 ID
     * @return 주문 조회 결과 {@link GetOrderResult}
     * @Date 2026-02-27
     * @Author 성효빈
     * @Description 구매자 소유자 검증 후 주문과 주문 상품 정보를 조회합니다.
     */
    GetOrderResult getOrderAndOrderProducts(Long id, Long buyerId);

    /**
     * @param id 주문 ID
     * @return 주문 도메인 {@link Order}
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 주문 정보를 조회합니다.
     */
    Order getOrder(Long id);

    /**
     * @param query 구매자 주문 내역 조회 쿼리
     * @return 구매자 주문 내역 조회 결과 {@link GetBuyerOrdersResult}
     * @Date 2026-01-05
     * @Author 성효빈
     * @Description 구매자 주문 내역을 조회합니다.
     */
    GetBuyerOrdersResult getBuyerOrderHistory(GetBuyerOrderHistoryQuery query);

    /**
     * @param query 구매자 주문 상품 목록 조회 쿼리
     * @return 구매자 주문 상품 목록 조회 결과 {@link GetBuyerOrderProductsResult}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 구매자의 주문 상품 목록을 조회합니다.
     */
    GetBuyerOrderProductsResult getBuyerOrderProducts(GetBuyerOrderProductsQuery query);

    /**
     * @param query 구매자 주문 내역 개수 조회 쿼리
     * @return 구매자 주문 내역 개수 조회 결과 {@link GetOrderCountResult}
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 구매자 주문 내역 개수를 조회합니다.
     */
    GetOrderCountResult getBuyerOrderCount(GetBuyerOrderHistoryQuery query);

    /**
     * @param id 주문 ID
     * @return 주문 키 조회 결과 {@link GetOrderKeyResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 주문 키를 조회합니다. (내부용, 소유자 검증 없음)
     */
    GetOrderKeyResult getOrderKey(Long id);

    /**
     * @param id      주문 ID
     * @param buyerId 요청 구매자 ID
     * @return 주문 키 조회 결과 {@link GetOrderKeyResult}
     * @Date 2026-02-27
     * @Author 성효빈
     * @Description 구매자 소유자 검증 후 주문 키를 조회합니다.
     */
    GetOrderKeyResult getOrderKey(Long id, Long buyerId);

    /**
     * @param orderId       주문 ID
     * @param pricePolicyId 가격 정책 ID
     * @return 주문 상품 도메인 {@link OrderProduct}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 주문 상품 정보를 조회합니다.
     */
    OrderProduct getOrderProduct(Long orderId, Long pricePolicyId);
}
