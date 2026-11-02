package com.personal.marketnote.product.adapter.out.persistence.pricepolicy.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.QPricePolicyJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcategory.entity.QProductCategoryJpaEntity;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PricePolicySearchQueryRepositoryImpl implements PricePolicySearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PricePolicyJpaEntity> findActivePricePoliciesByOffset(
            List<Long> pricePolicyIds,
            int page,
            int pageSize,
            boolean isAsc,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword,
            Long categoryId
    ) {
        QPricePolicyJpaEntity pp = QPricePolicyJpaEntity.pricePolicyJpaEntity;

        BooleanBuilder where = new BooleanBuilder()
                .and(pp.status.eq(EntityStatus.ACTIVE))
                .and(pp.productJpaEntity.status.eq(EntityStatus.ACTIVE));

        if (FormatValidator.hasValue(pricePolicyIds)) {
            where.and(pp.id.in(pricePolicyIds));
        }

        if (FormatValidator.hasValue(categoryId)) {
            QProductCategoryJpaEntity pc = QProductCategoryJpaEntity.productCategoryJpaEntity;
            where.and(JPAExpressions
                    .selectOne()
                    .from(pc)
                    .where(pc.productId.eq(pp.productJpaEntity.id).and(pc.categoryId.eq(categoryId)))
                    .exists());
        }

        if (FormatValidator.hasValue(searchKeyword) && FormatValidator.hasValue(searchTarget)) {
            where.and(buildSearchExpression(pp, searchTarget, searchKeyword));
        }

        List<OrderSpecifier<?>> orders = buildOrderSpecifiers(pp, sortProperty, isAsc);

        return queryFactory
                .selectFrom(pp)
                .join(pp.productJpaEntity)
                .where(where)
                .orderBy(orders.toArray(new OrderSpecifier<?>[0]))
                .offset((long) page * pageSize)
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression buildSearchExpression(
            QPricePolicyJpaEntity pp,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        String pattern = "%" + searchKeyword + "%";
        return switch (searchTarget) {
            case NAME -> pp.productJpaEntity.name.like(pattern);
            case BRAND_NAME -> pp.productJpaEntity.brandName.like(pattern);
        };
    }

    private List<OrderSpecifier<?>> buildOrderSpecifiers(
            QPricePolicyJpaEntity pp,
            ProductSortProperty sortProperty,
            boolean isAsc
    ) {
        Order direction = isAsc ? Order.ASC : Order.DESC;
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        switch (sortProperty) {
            case ORDER_NUM -> orders.add(new OrderSpecifier<>(direction, pp.productJpaEntity.orderNum));
            case POPULARITY -> orders.add(new OrderSpecifier<>(direction, pp.popularity));
            case DISCOUNT_PRICE -> orders.add(new OrderSpecifier<>(direction, pp.discountPrice));
            case ACCUMULATED_POINT -> {
                orders.add(new OrderSpecifier<>(direction, pp.accumulatedPoint));
                orders.add(new OrderSpecifier<>(Order.DESC, pp.productJpaEntity.id));
            }
            case ACCUMULATED_POINT_RATE -> {
                orders.add(new OrderSpecifier<>(direction, pp.accumulationRate));
                orders.add(new OrderSpecifier<>(Order.DESC, pp.productJpaEntity.id));
            }
        }

        orders.add(new OrderSpecifier<>(direction, pp.id));
        return orders;
    }
}
