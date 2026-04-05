package com.personal.marketnote.reward.domain.gifticon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GifticonGoodsTest {

    @Nested
    @DisplayName("CreateState로 생성")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로 GifticonGoods를 생성하면 exposed=false, orderNum=null이다")
        void createWithDefaults() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsCreateState.builder()
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .brandImageUrl("https://example.com/brand.jpg")
                    .categoryCode("3")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4500L)
                    .imageUrl("https://example.com/image.jpg")
                    .description("상품 설명")
                    .validDays(90)
                    .goodsStatus("SALE")
                    .build());

            assertThat(goods.getGoodsCode()).isEqualTo("G001");
            assertThat(goods.getGoodsName()).isEqualTo("스타벅스 아메리카노");
            assertThat(goods.getBrandCode()).isEqualTo("B001");
            assertThat(goods.getBrandName()).isEqualTo("스타벅스");
            assertThat(goods.getRealPrice()).isEqualTo(5000L);
            assertThat(goods.getSalePrice()).isEqualTo(4500L);
            assertThat(goods.getCashPrice()).isEqualTo(4500L);
            assertThat(goods.getGoodsStatus()).isEqualTo("SALE");
            assertThat(goods.isExposed()).isFalse();
            assertThat(goods.getOrderNum()).isNull();
        }
    }

    @Nested
    @DisplayName("SnapshotState로 복원")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 GifticonGoods를 복원한다")
        void restoreFromSnapshotState() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                    .id(1L)
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .brandImageUrl("https://example.com/brand.jpg")
                    .categoryCode("3")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4000L)
                    .imageUrl("https://example.com/image.jpg")
                    .description("상품 설명")
                    .validDays(90)
                    .goodsStatus("SALE")
                    .exposed(true)
                    .orderNum(1)
                    .createdAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                    .modifiedAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                    .build());

            assertThat(goods.getId()).isEqualTo(1L);
            assertThat(goods.isExposed()).isTrue();
            assertThat(goods.getOrderNum()).isEqualTo(1);
            assertThat(goods.getCashPrice()).isEqualTo(4000L);
        }
    }

    @Nested
    @DisplayName("상태 변경 메서드")
    class StateMutation {

        @Test
        @DisplayName("expose 호출 시 exposed가 true로 변경된다")
        void exposeChangesExposedToTrue() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsCreateState.builder()
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4500L)
                    .goodsStatus("SALE")
                    .build());

            goods.expose();

            assertThat(goods.isExposed()).isTrue();
        }

        @Test
        @DisplayName("unexpose 호출 시 exposed가 false로 변경된다")
        void unexposeChangesExposedToFalse() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                    .id(1L)
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4500L)
                    .goodsStatus("SALE")
                    .exposed(true)
                    .orderNum(1)
                    .createdAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                    .modifiedAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                    .build());

            goods.unexpose();

            assertThat(goods.isExposed()).isFalse();
        }

        @Test
        @DisplayName("changeOrderNum 호출 시 orderNum이 변경된다")
        void changeOrderNumUpdatesOrderNum() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsCreateState.builder()
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4500L)
                    .goodsStatus("SALE")
                    .build());

            goods.changeOrderNum(5);

            assertThat(goods.getOrderNum()).isEqualTo(5);
        }

        @Test
        @DisplayName("동기화 시 상품 정보가 업데이트된다")
        void syncUpdatesGoodsInfo() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsSnapshotState.builder()
                    .id(1L)
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4000L)
                    .goodsStatus("SALE")
                    .exposed(true)
                    .orderNum(1)
                    .createdAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                    .modifiedAt(LocalDateTime.of(2026, 4, 3, 12, 0, 0))
                    .build());

            goods.syncFromApi("스타벅스 카페라떼", "B001", "스타벅스", "https://example.com/brand2.jpg",
                    "3", 5500L, 5000L, "https://example.com/image2.jpg", "새 설명", 90, "SALE");

            assertThat(goods.getGoodsName()).isEqualTo("스타벅스 카페라떼");
            assertThat(goods.getRealPrice()).isEqualTo(5500L);
            assertThat(goods.getSalePrice()).isEqualTo(5000L);
            assertThat(goods.getCashPrice()).isEqualTo(4000L);
            assertThat(goods.isExposed()).isTrue();
            assertThat(goods.getOrderNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("isSale은 goodsStatus가 SALE일 때 true이다")
        void isSaleReturnsTrueWhenSale() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsCreateState.builder()
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4500L)
                    .goodsStatus("SALE")
                    .build());

            assertThat(goods.isSale()).isTrue();
        }

        @Test
        @DisplayName("isSale은 goodsStatus가 SUS일 때 false이다")
        void isSaleReturnsFalseWhenSuspended() {
            GifticonGoods goods = GifticonGoods.from(GifticonGoodsCreateState.builder()
                    .goodsCode("G001")
                    .goodsName("스타벅스 아메리카노")
                    .brandCode("B001")
                    .brandName("스타벅스")
                    .realPrice(5000L)
                    .salePrice(4500L)
                    .cashPrice(4500L)
                    .goodsStatus("SUS")
                    .build());

            assertThat(goods.isSale()).isFalse();
        }
    }
}
