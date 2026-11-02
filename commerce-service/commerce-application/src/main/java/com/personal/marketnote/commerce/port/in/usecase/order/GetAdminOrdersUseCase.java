package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.GetAdminOrdersQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;

/**
 * 관리자 주문 내역 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 관리자가 전체 주문 내역을 판매자별, 기간별, 상태별로 조회합니다.
 */
public interface GetAdminOrdersUseCase {
    GetAdminOrdersResult getAdminOrders(GetAdminOrdersQuery query);
}
