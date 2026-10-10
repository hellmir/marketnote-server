package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonGoodsJpaRepository;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import com.personal.marketnote.reward.port.out.gifticon.EvictGifticonGoodsCachePort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.SaveGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.personal.marketnote.common.utility.ApiConstant.FIRST_PAGE_CURSOR_VALUE;

@PersistenceAdapter
@RequiredArgsConstructor
public class GifticonGoodsPersistenceAdapter implements FindGifticonGoodsPort, SaveGifticonGoodsPort, UpdateGifticonGoodsPort, EvictGifticonGoodsCachePort {

    private final GifticonGoodsJpaRepository repository;

    @Override
    public Optional<GifticonGoods> findByGoodsCode(String goodsCode) {
        return repository.findByGoodsCode(goodsCode)
                .map(GifticonGoodsJpaEntity::toDomain);
    }

    @Override
    public List<GifticonGoods> findAllByGoodsStatus(GoodsStatus goodsStatus) {
        return repository.findAllByGoodsStatus(goodsStatus).stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void save(GifticonGoods goods) {
        repository.save(GifticonGoodsJpaEntity.from(goods));
    }

    @Override
    public void update(GifticonGoods goods) {
        GifticonGoodsJpaEntity entity = repository.findById(goods.getId())
                .orElseThrow(() -> new GifticonGoodsNotFoundException(goods.getGoodsCode().getValue()));
        entity.updateFrom(goods);
    }

    @Override
    public List<GifticonGoodsBrandProjection> findDistinctBrandsByCategoryCode(String categoryCode) {
        return repository.findDistinctBrandsByCategoryCode(categoryCode, GoodsStatus.SALE).stream()
                .map(row -> new GifticonGoodsBrandProjection(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2]
                ))
                .toList();
    }

    @Override
    public FindAllForAdminResult findAllForAdmin(
            int page, int pageSize, GoodsStatus goodsStatus, Boolean exposed, String keyword
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        Page<GifticonGoodsJpaEntity> pageResult = fetchAdminGoodsPage(goodsStatus, exposed, keyword, pageRequest);
        List<GifticonGoods> items = pageResult.getContent().stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
        return new FindAllForAdminResult(items, pageResult.getTotalElements());
    }

    private Page<GifticonGoodsJpaEntity> fetchAdminGoodsPage(
            GoodsStatus goodsStatus, Boolean exposed, String keyword, PageRequest pageRequest
    ) {
        if (FormatValidator.hasNoValue(goodsStatus)) {
            return repository.findAllForAdminWithoutStatus(exposed, keyword, pageRequest);
        }
        return repository.findAllForAdminWithStatus(goodsStatus, exposed, keyword, pageRequest);
    }

    @Override
    public List<GifticonGoods> findAllExposedByCursor(String categoryCode, String brandCode, Long cursor, int limit) {
        String categoryCodeParam = normalizeFilter(categoryCode);
        String brandCodeParam = normalizeFilter(brandCode);
        Pageable pageable = PageRequest.of(0, limit);

        List<GifticonGoodsJpaEntity> entities = fetchExposed(
                categoryCodeParam, brandCodeParam, cursor, pageable
        );
        return entities.stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
    }

    private List<GifticonGoodsJpaEntity> fetchExposed(
            String categoryCodeParam, String brandCodeParam, Long cursor, Pageable pageable
    ) {
        if (FormatValidator.equals(cursor, FIRST_PAGE_CURSOR_VALUE)) {
            return repository.findFirstPageExposed(
                    categoryCodeParam, brandCodeParam, GoodsStatus.SALE, pageable
            );
        }
        Optional<GifticonGoodsJpaEntity> anchor = repository.findById(cursor);
        if (anchor.isEmpty()) {
            return repository.findFirstPageExposed(
                    categoryCodeParam, brandCodeParam, GoodsStatus.SALE, pageable
            );
        }
        Integer anchorOrderNum = anchor.get().getOrderNum();
        if (FormatValidator.hasValue(anchorOrderNum)) {
            return repository.findExposedAfterNonNullAnchor(
                    categoryCodeParam, brandCodeParam, GoodsStatus.SALE, anchorOrderNum, cursor, pageable
            );
        }
        return repository.findExposedAfterNullAnchor(
                categoryCodeParam, brandCodeParam, GoodsStatus.SALE, cursor, pageable
        );
    }

    @Override
    public long countAllExposed(String categoryCode, String brandCode) {
        return repository.countAllExposed(normalizeFilter(categoryCode), normalizeFilter(brandCode), GoodsStatus.SALE);
    }

    private String normalizeFilter(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            return "";
        }
        return value;
    }

    @Override
    public List<GifticonGoods> findAllPopularAndExposed(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return repository.findAllPopularAndExposed(GoodsStatus.SALE, pageable).stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
    }

    @Override
    @CacheEvict(value = "gifticon:goods:featured", allEntries = true)
    public void evictFeaturedGoodsCache() {
        // 캐시 evict만 수행
    }
}
