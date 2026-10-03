package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonGoodsJpaRepository;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort.FindAllForAdminResult;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort.GifticonGoodsBrandProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonGoodsPersistenceAdapter 테스트")
class GifticonGoodsPersistenceAdapterTest {

    @Mock
    private GifticonGoodsJpaRepository repository;

    @InjectMocks
    private GifticonGoodsPersistenceAdapter adapter;

    private GifticonGoodsJpaEntity buildEntity(Long id, String goodsCode) {
        GifticonGoods restored = GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                .id(id)
                .goodsCode(goodsCode)
                .goodsName("테스트 상품")
                .brandCode("B001")
                .brandName("스타벅스")
                .brandImageUrl("http://img.test.com/brand.jpg")
                .categoryCode("C001")
                .realPrice(12_000L)
                .salePrice(10_000L)
                .cashPrice(10_000L)
                .imageUrl("http://img.test.com/goods.jpg")
                .description("테스트 상품 설명")
                .validDays(30)
                .goodsStatus(GoodsStatus.SALE)
                .exposed(true)
                .popular(false)
                .orderNum(1)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
        return GifticonGoodsJpaEntity.from(restored);
    }

    @Test
    @DisplayName("findByGoodsCode는 엔티티를 도메인으로 변환한다")
    void shouldFindByGoodsCode() {
        given(repository.findByGoodsCode("G001")).willReturn(Optional.of(buildEntity(1L, "G001")));
        Optional<GifticonGoods> result = adapter.findByGoodsCode("G001");
        assertThat(result).isPresent();
        assertThat(result.get().getGoodsCode().getValue()).isEqualTo("G001");
    }

    @Test
    @DisplayName("findAllByGoodsStatus는 모든 엔티티를 도메인 리스트로 반환한다")
    void shouldFindAllByGoodsStatus() {
        given(repository.findAllByGoodsStatus(GoodsStatus.SALE))
                .willReturn(List.of(buildEntity(1L, "G001"), buildEntity(2L, "G002")));
        List<GifticonGoods> result = adapter.findAllByGoodsStatus(GoodsStatus.SALE);
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("save는 엔티티를 저장한다")
    void shouldSave() {
        GifticonGoods domain = buildEntity(1L, "G001").toDomain();
        adapter.save(domain);
        verify(repository).save(any(GifticonGoodsJpaEntity.class));
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("기존 엔티티가 존재하면 updateFrom으로 도메인 상태를 반영한다")
        void shouldUpdateExistingEntity() {
            // given
            GifticonGoodsJpaEntity existing = buildEntity(1L, "G001");
            given(repository.findById(1L)).willReturn(Optional.of(existing));

            // when
            adapter.update(existing.toDomain());

            // then
            verify(repository).findById(1L);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 GifticonGoodsNotFoundException을 던진다")
        void shouldThrowNotFoundWhenNoEntity() {
            GifticonGoodsJpaEntity sample = buildEntity(99L, "G099");
            given(repository.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> adapter.update(sample.toDomain()))
                    .isInstanceOf(GifticonGoodsNotFoundException.class);
        }
    }

    @Test
    @DisplayName("findDistinctBrandsByCategoryCode는 Object[] 결과를 GifticonGoodsBrandProjection으로 매핑한다")
    void shouldMapDistinctBrandsProjection() {
        // given
        List<Object[]> rows = List.of(
                new Object[]{"B001", "스타벅스", "http://img.test.com/brand1.jpg"},
                new Object[]{"B002", "베스킨라빈스", "http://img.test.com/brand2.jpg"}
        );
        given(repository.findDistinctBrandsByCategoryCode("C001", GoodsStatus.SALE)).willReturn(rows);

        // when
        List<GifticonGoodsBrandProjection> result = adapter.findDistinctBrandsByCategoryCode("C001");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).brandCode()).isEqualTo("B001");
        assertThat(result.get(0).brandName()).isEqualTo("스타벅스");
    }

    @Nested
    @DisplayName("findAllForAdmin")
    class FindAllForAdminTest {

        @Test
        @DisplayName("goodsStatus가 null이면 status 없는 쿼리를 호출한다")
        void shouldUseWithoutStatusQueryWhenStatusIsNull() {
            // given
            Page<GifticonGoodsJpaEntity> page = new PageImpl<>(List.of(buildEntity(1L, "G001")));
            given(repository.findAllForAdminWithoutStatus(any(), any(), any(Pageable.class))).willReturn(page);

            // when
            FindAllForAdminResult result = adapter.findAllForAdmin(1, 10, null, true, "");

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            verify(repository).findAllForAdminWithoutStatus(eq(true), eq(""), any(Pageable.class));
        }

        @Test
        @DisplayName("goodsStatus가 있으면 status 포함 쿼리를 호출한다")
        void shouldUseWithStatusQueryWhenStatusProvided() {
            // given
            Page<GifticonGoodsJpaEntity> page = new PageImpl<>(List.of(buildEntity(1L, "G001")));
            given(repository.findAllForAdminWithStatus(eq(GoodsStatus.SALE), any(), any(), any(Pageable.class)))
                    .willReturn(page);

            // when
            FindAllForAdminResult result = adapter.findAllForAdmin(1, 10, GoodsStatus.SALE, null, "테스트");

            // then
            assertThat(result.items()).hasSize(1);
            verify(repository).findAllForAdminWithStatus(eq(GoodsStatus.SALE), eq(null), eq("테스트"), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("findAllExposed는 categoryCode/brandCode가 null이면 빈 문자열로 변환하여 호출한다")
    void shouldFindAllExposedWithNullParams() {
        // given
        Page<GifticonGoodsJpaEntity> page = new PageImpl<>(List.of(buildEntity(1L, "G001")));
        given(repository.findAllExposed(eq(""), eq(""), eq(GoodsStatus.SALE), any(Pageable.class))).willReturn(page);

        // when
        List<GifticonGoods> result = adapter.findAllExposed(null, null, 1, 10);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("countAllExposed는 검색 조건으로 총 건수를 반환한다")
    void shouldCountAllExposed() {
        Page<GifticonGoodsJpaEntity> page = new PageImpl<>(
                List.of(buildEntity(1L, "G001")), PageRequest.of(0, 1), 42L);
        given(repository.findAllExposed(eq("C001"), eq("B001"), eq(GoodsStatus.SALE), any(Pageable.class)))
                .willReturn(page);
        assertThat(adapter.countAllExposed("C001", "B001")).isEqualTo(42L);
    }

    @Test
    @DisplayName("findAllPopularAndExposed는 인기 상품 리스트를 반환한다")
    void shouldFindAllPopularAndExposed() {
        given(repository.findAllPopularAndExposed(eq(GoodsStatus.SALE), any(Pageable.class)))
                .willReturn(List.of(buildEntity(1L, "G001"), buildEntity(2L, "G002")));
        List<GifticonGoods> result = adapter.findAllPopularAndExposed(5);
        assertThat(result).hasSize(2);
    }
}
