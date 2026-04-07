package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonGoodsJpaRepository;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.SaveGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class GifticonGoodsPersistenceAdapter implements FindGifticonGoodsPort, SaveGifticonGoodsPort, UpdateGifticonGoodsPort {

    private final GifticonGoodsJpaRepository repository;

    @Override
    public Optional<GifticonGoods> findByGoodsCode(String goodsCode) {
        return repository.findByGoodsCode(goodsCode)
                .map(GifticonGoodsJpaEntity::toDomain);
    }

    @Override
    public List<GifticonGoods> findAllByGoodsStatus(String goodsStatus) {
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
        return repository.findDistinctBrandsByCategoryCode(categoryCode).stream()
                .map(row -> new GifticonGoodsBrandProjection(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2]
                ))
                .toList();
    }

    @Override
    public FindAllForAdminResult findAllForAdmin(int page, int pageSize, String goodsStatus, Boolean exposed, String keyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize);
        Page<GifticonGoodsJpaEntity> pageResult = repository.findAllForAdmin(goodsStatus, exposed, keyword, pageRequest);
        List<GifticonGoods> items = pageResult.getContent().stream()
                .map(GifticonGoodsJpaEntity::toDomain)
                .toList();
        return new FindAllForAdminResult(items, pageResult.getTotalElements());
    }
}
