package com.personal.marketnote.product.adapter.out.persistence.productcatalog.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProductCatalogReadModelQueryRepositoryImpl implements ProductCatalogReadModelQueryRepository {

    private final EntityManager entityManager;

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
        String sortColumn = resolveSortColumn(sortProperty);
        String direction = isAsc ? "ASC" : "DESC";
        String comparator = isAsc ? ">" : "<";

        Object cursorSortValue = resolveCursorSortValue(cursor, sortColumn);
        boolean hasCursor = FormatValidator.hasValue(cursorSortValue);

        StringBuilder jpql = new StringBuilder(
                "SELECT p FROM ProductCatalogReadModelJpaEntity p WHERE p.status = :status "
        );

        if (FormatValidator.hasValue(categoryId)) {
            jpql.append("AND p.primaryCategoryId = :categoryId ");
        }

        if (FormatValidator.hasValue(pricePolicyIds)) {
            jpql.append("AND p.pricePolicyId IN :pricePolicyIds ");
        }

        if (FormatValidator.hasValue(searchKeyword) && FormatValidator.hasValue(searchTarget)) {
            String searchColumn = resolveSearchColumn(searchTarget);
            jpql.append("AND p.").append(searchColumn).append(" LIKE :searchKeyword ");
        }

        if (hasCursor) {
            jpql.append("AND (p.").append(sortColumn).append(" ").append(comparator).append(" :cursorSortValue ")
                    .append("OR (p.").append(sortColumn).append(" = :cursorSortValue ")
                    .append("AND p.pricePolicyId ").append(comparator).append(" :cursor)) ");
        }

        jpql.append("ORDER BY p.").append(sortColumn).append(" ").append(direction);
        jpql.append(", p.pricePolicyId ").append(direction);

        TypedQuery<ProductCatalogReadModelJpaEntity> query =
                entityManager.createQuery(jpql.toString(), ProductCatalogReadModelJpaEntity.class);
        query.setParameter("status", EntityStatus.ACTIVE);

        if (FormatValidator.hasValue(categoryId)) {
            query.setParameter("categoryId", categoryId);
        }

        if (FormatValidator.hasValue(pricePolicyIds)) {
            query.setParameter("pricePolicyIds", pricePolicyIds);
        }

        if (FormatValidator.hasValue(searchKeyword) && FormatValidator.hasValue(searchTarget)) {
            query.setParameter("searchKeyword", "%" + searchKeyword + "%");
        }

        if (hasCursor) {
            query.setParameter("cursorSortValue", cursorSortValue);
            query.setParameter("cursor", cursor);
        }

        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    @Override
    public long countCatalogItems(
            Long categoryId,
            ProductSearchTarget searchTarget,
            String searchKeyword
    ) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(p) FROM ProductCatalogReadModelJpaEntity p WHERE p.status = :status "
        );

        if (FormatValidator.hasValue(categoryId)) {
            jpql.append("AND p.primaryCategoryId = :categoryId ");
        }

        if (FormatValidator.hasValue(searchKeyword) && FormatValidator.hasValue(searchTarget)) {
            String searchColumn = resolveSearchColumn(searchTarget);
            jpql.append("AND p.").append(searchColumn).append(" LIKE :searchKeyword ");
        }

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        query.setParameter("status", EntityStatus.ACTIVE);

        if (FormatValidator.hasValue(categoryId)) {
            query.setParameter("categoryId", categoryId);
        }

        if (FormatValidator.hasValue(searchKeyword) && FormatValidator.hasValue(searchTarget)) {
            query.setParameter("searchKeyword", "%" + searchKeyword + "%");
        }

        return query.getSingleResult();
    }

    private Object resolveCursorSortValue(Long cursor, String sortColumn) {
        if (FormatValidator.hasNoValue(cursor) || cursor <= 0) {
            return null;
        }
        TypedQuery<ProductCatalogReadModelJpaEntity> cursorQuery = entityManager.createQuery(
                "SELECT p FROM ProductCatalogReadModelJpaEntity p WHERE p.pricePolicyId = :pricePolicyId",
                ProductCatalogReadModelJpaEntity.class
        );
        cursorQuery.setParameter("pricePolicyId", cursor);
        cursorQuery.setMaxResults(1);
        List<ProductCatalogReadModelJpaEntity> rows = cursorQuery.getResultList();
        if (FormatValidator.hasNoValue(rows)) {
            return null;
        }
        return extractSortValue(rows.getFirst(), sortColumn);
    }

    private Object extractSortValue(ProductCatalogReadModelJpaEntity entity, String sortColumn) {
        return switch (sortColumn) {
            case "orderNum" -> entity.getOrderNum();
            case "popularity" -> entity.getPopularity();
            case "accumulatedPoint" -> entity.getAccumulatedPoint();
            case "accumulatedPointRate" -> entity.getAccumulatedPointRate();
            case "discountPrice" -> entity.getDiscountPrice();
            default -> 0L;
        };
    }

    private String resolveSortColumn(ProductSortProperty sortProperty) {
        return switch (sortProperty) {
            case ORDER_NUM -> "orderNum";
            case POPULARITY -> "popularity";
            case ACCUMULATED_POINT -> "accumulatedPoint";
            case ACCUMULATED_POINT_RATE -> "accumulatedPointRate";
            case DISCOUNT_PRICE -> "discountPrice";
        };
    }

    private String resolveSearchColumn(ProductSearchTarget searchTarget) {
        return switch (searchTarget) {
            case NAME -> "name";
            case BRAND_NAME -> "brandName";
        };
    }
}
