package com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.QProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class ProductCatalogReadModelQueryRepositoryImpl implements ProductCatalogReadModelQueryRepository {

    private static final QProductCatalogReadModelJpaEntity catalog =
            QProductCatalogReadModelJpaEntity.productCatalogReadModelJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductCatalogReadModelJpaEntity> findCatalogItems(
            Long categoryId,
            List<Long> pricePolicyIds,
            Long cursor,
            int pageSize,
            boolean isAsc,
            ProductSortProperty sortProperty,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        boolean hasExplicitCursor = FormatValidator.hasValue(cursor) && cursor > 0;
        Object cursorSortValue = resolveCursorSortValue(cursor, sortProperty);
        if (hasExplicitCursor && FormatValidator.hasNoValue(cursorSortValue)) {
            // 커서 레코드가 삭제/비활성화되어 정렬 기준값을 복원할 수 없다.
            // 1페이지 재조회(무한 스크롤 중복 노출) 를 막기 위해 빈 결과를 반환한다.
            return List.of();
        }

        return queryFactory
                .selectFrom(catalog)
                .where(
                        catalog.status.eq(EntityStatus.ACTIVE),
                        categoryIdEq(categoryId),
                        pricePolicyIdsIn(pricePolicyIds),
                        searchKeywordLike(searchTarget, searchKeyword),
                        cursorCondition(sortProperty, cursor, cursorSortValue, isAsc)
                )
                .orderBy(sortOrderSpecifiers(sortProperty, isAsc))
                .limit(pageSize)
                .fetch();
    }

    @Override
    public long countCatalogItems(
            Long categoryId,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        Long count = queryFactory
                .select(catalog.count())
                .from(catalog)
                .where(
                        catalog.status.eq(EntityStatus.ACTIVE),
                        categoryIdEq(categoryId),
                        searchKeywordLike(searchTarget, searchKeyword)
                )
                .fetchOne();
        if (FormatValidator.hasNoValue(count)) {
            return 0L;
        }
        return count;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        if (FormatValidator.hasNoValue(categoryId)) {
            return null;
        }
        return catalog.primaryCategoryId.eq(categoryId);
    }

    private BooleanExpression pricePolicyIdsIn(List<Long> pricePolicyIds) {
        if (FormatValidator.hasNoValue(pricePolicyIds)) {
            return null;
        }
        return catalog.pricePolicyId.in(pricePolicyIds);
    }

    private BooleanExpression searchKeywordLike(ProductSearchTarget searchTarget, String searchKeyword) {
        if (FormatValidator.hasNoValue(searchKeyword) || FormatValidator.hasNoValue(searchTarget)) {
            return null;
        }
        StringPath path = searchPath(searchTarget);
        return path.like("%" + searchKeyword + "%");
    }

    private BooleanExpression cursorCondition(
            ProductSortProperty sortProperty,
            Long cursor,
            Object cursorSortValue,
            boolean isAsc
    ) {
        if (FormatValidator.hasNoValue(cursorSortValue)) {
            return null;
        }
        return switch (sortProperty) {
            case ORDER_NUM -> longCursor(catalog.orderNum, cursor, (Long) cursorSortValue, isAsc);
            case POPULARITY -> longCursor(catalog.popularity, cursor, (Long) cursorSortValue, isAsc);
            case ACCUMULATED_POINT -> longCursor(catalog.accumulatedPoint, cursor, (Long) cursorSortValue, isAsc);
            case ACCUMULATED_POINT_RATE -> bigDecimalCursor(
                    catalog.accumulatedPointRate, cursor, (BigDecimal) cursorSortValue, isAsc
            );
            case DISCOUNT_PRICE -> longCursor(catalog.discountPrice, cursor, (Long) cursorSortValue, isAsc);
        };
    }

    private BooleanExpression longCursor(NumberPath<Long> sortPath, Long cursor, Long cursorSortValue, boolean isAsc) {
        if (isAsc) {
            return sortPath.gt(cursorSortValue)
                    .or(sortPath.eq(cursorSortValue).and(catalog.pricePolicyId.gt(cursor)));
        }
        return sortPath.lt(cursorSortValue)
                .or(sortPath.eq(cursorSortValue).and(catalog.pricePolicyId.lt(cursor)));
    }

    private BooleanExpression bigDecimalCursor(
            NumberPath<BigDecimal> sortPath,
            Long cursor,
            BigDecimal cursorSortValue,
            boolean isAsc
    ) {
        if (isAsc) {
            return sortPath.gt(cursorSortValue)
                    .or(sortPath.eq(cursorSortValue).and(catalog.pricePolicyId.gt(cursor)));
        }
        return sortPath.lt(cursorSortValue)
                .or(sortPath.eq(cursorSortValue).and(catalog.pricePolicyId.lt(cursor)));
    }

    private OrderSpecifier<?>[] sortOrderSpecifiers(ProductSortProperty sortProperty, boolean isAsc) {
        OrderSpecifier<?> primary = primaryOrderSpecifier(sortProperty, isAsc);
        OrderSpecifier<Long> tieBreaker = isAsc ? catalog.pricePolicyId.asc() : catalog.pricePolicyId.desc();
        return new OrderSpecifier<?>[]{primary, tieBreaker};
    }

    private OrderSpecifier<?> primaryOrderSpecifier(ProductSortProperty sortProperty, boolean isAsc) {
        return switch (sortProperty) {
            case ORDER_NUM -> isAsc ? catalog.orderNum.asc() : catalog.orderNum.desc();
            case POPULARITY -> isAsc ? catalog.popularity.asc() : catalog.popularity.desc();
            case ACCUMULATED_POINT -> isAsc ? catalog.accumulatedPoint.asc() : catalog.accumulatedPoint.desc();
            case ACCUMULATED_POINT_RATE -> isAsc ? catalog.accumulatedPointRate.asc() : catalog.accumulatedPointRate.desc();
            case DISCOUNT_PRICE -> isAsc ? catalog.discountPrice.asc() : catalog.discountPrice.desc();
        };
    }

    private StringPath searchPath(ProductSearchTarget searchTarget) {
        return switch (searchTarget) {
            case NAME -> catalog.name;
            case BRAND_NAME -> catalog.brandName;
        };
    }

    private Object resolveCursorSortValue(Long cursor, ProductSortProperty sortProperty) {
        if (FormatValidator.hasNoValue(cursor) || cursor <= 0) {
            return null;
        }
        ProductCatalogReadModelJpaEntity entity = queryFactory
                .selectFrom(catalog)
                .where(catalog.pricePolicyId.eq(cursor))
                .fetchFirst();
        if (FormatValidator.hasNoValue(entity)) {
            return null;
        }
        return extractSortValue(entity, sortProperty);
    }

    private Object extractSortValue(ProductCatalogReadModelJpaEntity entity, ProductSortProperty sortProperty) {
        return switch (sortProperty) {
            case ORDER_NUM -> entity.getOrderNum();
            case POPULARITY -> entity.getPopularity();
            case ACCUMULATED_POINT -> entity.getAccumulatedPoint();
            case ACCUMULATED_POINT_RATE -> entity.getAccumulatedPointRate();
            case DISCOUNT_PRICE -> entity.getDiscountPrice();
        };
    }
}
