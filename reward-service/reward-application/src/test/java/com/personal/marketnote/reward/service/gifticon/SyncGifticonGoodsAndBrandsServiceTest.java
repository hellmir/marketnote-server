package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.gifticon.*;
import com.personal.marketnote.reward.port.out.gifticon.*;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonBrandPort.FetchGifticonBrandResult;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonBrandPort.FetchedGifticonBrandItem;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort.FetchGifticonGoodsResult;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort.FetchedGifticonGoodsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncGifticonGoodsAndBrandsServiceTest {

    @InjectMocks
    private SyncGifticonGoodsAndBrandsService syncGifticonGoodsAndBrandsService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private FetchGifticonBrandPort fetchGifticonBrandPort;

    @Mock
    private FetchGifticonGoodsPort fetchGifticonGoodsPort;

    @Mock
    private FindGifticonGoodsPort findGifticonGoodsPort;

    @Mock
    private SaveGifticonGoodsPort saveGifticonGoodsPort;

    @Mock
    private UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Mock
    private FindGifticonBrandPort findGifticonBrandPort;

    @Mock
    private SaveGifticonBrandPort saveGifticonBrandPort;

    @Mock
    private UpdateGifticonBrandPort updateGifticonBrandPort;

    @Mock
    private FindGifticonCategoryPort findGifticonCategoryPort;

    @Mock
    private SaveGifticonCategoryPort saveGifticonCategoryPort;

    @Mock
    private UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Mock
    private FindGifticonCategoryMappingPort findGifticonCategoryMappingPort;

    @Mock
    private SaveGifticonCategoryMappingPort saveGifticonCategoryMappingPort;

    @BeforeEach
    void setUp() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    @DisplayName("신규 브랜드를 동기화하면 브랜드가 저장된다")
    void shouldSaveNewBrand() {
        // given
        FetchedGifticonBrandItem brandItem = new FetchedGifticonBrandItem(
                "BR001", "스타벅스", "https://img.com/starbucks.png", "1", "커피"
        );
        when(fetchGifticonBrandPort.fetchBrandList())
                .thenReturn(new FetchGifticonBrandResult(1, List.of(brandItem)));
        when(findGifticonBrandPort.findByBrandCode("BR001"))
                .thenReturn(Optional.empty());
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(0, List.of()));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of());
        when(findGifticonCategoryMappingPort.findCategoryIdByGiftishowCategorySeq("1"))
                .thenReturn(Optional.empty());
        when(findGifticonCategoryPort.findByCategoryCode("1"))
                .thenReturn(Optional.empty());
        GifticonCategory savedCategory = createCategory(1L, "1", "커피");
        when(saveGifticonCategoryPort.save(any(GifticonCategory.class)))
                .thenReturn(savedCategory);

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        ArgumentCaptor<GifticonBrand> brandCaptor = ArgumentCaptor.forClass(GifticonBrand.class);
        verify(saveGifticonBrandPort).save(brandCaptor.capture());
        GifticonBrand savedBrand = brandCaptor.getValue();
        assertThat(savedBrand.getBrandCode()).isEqualTo("BR001");
        assertThat(savedBrand.getBrandName()).isEqualTo("스타벅스");
        assertThat(savedBrand.getBrandImageUrl()).isEqualTo("https://img.com/starbucks.png");
    }

    @Test
    @DisplayName("기존 브랜드가 존재하면 브랜드 정보를 갱신한다")
    void shouldUpdateExistingBrand() {
        // given
        GifticonBrand existingBrand = createBrand(1L, "BR001", "스타벅스(구)", "https://img.com/old.png");
        FetchedGifticonBrandItem brandItem = new FetchedGifticonBrandItem(
                "BR001", "스타벅스", "https://img.com/new.png", "1", "커피"
        );
        when(fetchGifticonBrandPort.fetchBrandList())
                .thenReturn(new FetchGifticonBrandResult(1, List.of(brandItem)));
        when(findGifticonBrandPort.findByBrandCode("BR001"))
                .thenReturn(Optional.of(existingBrand));
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(0, List.of()));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of());
        when(findGifticonCategoryMappingPort.findCategoryIdByGiftishowCategorySeq("1"))
                .thenReturn(Optional.of(1L));
        when(findGifticonCategoryPort.findById(1L))
                .thenReturn(Optional.of(createCategory(1L, "1", "커피")));

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        verify(updateGifticonBrandPort).update(existingBrand);
        assertThat(existingBrand.getBrandName()).isEqualTo("스타벅스");
        assertThat(existingBrand.getBrandImageUrl()).isEqualTo("https://img.com/new.png");
        verify(saveGifticonBrandPort, never()).save(any());
    }

    @Test
    @DisplayName("신규 카테고리를 생성하고 매핑을 저장한다")
    void shouldCreateNewCategoryAndMapping() {
        // given
        FetchedGifticonBrandItem brandItem = new FetchedGifticonBrandItem(
                "BR001", "스타벅스", "https://img.com/sb.png", "1", "커피"
        );
        when(fetchGifticonBrandPort.fetchBrandList())
                .thenReturn(new FetchGifticonBrandResult(1, List.of(brandItem)));
        when(findGifticonBrandPort.findByBrandCode("BR001"))
                .thenReturn(Optional.empty());
        when(findGifticonCategoryMappingPort.findCategoryIdByGiftishowCategorySeq("1"))
                .thenReturn(Optional.empty());
        when(findGifticonCategoryPort.findByCategoryCode("1"))
                .thenReturn(Optional.empty());
        GifticonCategory savedCategory = createCategory(10L, "1", "커피");
        when(saveGifticonCategoryPort.save(any(GifticonCategory.class)))
                .thenReturn(savedCategory);
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(0, List.of()));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of());

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        ArgumentCaptor<GifticonCategory> categoryCaptor = ArgumentCaptor.forClass(GifticonCategory.class);
        verify(saveGifticonCategoryPort).save(categoryCaptor.capture());
        GifticonCategory created = categoryCaptor.getValue();
        assertThat(created.getCategoryCode()).isEqualTo("1");
        assertThat(created.getCategoryName()).isEqualTo("커피");
        assertThat(created.isExposed()).isFalse();

        verify(saveGifticonCategoryMappingPort).save("1", 10L);
    }

    @Test
    @DisplayName("기존 카테고리 매핑이 존재하면 카테고리 이름을 갱신한다")
    void shouldUpdateExistingCategoryName() {
        // given
        GifticonCategory existingCategory = createCategory(10L, "1", "커피(구)");
        FetchedGifticonBrandItem brandItem = new FetchedGifticonBrandItem(
                "BR001", "스타벅스", "https://img.com/sb.png", "1", "커피"
        );
        when(fetchGifticonBrandPort.fetchBrandList())
                .thenReturn(new FetchGifticonBrandResult(1, List.of(brandItem)));
        when(findGifticonBrandPort.findByBrandCode("BR001"))
                .thenReturn(Optional.empty());
        when(findGifticonCategoryMappingPort.findCategoryIdByGiftishowCategorySeq("1"))
                .thenReturn(Optional.of(10L));
        when(findGifticonCategoryPort.findById(10L))
                .thenReturn(Optional.of(existingCategory));
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(0, List.of()));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of());

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        assertThat(existingCategory.getCategoryName()).isEqualTo("커피");
        verify(updateGifticonCategoryPort).update(existingCategory);
        verify(saveGifticonCategoryPort, never()).save(any());
        verify(saveGifticonCategoryMappingPort, never()).save(anyString(), anyLong());
    }

    @Test
    @DisplayName("신규 상품을 동기화하면 exposed=false, cashPrice=salePrice로 저장된다")
    void shouldSaveNewGoodsWithDefaultValues() {
        // given
        stubEmptyBrandSync();
        FetchedGifticonGoodsItem goodsItem = createGoodsItem(
                "GD001", "아메리카노", "BR001", "스타벅스", "https://img.com/sb.png",
                "1", 5000L, 4500L, 30, "맛있는 커피", "SALE"
        );
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(1, List.of(goodsItem)));
        when(findGifticonGoodsPort.findByGoodsCode("GD001"))
                .thenReturn(Optional.empty());
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of());

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        ArgumentCaptor<GifticonGoods> goodsCaptor = ArgumentCaptor.forClass(GifticonGoods.class);
        verify(saveGifticonGoodsPort).save(goodsCaptor.capture());
        GifticonGoods savedGoods = goodsCaptor.getValue();
        assertThat(savedGoods.getGoodsCode()).isEqualTo("GD001");
        assertThat(savedGoods.getGoodsName()).isEqualTo("아메리카노");
        assertThat(savedGoods.getCashPrice()).isEqualTo(4500L);
        assertThat(savedGoods.getSalePrice()).isEqualTo(4500L);
        assertThat(savedGoods.isExposed()).isFalse();
        assertThat(savedGoods.getOrderNum()).isNull();
        assertThat(savedGoods.getGoodsStatus()).isEqualTo("SALE");
    }

    @Test
    @DisplayName("기존 상품을 갱신할 때 cashPrice는 변경하지 않는다")
    void shouldNotChangeCashPriceOnUpdate() {
        // given
        stubEmptyBrandSync();
        GifticonGoods existingGoods = createGoods(1L, "GD001", "아메리카노(구)", 4500L, 3500L, "SALE");
        FetchedGifticonGoodsItem goodsItem = createGoodsItem(
                "GD001", "아메리카노", "BR001", "스타벅스", "https://img.com/sb.png",
                "1", 5000L, 4800L, 30, "맛있는 커피", "SALE"
        );
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(1, List.of(goodsItem)));
        when(findGifticonGoodsPort.findByGoodsCode("GD001"))
                .thenReturn(Optional.of(existingGoods));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of(existingGoods));

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        verify(updateGifticonGoodsPort).update(existingGoods);
        assertThat(existingGoods.getGoodsName()).isEqualTo("아메리카노");
        assertThat(existingGoods.getSalePrice()).isEqualTo(4800L);
        assertThat(existingGoods.getCashPrice()).isEqualTo(3500L);
        verify(saveGifticonGoodsPort, never()).save(any());
    }

    @Test
    @DisplayName("API에서 사라진 SALE 상품은 SUS 상태로 전환된다")
    void shouldSuspendMissingGoods() {
        // given
        stubEmptyBrandSync();
        GifticonGoods missingGoods = createGoods(2L, "GD002", "빙수", 4500L, 4500L, "SALE");
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(0, List.of()));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of(missingGoods));

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        verify(updateGifticonGoodsPort).update(missingGoods);
        assertThat(missingGoods.getGoodsStatus()).isEqualTo("SUS");
    }

    @Test
    @DisplayName("API에 여전히 존재하는 SALE 상품은 SUS 처리되지 않는다")
    void shouldNotSuspendGoodsStillInApi() {
        // given
        stubEmptyBrandSync();
        GifticonGoods existingGoods = createGoods(1L, "GD001", "아메리카노", 4500L, 4500L, "SALE");
        FetchedGifticonGoodsItem goodsItem = createGoodsItem(
                "GD001", "아메리카노", "BR001", "스타벅스", "https://img.com/sb.png",
                "1", 5000L, 4500L, 30, "맛있는 커피", "SALE"
        );
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(1, List.of(goodsItem)));
        when(findGifticonGoodsPort.findByGoodsCode("GD001"))
                .thenReturn(Optional.of(existingGoods));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of(existingGoods));

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        assertThat(existingGoods.getGoodsStatus()).isEqualTo("SALE");
    }

    @Test
    @DisplayName("2페이지 이상의 상품을 순차적으로 수집한다")
    void shouldFetchMultiplePagesSequentially() {
        // given
        stubEmptyBrandSync();
        FetchedGifticonGoodsItem item1 = createGoodsItem(
                "GD001", "아메리카노", "BR001", "스타벅스", "https://img.com/sb.png",
                "1", 5000L, 4500L, 30, "커피", "SALE"
        );
        FetchedGifticonGoodsItem item2 = createGoodsItem(
                "GD002", "라떼", "BR001", "스타벅스", "https://img.com/sb.png",
                "1", 6000L, 5500L, 30, "라떼", "SALE"
        );
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(2, List.of(item1)));
        when(fetchGifticonGoodsPort.fetchProductList(21, 20))
                .thenReturn(new FetchGifticonGoodsResult(2, List.of(item2)));
        when(findGifticonGoodsPort.findByGoodsCode("GD001")).thenReturn(Optional.empty());
        when(findGifticonGoodsPort.findByGoodsCode("GD002")).thenReturn(Optional.empty());
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE")).thenReturn(List.of());

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        verify(fetchGifticonGoodsPort).fetchProductList(1, 20);
        verify(fetchGifticonGoodsPort).fetchProductList(21, 20);
        verify(saveGifticonGoodsPort, times(2)).save(any(GifticonGoods.class));
    }

    @Test
    @DisplayName("비SALE 상태 상품은 suspend 대상에서 제외된다")
    void shouldNotSuspendNonSaleGoods() {
        // given
        stubEmptyBrandSync();
        GifticonGoods susGoods = createGoods(3L, "GD003", "만료상품", 3000L, 3000L, "SUS");
        when(fetchGifticonGoodsPort.fetchProductList(1, 20))
                .thenReturn(new FetchGifticonGoodsResult(0, List.of()));
        when(findGifticonGoodsPort.findAllByGoodsStatus("SALE"))
                .thenReturn(List.of());

        // when
        syncGifticonGoodsAndBrandsService.syncAll();

        // then
        assertThat(susGoods.getGoodsStatus()).isEqualTo("SUS");
        verify(updateGifticonGoodsPort, never()).update(susGoods);
    }

    // --- Helper Methods ---

    private void stubEmptyBrandSync() {
        when(fetchGifticonBrandPort.fetchBrandList())
                .thenReturn(new FetchGifticonBrandResult(0, List.of()));
    }

    private GifticonBrand createBrand(Long id, String brandCode, String brandName, String brandImageUrl) {
        return GifticonBrand.from(GifticonBrandSnapshotState.builder()
                .id(id)
                .brandCode(brandCode)
                .brandName(brandName)
                .brandImageUrl(brandImageUrl)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private GifticonCategory createCategory(Long id, String categoryCode, String categoryName) {
        return GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .exposed(false)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private GifticonGoods createGoods(Long id, String goodsCode, String goodsName,
                                      Long salePrice, Long cashPrice, String goodsStatus) {
        return GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(id)
                .goodsCode(goodsCode)
                .goodsName(goodsName)
                .brandCode("BR001")
                .brandName("스타벅스")
                .brandImageUrl("https://img.com/sb.png")
                .categoryCode("1")
                .realPrice(5000L)
                .salePrice(salePrice)
                .cashPrice(cashPrice)
                .imageUrl("https://img.com/goods.png")
                .description("설명")
                .validDays(30)
                .goodsStatus(goodsStatus)
                .exposed(false)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private FetchedGifticonGoodsItem createGoodsItem(String goodsCode, String goodsName,
                                                      String brandCode, String brandName, String brandIconImg,
                                                      String category1Seq, long realPrice, long salePrice,
                                                      int limitDay, String content, String goodsStatus) {
        return new FetchedGifticonGoodsItem(
                goodsCode, goodsName, "https://img.com/" + goodsCode + ".png",
                brandCode, brandName, brandIconImg,
                category1Seq, salePrice, realPrice,
                limitDay, content, goodsStatus
        );
    }
}
