package com.personal.marketnote.commerce.port.in.command.settlement;

/**
 * 판매자 정산 내역 조회 쿼리
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 판매자 본인의 정산 내역 조회에 필요한 조건을 개입화합니다.
 */
public record GetSellerSettlementsQuery(
        Long sellerId,
        Integer year,
        Integer month
) {
    public static GetSellerSettlementsQuery of(Long sellerId, Integer year, Integer month) {
        return new GetSellerSettlementsQuery(sellerId, year, month);
    }
}
