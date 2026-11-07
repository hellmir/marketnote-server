package com.personal.marketnote.reward.mapper;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsCreateState;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSyncState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderCreateState;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort.FetchedGifticonGoodsItem;

public class GifticonToStateMapper {

    public static GifticonGoodsCreateState mapToGoodsCreateState(FetchedGifticonGoodsItem item) {
        return GifticonGoodsCreateState.builder()
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
                .goodsStatus(GoodsStatus.from(item.goodsStatus()))
                .build();
    }

    public static GifticonGoodsSyncState mapToGoodsSyncState(FetchedGifticonGoodsItem item) {
        return GifticonGoodsSyncState.builder()
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
                .goodsStatus(GoodsStatus.from(item.goodsStatus()))
                .build();
    }

    /**
     * 7개 개별 파라미터로 {@link GifticonOrderCreateState}를 생성한다.
     *
     * <p><b>구조적 제약 (HIGH-2):</b>
     * 일반적인 매퍼 패턴은 단일 source 객체(Command/Result/Entity)를 인자로 받지만,
     * 본 메서드는 호출자({@code PurchaseGifticonTransactionHelper#deductCashAndCreateOrder})가
     * 이미 7개의 개별 파라미터로 분해된 상태에서 호출하므로 원본 객체가 존재하지 않는다.
     * 호출자에서 다시 묶어 전달하려면 {@code port.in.command} 레이어의 Command 객체를
     * application 레이어에서 합성해야 하며, 이는 Service → Port(in) → Command 역참조로
     * 헥사고날 의존 방향을 깨뜨린다. 따라서 예외적으로 7개 개별 파라미터를 유지한다.</p>
     */
    public static GifticonOrderCreateState mapToOrderCreateState(
            Long userId, String goodsCode, String goodsName, String brandName,
            String productImageUrl, String trId, Long cashPrice
    ) {
        return GifticonOrderCreateState.builder()
                .userId(userId)
                .goodsCode(goodsCode)
                .goodsName(goodsName)
                .brandName(brandName)
                .productImageUrl(productImageUrl)
                .trId(trId)
                .cashPrice(cashPrice)
                .build();
    }
}
