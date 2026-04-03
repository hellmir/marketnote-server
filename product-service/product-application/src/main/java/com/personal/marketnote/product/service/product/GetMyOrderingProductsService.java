package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.port.in.command.GetMyOrderingProductsQuery;
import com.personal.marketnote.product.port.in.command.OrderingItemQuery;
import com.personal.marketnote.product.port.in.result.cart.GetCartProductResult;
import com.personal.marketnote.product.port.in.result.product.GetMyOrderProductsResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetMyOrderingProductsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductInventoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetMyOrderingProductsService implements GetMyOrderingProductsUseCase {
    private final GetPricePoliciesUseCase getPricePoliciesUseCase;
    private final GetProductInventoryUseCase getProductInventoryUseCase;

    @Override
    public GetMyOrderProductsResult getMyOrderingProducts(GetMyOrderingProductsQuery query) {
        List<Long> pricePolicyIds = query.orderingItemQueries()
                .stream()
                .map(OrderingItemQuery::pricePolicyId)
                .toList();

        List<PricePolicy> pricePolicies = getPricePoliciesUseCase.getPricePoliciesAndOptions(pricePolicyIds);

        // 상품 재고 수량 조회
        Map<Long, Integer> stocks = getProductInventoryUseCase.getProductStocks(pricePolicyIds);

        List<GetCartProductResult> orderingProducts = new ArrayList<>();
        pricePolicies.forEach(
                pricePolicy -> query.orderingItemQueries()
                        .stream()
                        .filter(
                                request -> FormatValidator.equals(
                                        request.pricePolicyId(), pricePolicy.getId()
                                )
                        )
                        .findFirst()
                        .ifPresent(
                                itemQuery -> orderingProducts.add(
                                        GetCartProductResult.of(
                                                pricePolicy,
                                                itemQuery.imageUrl(),
                                                itemQuery.quantity(),
                                                stocks.get(pricePolicy.getId()),
                                                itemQuery.sharerKey()
                                        )
                                )
                        )
        );

        return GetMyOrderProductsResult.from(orderingProducts);
    }
}
