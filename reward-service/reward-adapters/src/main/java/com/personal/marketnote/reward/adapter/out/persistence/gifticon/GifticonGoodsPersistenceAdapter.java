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
                .orElseThrow(() -> new GifticonGoodsNotFoundException(goods.getGoodsCode()));
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
    public FindAllForAdminResult findAllForAdmin(int page, int pageSize, GoodsStatus goodsStatus, Boolean exposed, String keyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        Page<GifticonGoodsJpaEntity> pageResult = fetchAdminGoodsPage(goodsStatus, exposed, keyword, pageRequest);
        List<GifticonGoods> items = pageResult.getContent().stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
        return new FindAllForAdminResult(items, pageResult.getTotalElements());
    }

    private Page<GifticonGoodsJpaEntity> fetchAdminGoodsPage(GoodsStatus goodsStatus, Boolean exposed, String keyword, PageRequest pageRequest) {
        if (FormatValidator.hasNoValue(goodsStatus)) {
            return repository.findAllForAdminWithoutStatus(exposed, keyword, pageRequest);
        }
        return repository.findAllForAdminWithStatus(goodsStatus, exposed, keyword, pageRequest);
    }

    @Override
    public List<GifticonGoods> findAllExposed(String categoryCode, String brandCode, int page, int pageSize) {
        String categoryCodeParam = FormatValidator.hasValue(categoryCode) ? categoryCode : "";
        String brandCodeParam = FormatValidator.hasValue(brandCode) ? brandCode : "";
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<GifticonGoodsJpaEntity> result = repository.findAllExposed(categoryCodeParam, brandCodeParam, GoodsStatus.SALE, pageable);
        return result.getContent().stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countAllExposed(String categoryCode, String brandCode) {
        String categoryCodeParam = FormatValidator.hasValue(categoryCode) ? categoryCode : "";
        String brandCodeParam = FormatValidator.hasValue(brandCode) ? brandCode : "";
        Pageable pageable = PageRequest.of(0, 1);
        Page<GifticonGoodsJpaEntity> result = repository.findAllExposed(categoryCodeParam, brandCodeParam, GoodsStatus.SALE, pageable);
        return result.getTotalElements();
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
