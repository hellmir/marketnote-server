package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.QGifticonGoodsJpaEntity;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class GifticonGoodsQueryRepositoryImpl implements GifticonGoodsQueryRepository {

    private static final QGifticonGoodsJpaEntity goods = QGifticonGoodsJpaEntity.gifticonGoodsJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Object[]> findDistinctBrandsByCategoryCode(String categoryCode, GoodsStatus saleStatus) {
        List<com.querydsl.core.Tuple> rows = queryFactory
                .select(goods.brandCode, goods.brandName, goods.brandImageUrl)
                .from(goods)
                .where(
                        goods.exposed.isTrue(),
                        goods.goodsStatus.eq(saleStatus),
                        goods.categoryCode.eq(categoryCode)
                )
                .distinct()
                .orderBy(goods.brandName.asc())
                .fetch();
        return rows.stream()
                .map(tuple -> new Object[]{
                        tuple.get(goods.brandCode),
                        tuple.get(goods.brandName),
                        tuple.get(goods.brandImageUrl)
                })
                .toList();
    }

    @Override
    public List<GifticonGoodsJpaEntity> findExposedByAnchor(
            String categoryCode,
            String brandCode,
            GoodsStatus saleStatus,
            Integer anchorOrderNum,
            Long anchorId,
            Pageable pageable
    ) {
        return queryFactory
                .selectFrom(goods)
                .where(
                        goods.exposed.isTrue(),
                        goods.goodsStatus.eq(saleStatus),
                        categoryCodeEq(categoryCode),
                        brandCodeEq(brandCode),
                        anchorCondition(anchorOrderNum, anchorId)
                )
                .orderBy(exposedOrderSpecifiers(anchorOrderNum, anchorId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long countAllExposed(String categoryCode, String brandCode, GoodsStatus saleStatus) {
        Long count = queryFactory
                .select(goods.count())
                .from(goods)
                .where(
                        goods.exposed.isTrue(),
                        goods.goodsStatus.eq(saleStatus),
                        categoryCodeEq(categoryCode),
                        brandCodeEq(brandCode)
                )
                .fetchOne();
        if (FormatValidator.hasNoValue(count)) {
            return 0L;
        }
        return count;
    }

    @Override
    public Page<GifticonGoodsJpaEntity> findAllForAdmin(
            GoodsStatus goodsStatus,
            Boolean exposed,
            String keyword,
            Pageable pageable
    ) {
        List<GifticonGoodsJpaEntity> content = queryFactory
                .selectFrom(goods)
                .where(
                        goodsStatusEq(goodsStatus),
                        exposedEq(exposed),
                        goodsNameLike(keyword)
                )
                .orderBy(goods.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = queryFactory
                .select(goods.count())
                .from(goods)
                .where(
                        goodsStatusEq(goodsStatus),
                        exposedEq(exposed),
                        goodsNameLike(keyword)
                )
                .fetchOne();
        long totalCount = FormatValidator.hasNoValue(total) ? 0L : total;
        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public List<GifticonGoodsJpaEntity> findAllPopularAndExposed(GoodsStatus saleStatus, Pageable pageable) {
        return queryFactory
                .selectFrom(goods)
                .where(
                        goods.popular.isTrue(),
                        goods.exposed.isTrue(),
                        goods.goodsStatus.eq(saleStatus)
                )
                .orderBy(orderNumNullsLast().asc(), goods.orderNum.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression categoryCodeEq(String categoryCode) {
        if (FormatValidator.hasNoValue(categoryCode) || categoryCode.isBlank()) {
            return null;
        }
        return goods.categoryCode.eq(categoryCode);
    }

    private BooleanExpression brandCodeEq(String brandCode) {
        if (FormatValidator.hasNoValue(brandCode) || brandCode.isBlank()) {
            return null;
        }
        return goods.brandCode.eq(brandCode);
    }

    private BooleanExpression goodsStatusEq(GoodsStatus goodsStatus) {
        if (FormatValidator.hasNoValue(goodsStatus)) {
            return null;
        }
        return goods.goodsStatus.eq(goodsStatus);
    }

    private BooleanExpression exposedEq(Boolean exposed) {
        if (FormatValidator.hasNoValue(exposed)) {
            return null;
        }
        return goods.exposed.eq(exposed);
    }

    private BooleanExpression goodsNameLike(String keyword) {
        if (FormatValidator.hasNoValue(keyword) || keyword.isBlank()) {
            return null;
        }
        return goods.goodsName.like("%" + keyword + "%");
    }

    private BooleanExpression anchorCondition(Integer anchorOrderNum, Long anchorId) {
        if (FormatValidator.hasNoValue(anchorOrderNum) && FormatValidator.hasNoValue(anchorId)) {
            return null;
        }
        if (FormatValidator.hasValue(anchorOrderNum)) {
            return goods.orderNum.isNull()
                    .or(goods.orderNum.gt(anchorOrderNum))
                    .or(goods.orderNum.eq(anchorOrderNum).and(goods.id.gt(anchorId)));
        }
        return goods.orderNum.isNull().and(goods.id.gt(anchorId));
    }

    private OrderSpecifier<?>[] exposedOrderSpecifiers(Integer anchorOrderNum, Long anchorId) {
        if (FormatValidator.hasNoValue(anchorOrderNum) && FormatValidator.hasValue(anchorId)) {
            return new OrderSpecifier<?>[]{goods.id.asc()};
        }
        return new OrderSpecifier<?>[]{
                orderNumNullsLast().asc(),
                goods.orderNum.asc(),
                goods.id.asc()
        };
    }

    private NumberExpression<Integer> orderNumNullsLast() {
        return new CaseBuilder()
                .when(goods.orderNum.isNull()).then(1)
                .otherwise(0);
    }
}
