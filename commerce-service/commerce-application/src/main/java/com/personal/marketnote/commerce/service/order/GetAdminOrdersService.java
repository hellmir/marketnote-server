package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.command.order.GetAdminOrdersQuery;
import com.personal.marketnote.commerce.port.in.result.order.GetAdminOrdersResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetAdminOrdersUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 관리자 주문 내역 조회 서비스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 관리자가 전체 주문 내역을 판매자별, 기간별, 상태별로 조회합니다.
 */
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
@Slf4j
public class GetAdminOrdersService implements GetAdminOrdersUseCase {
    private final FindOrderPort findOrderPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Override
    public GetAdminOrdersResult getAdminOrders(GetAdminOrdersQuery query) {
        log.info("관리자 주문 조회 - sellerId={}, startDate={}, endDate={}, status={}",
                query.sellerId(), query.startDate(), query.endDate(), query.orderStatus());

        List<Order> orders = findOrderPort.findAllWithFilters(
                query.sellerId(),
                query.startDate(),
                query.endDate(),
                query.orderStatus()
        );

        if (FormatValidator.hasNoValue(orders)) {
            return GetAdminOrdersResult.empty();
        }

        List<Long> pricePolicyIds = orders.stream()
                .flatMap(order -> order.getOrderProducts().stream())
                .map(OrderProduct::getPricePolicyId)
                .filter(FormatValidator::hasValue)
                .distinct()
                .toList();

        Map<Long, ProductInfoResult> productInfoMap = FormatValidator.hasNoValue(pricePolicyIds)
                ? Map.of()
                : Optional.ofNullable(findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds))
                .orElse(Map.of());

        return GetAdminOrdersResult.from(orders, productInfoMap);
    }
}
