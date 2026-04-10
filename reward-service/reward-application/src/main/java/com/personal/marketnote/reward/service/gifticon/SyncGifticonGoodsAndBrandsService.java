package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.gifticon.*;
import com.personal.marketnote.reward.port.in.usecase.gifticon.SyncGifticonGoodsAndBrandsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.*;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonBrandPort.FetchGifticonBrandResult;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonBrandPort.FetchedGifticonBrandItem;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort.FetchGifticonGoodsResult;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort.FetchedGifticonGoodsItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SyncGifticonGoodsAndBrandsService implements SyncGifticonGoodsAndBrandsUseCase {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_PAGES = 500;

    private final TransactionTemplate transactionTemplate;
    private final FetchGifticonBrandPort fetchGifticonBrandPort;
    private final FetchGifticonGoodsPort fetchGifticonGoodsPort;
    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final SaveGifticonGoodsPort saveGifticonGoodsPort;
    private final UpdateGifticonGoodsPort updateGifticonGoodsPort;
    private final FindGifticonBrandPort findGifticonBrandPort;
    private final SaveGifticonBrandPort saveGifticonBrandPort;
    private final UpdateGifticonBrandPort updateGifticonBrandPort;
    private final FindGifticonCategoryPort findGifticonCategoryPort;
    private final SaveGifticonCategoryPort saveGifticonCategoryPort;
    private final UpdateGifticonCategoryPort updateGifticonCategoryPort;
    private final FindGifticonCategoryMappingPort findGifticonCategoryMappingPort;
    private final SaveGifticonCategoryMappingPort saveGifticonCategoryMappingPort;

    @Override
    public void syncAll() {
        FetchGifticonBrandResult brandResult = fetchGifticonBrandPort.fetchBrandList();
        List<FetchedGifticonGoodsItem> allGoodsItems = fetchAllGoodsPages();

        transactionTemplate.execute(status -> {
            syncBrandsAndCategories(brandResult.items());
            Set<String> syncedGoodsCodes = syncGoods(allGoodsItems);
            suspendMissingGoods(syncedGoodsCodes);
            return null;
        });
    }

    private List<FetchedGifticonGoodsItem> fetchAllGoodsPages() {
        List<FetchedGifticonGoodsItem> allItems = new ArrayList<>();
        int start = 1;
        int pageCount = 0;

        while (pageCount < MAX_PAGES) {
            FetchGifticonGoodsResult page = fetchGifticonGoodsPort.fetchProductList(start, PAGE_SIZE);
            allItems.addAll(page.items());
            pageCount++;

            if (allItems.size() >= page.totalCount() || page.items().isEmpty()) {
                break;
            }
            start += PAGE_SIZE;
        }

        return allItems;
    }

    private void syncBrandsAndCategories(List<FetchedGifticonBrandItem> brandItems) {
        for (FetchedGifticonBrandItem item : brandItems) {
            syncBrand(item);
            syncCategoryFromBrand(item);
        }
    }

    private void syncBrand(FetchedGifticonBrandItem item) {
        Optional<GifticonBrand> existing = findGifticonBrandPort.findByBrandCode(item.brandCode());

        if (existing.isPresent()) {
            GifticonBrand brand = existing.get();
            brand.syncFromApi(item.brandName(), item.brandIconImg());
            updateGifticonBrandPort.update(brand);
            return;
        }

        GifticonBrand brand = GifticonBrand.from(GifticonBrandCreateState.builder()
                .brandCode(item.brandCode())
                .brandName(item.brandName())
                .brandImageUrl(item.brandIconImg())
                .build());
        saveGifticonBrandPort.save(brand);
    }

    private void syncCategoryFromBrand(FetchedGifticonBrandItem item) {
        Optional<Long> existingCategoryId = findGifticonCategoryMappingPort
                .findCategoryIdByGiftishowCategorySeq(item.category1Seq());

        if (existingCategoryId.isPresent()) {
            updateExistingCategory(existingCategoryId.get(), item.category1Name());
            return;
        }

        createCategoryAndMapping(item.category1Seq(), item.category1Name());
    }

    private void updateExistingCategory(Long categoryId, String categoryName) {
        Optional<GifticonCategory> category = findGifticonCategoryPort.findById(categoryId);

        if (category.isEmpty()) {
            return;
        }

        GifticonCategory existing = category.get();
        existing.syncFromApi(categoryName);
        updateGifticonCategoryPort.update(existing);
    }

    private void createCategoryAndMapping(String category1Seq, String category1Name) {
        Optional<GifticonCategory> existing = findGifticonCategoryPort.findByCategoryCode(category1Seq);

        if (existing.isPresent()) {
            saveGifticonCategoryMappingPort.save(category1Seq, existing.get().getId());
            return;
        }

        GifticonCategory category = GifticonCategory.from(GifticonCategoryCreateState.builder()
                .categoryCode(category1Seq)
                .categoryName(category1Name)
                .build());
        GifticonCategory savedCategory = saveGifticonCategoryPort.save(category);
        saveGifticonCategoryMappingPort.save(category1Seq, savedCategory.getId());
    }

    private Set<String> syncGoods(List<FetchedGifticonGoodsItem> goodsItems) {
        Set<String> syncedGoodsCodes = new HashSet<>();

        for (FetchedGifticonGoodsItem item : goodsItems) {
            syncSingleGoods(item);
            syncedGoodsCodes.add(item.goodsCode());
        }

        return syncedGoodsCodes;
    }

    private void syncSingleGoods(FetchedGifticonGoodsItem item) {
        Optional<GifticonGoods> existing = findGifticonGoodsPort.findByGoodsCode(item.goodsCode());

        if (existing.isPresent()) {
            GifticonGoods goods = existing.get();
            goods.syncFromApi(GifticonGoodsSyncState.builder()
                    .goodsName(item.goodsName())
                    .brandCode(item.brandCode())
                    .brandName(item.brandName())
                    .brandImageUrl(item.brandIconImg())
                    .categoryCode(item.category1Seq())
                    .realPrice(item.realPrice())
                    .salePrice(item.salePrice())
                    .imageUrl(item.goodsImgB())
                    .description(item.content())
                    .validDays(item.limitDay())
                    .goodsStatus(item.goodsStatus())
                    .build());
            updateGifticonGoodsPort.update(goods);
            return;
        }

        GifticonGoods goods = GifticonGoods.from(GifticonGoodsCreateState.builder()
                .goodsCode(item.goodsCode())
                .goodsName(item.goodsName())
                .brandCode(item.brandCode())
                .brandName(item.brandName())
                .brandImageUrl(item.brandIconImg())
                .categoryCode(item.category1Seq())
                .realPrice(item.realPrice())
                .salePrice(item.salePrice())
                .cashPrice(item.salePrice())
                .imageUrl(item.goodsImgB())
                .description(item.content())
                .validDays(item.limitDay())
                .goodsStatus(item.goodsStatus())
                .build());
        saveGifticonGoodsPort.save(goods);
    }

    private void suspendMissingGoods(Set<String> syncedGoodsCodes) {
        List<GifticonGoods> saleGoods = findGifticonGoodsPort.findAllByGoodsStatus("SALE");

        for (GifticonGoods goods : saleGoods) {
            if (syncedGoodsCodes.contains(goods.getGoodsCode())) {
                continue;
            }
            goods.suspend();
            updateGifticonGoodsPort.update(goods);
        }
    }
}
