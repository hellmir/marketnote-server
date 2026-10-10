package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("GifticonGoodsJpaRepository 테스트")
class GifticonGoodsJpaRepositoryTest {

    @Autowired
    private GifticonGoodsJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private GifticonGoodsJpaEntity persistGoods(
            String goodsCode, String brandCode, String categoryCode,
            GoodsStatus status, boolean exposed, boolean popular, Integer orderNum
    ) {
        GifticonGoods domain = GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .goodsCode(goodsCode)
                .goodsName("상품 " + goodsCode)
                .brandCode(brandCode)
                .brandName("브랜드 " + brandCode)
                .brandImageUrl("http://img.test.com/brand.jpg")
                .categoryCode(categoryCode)
                .realPrice(12_000L)
                .salePrice(10_000L)
                .cashPrice(10_000L)
                .imageUrl("http://img.test.com/goods.jpg")
                .description("설명")
                .validDays(30)
                .goodsStatus(status)
                .exposed(exposed)
                .popular(popular)
                .orderNum(orderNum)
                .build());
        GifticonGoodsJpaEntity entity = GifticonGoodsJpaEntity.from(domain);
        em.persist(entity);
        em.flush();
        return entity;
    }

    @Test
    @DisplayName("findByGoodsCode는 goodsCode로 상품을 조회한다")
    void shouldFindByGoodsCode() {
        persistGoods("G001", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        em.clear();
        Optional<GifticonGoodsJpaEntity> result = repository.findByGoodsCode("G001");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("existsByGoodsCode는 goodsCode 존재 여부를 반환한다")
    void shouldExistsByGoodsCode() {
        persistGoods("G002", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        assertThat(repository.existsByGoodsCode("G002")).isTrue();
        assertThat(repository.existsByGoodsCode("G-NONE")).isFalse();
    }

    @Test
    @DisplayName("findAllByGoodsStatus는 특정 상태의 모든 상품을 반환한다")
    void shouldFindAllByGoodsStatus() {
        persistGoods("G-SALE-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-SALE-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        persistGoods("G-SUS-1", "B001", "C001", GoodsStatus.SUSPENDED, true, false, 3);
        em.clear();

        List<GifticonGoodsJpaEntity> sale = repository.findAllByGoodsStatus(GoodsStatus.SALE);
        assertThat(sale).hasSize(2);
    }

    @Test
    @DisplayName("findDistinctBrandsByCategoryCode는 카테고리별 브랜드를 중복 없이 반환한다")
    void shouldFindDistinctBrands() {
        persistGoods("G-D-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-D-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        persistGoods("G-D-3", "B002", "C001", GoodsStatus.SALE, true, false, 3);
        em.clear();

        List<Object[]> rows = repository.findDistinctBrandsByCategoryCode("C001", GoodsStatus.SALE);
        assertThat(rows).hasSize(2);
    }

    @Test
    @DisplayName("findFirstPageExposed는 노출된 SALE 상품 중 카테고리/브랜드 필터를 적용하고 orderNum/id 순으로 정렬한다")
    void shouldFindFirstPageExposedWithFilters() {
        persistGoods("G-E-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-E-2", "B002", "C001", GoodsStatus.SALE, true, false, 2);
        persistGoods("G-E-3", "B001", "C002", GoodsStatus.SALE, true, false, 3);
        persistGoods("G-E-4", "B001", "C001", GoodsStatus.SALE, false, false, 4);
        em.clear();

        List<GifticonGoodsJpaEntity> all = repository.findFirstPageExposed(
                "", "", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(all).hasSize(3);
        assertThat(all).extracting(GifticonGoodsJpaEntity::getOrderNum).containsExactly(1, 2, 3);

        List<GifticonGoodsJpaEntity> byCategory = repository.findFirstPageExposed(
                "C001", "", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(byCategory).hasSize(2);

        List<GifticonGoodsJpaEntity> byBrand = repository.findFirstPageExposed(
                "", "B001", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(byBrand).hasSize(2);
    }

    @Test
    @DisplayName("findFirstPageExposed는 orderNum IS NULL 상품을 뒤로 정렬한다 (NULLS LAST)")
    void shouldPushNullOrderNumToLastOnFirstPage() {
        persistGoods("G-N-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-N-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        persistGoods("G-N-3", "B001", "C001", GoodsStatus.SALE, true, false, null);
        persistGoods("G-N-4", "B001", "C001", GoodsStatus.SALE, true, false, null);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findFirstPageExposed(
                "", "", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getGoodsCode()).isEqualTo("G-N-1");
        assertThat(result.get(1).getGoodsCode()).isEqualTo("G-N-2");
        assertThat(result.get(2).getOrderNum()).isNull();
        assertThat(result.get(3).getOrderNum()).isNull();
    }

    @Test
    @DisplayName("findFirstPageExposed는 orderNum이 같으면 id 오름차순으로 tie-break한다")
    void shouldTieBreakByIdWhenOrderNumEqual() {
        GifticonGoodsJpaEntity a = persistGoods("G-T-1", "B001", "C001", GoodsStatus.SALE, true, false, 5);
        GifticonGoodsJpaEntity b = persistGoods("G-T-2", "B001", "C001", GoodsStatus.SALE, true, false, 5);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findFirstPageExposed(
                "", "", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(result).extracting(GifticonGoodsJpaEntity::getId)
                .containsExactly(a.getId(), b.getId());
    }

    @Test
    @DisplayName("findFirstPageExposed는 exposed=false 상품을 제외한다")
    void shouldExcludeNonExposed() {
        persistGoods("G-X-1", "B001", "C001", GoodsStatus.SALE, false, false, 1);
        persistGoods("G-X-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findFirstPageExposed(
                "", "", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGoodsCode()).isEqualTo("G-X-2");
    }

    @Test
    @DisplayName("findFirstPageExposed는 SALE이 아닌 상태의 상품을 제외한다")
    void shouldExcludeNonSaleStatus() {
        persistGoods("G-ST-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-ST-2", "B001", "C001", GoodsStatus.SUSPENDED, true, false, 2);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findFirstPageExposed(
                "", "", GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGoodsCode()).isEqualTo("G-ST-1");
    }

    @Test
    @DisplayName("findExposedAfterNonNullAnchor는 cursor orderNum 다음부터 keyset 순으로 반환한다")
    void shouldSliceAfterNonNullAnchor() {
        GifticonGoodsJpaEntity g1 = persistGoods("G-C-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        GifticonGoodsJpaEntity g2 = persistGoods("G-C-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        GifticonGoodsJpaEntity g3 = persistGoods("G-C-3", "B001", "C001", GoodsStatus.SALE, true, false, 3);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findExposedAfterNonNullAnchor(
                "", "", GoodsStatus.SALE, 1, g1.getId(), PageRequest.of(0, 10));
        assertThat(result).extracting(GifticonGoodsJpaEntity::getId)
                .containsExactly(g2.getId(), g3.getId());
    }

    @Test
    @DisplayName("findExposedAfterNonNullAnchor는 같은 orderNum 그룹 내에서 cursor 이후 id만 반환한다")
    void shouldSliceWithinSameOrderNumGroup() {
        GifticonGoodsJpaEntity a = persistGoods("G-S-1", "B001", "C001", GoodsStatus.SALE, true, false, 5);
        GifticonGoodsJpaEntity b = persistGoods("G-S-2", "B001", "C001", GoodsStatus.SALE, true, false, 5);
        GifticonGoodsJpaEntity c = persistGoods("G-S-3", "B001", "C001", GoodsStatus.SALE, true, false, 6);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findExposedAfterNonNullAnchor(
                "", "", GoodsStatus.SALE, 5, a.getId(), PageRequest.of(0, 10));
        assertThat(result).extracting(GifticonGoodsJpaEntity::getId)
                .containsExactly(b.getId(), c.getId());
    }

    @Test
    @DisplayName("findExposedAfterNonNullAnchor는 anchor 다음 orderNum 전체를 포함하며 NULL 그룹도 이어서 반환한다")
    void shouldIncludeNullGroupAfterNonNullAnchor() {
        GifticonGoodsJpaEntity g1 = persistGoods("G-NA-1", "B001", "C001", GoodsStatus.SALE, true, false, 10);
        GifticonGoodsJpaEntity g2 = persistGoods("G-NA-2", "B001", "C001", GoodsStatus.SALE, true, false, 11);
        GifticonGoodsJpaEntity g3 = persistGoods("G-NA-3", "B001", "C001", GoodsStatus.SALE, true, false, null);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findExposedAfterNonNullAnchor(
                "", "", GoodsStatus.SALE, 10, g1.getId(), PageRequest.of(0, 10));
        assertThat(result).extracting(GifticonGoodsJpaEntity::getId)
                .containsExactly(g2.getId(), g3.getId());
    }

    @Test
    @DisplayName("findExposedAfterNullAnchor는 orderNum IS NULL 그룹 내 cursor 이후 아이템만 반환한다")
    void shouldSliceAmongNullOrderNumGroup() {
        persistGoods("G-NX-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        GifticonGoodsJpaEntity n1 = persistGoods("G-NX-2", "B001", "C001", GoodsStatus.SALE, true, false, null);
        GifticonGoodsJpaEntity n2 = persistGoods("G-NX-3", "B001", "C001", GoodsStatus.SALE, true, false, null);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findExposedAfterNullAnchor(
                "", "", GoodsStatus.SALE, n1.getId(), PageRequest.of(0, 10));
        assertThat(result).extracting(GifticonGoodsJpaEntity::getId)
                .containsExactly(n2.getId());
    }

    @Test
    @DisplayName("findExposedAfterNullAnchor는 non-null orderNum 상품을 반환하지 않는다")
    void shouldNotReturnNonNullOrderNumInNullAnchorQuery() {
        persistGoods("G-NN-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-NN-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        GifticonGoodsJpaEntity n1 = persistGoods("G-NN-3", "B001", "C001", GoodsStatus.SALE, true, false, null);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findExposedAfterNullAnchor(
                "", "", GoodsStatus.SALE, n1.getId() - 1, PageRequest.of(0, 10));
        assertThat(result).extracting(GifticonGoodsJpaEntity::getId)
                .containsExactly(n1.getId());
    }

    @Test
    @DisplayName("countAllExposed는 필터 조건에 맞는 노출 SALE 상품 수를 반환한다")
    void shouldCountAllExposed() {
        persistGoods("G-CT-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-CT-2", "B002", "C001", GoodsStatus.SALE, true, false, 2);
        persistGoods("G-CT-3", "B001", "C001", GoodsStatus.SALE, false, false, 3);
        persistGoods("G-CT-4", "B001", "C001", GoodsStatus.SUSPENDED, true, false, 4);
        em.clear();

        assertThat(repository.countAllExposed("", "", GoodsStatus.SALE)).isEqualTo(2L);
        assertThat(repository.countAllExposed("", "B001", GoodsStatus.SALE)).isEqualTo(1L);
        assertThat(repository.countAllExposed("C002", "", GoodsStatus.SALE)).isZero();
    }

    @Test
    @DisplayName("findAllForAdminWithStatus는 status/exposed/keyword 조건으로 조회한다")
    void shouldFindAllForAdminWithStatus() {
        persistGoods("G-A-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-A-2", "B001", "C001", GoodsStatus.SUSPENDED, true, false, 2);
        em.clear();

        Page<GifticonGoodsJpaEntity> result = repository.findAllForAdminWithStatus(
                GoodsStatus.SALE, null, "", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("findAllForAdminWithoutStatus는 status 조건 없이 조회한다")
    void shouldFindAllForAdminWithoutStatus() {
        persistGoods("G-W-1", "B001", "C001", GoodsStatus.SALE, true, false, 1);
        persistGoods("G-W-2", "B001", "C001", GoodsStatus.SUSPENDED, true, false, 2);
        em.clear();

        Page<GifticonGoodsJpaEntity> result = repository.findAllForAdminWithoutStatus(
                true, "", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findAllPopularAndExposed는 popular=true & exposed=true & SALE 상품만 반환한다")
    void shouldFindAllPopularAndExposed() {
        persistGoods("G-P-1", "B001", "C001", GoodsStatus.SALE, true, true, 1);
        persistGoods("G-P-2", "B001", "C001", GoodsStatus.SALE, true, false, 2);
        persistGoods("G-P-3", "B001", "C001", GoodsStatus.SALE, false, true, 3);
        em.clear();

        List<GifticonGoodsJpaEntity> result = repository.findAllPopularAndExposed(
                GoodsStatus.SALE, PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGoodsCode()).isEqualTo("G-P-1");
    }
}
