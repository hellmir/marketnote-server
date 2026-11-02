package com.personal.marketnote.product.adapter.in.web.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RegisterProductFulfillmentVendorGoodsRequest {
    @Schema(
            name = "godType",
            description = "상품유형(1:단일, 2:모음, 3:세트, 4:대표상품)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String godType;

    @Schema(
            name = "giftDiv",
            description = "사은품구분(01:본품, 02:사은품, 03:부자재)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String giftDiv;

    @Schema(
            name = "godOptCd1",
            description = "상품옵션코드1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String godOptCd1;

    @Schema(
            name = "godOptCd2",
            description = "상품옵션코드2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String godOptCd2;

    @Schema(
            name = "invGodNmUseYn",
            description = "송장출력용 상품명 사용여부 (Y/N)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String invGodNmUseYn;

    @Schema(
            name = "invGodNm",
            description = "송장출력용 상품명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String invGodNm;

    @Schema(
            name = "supCd",
            description = "공급사코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String supCd;

    @Schema(
            name = "cateCd",
            description = "카테고리코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String cateCd;

    @Schema(
            name = "seasonCd",
            description = "계절상품 코드(0:공용, 1:S/S, 2:F/W)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String seasonCd;

    @Schema(
            name = "genderCd",
            description = "성별상품 코드(A:Unisex, M:men, W:woman, K:kids)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String genderCd;

    @Schema(
            name = "makeYr",
            description = "연식",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String makeYr;

    @Schema(
            name = "godPr",
            description = "단가",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String godPr;

    @Schema(
            name = "inPr",
            description = "공급가",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String inPr;

    @Schema(
            name = "salPr",
            description = "판매가",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String salPr;

    @Schema(
            name = "dealTemp",
            description = "취급온도",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String dealTemp;

    @Schema(
            name = "pickFac",
            description = "피킹설비",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String pickFac;

    @Schema(
            name = "godBarcd",
            description = "상품바코드",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String godBarcd;

    @Schema(
            name = "boxWeight",
            description = "내품BOX무게",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String boxWeight;

    @Schema(
            name = "origin",
            description = "원산지",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String origin;

    @Schema(
            name = "distTermMgtYn",
            description = "유통기한관리여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String distTermMgtYn;

    @Schema(
            name = "useTermDay",
            description = "사용기한",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String useTermDay;

    @Schema(
            name = "outCanDay",
            description = "출고가능일수",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String outCanDay;

    @Schema(
            name = "inCanDay",
            description = "입고가능일수",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String inCanDay;

    @Schema(
            name = "boxDiv",
            description = "출고박스",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String boxDiv;

    @Schema(
            name = "bufGodYn",
            description = "완충상품여부(Y:사용,N:미사용,A:추가완충제)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String bufGodYn;

    @Schema(
            name = "loadingDirection",
            description = "출고박스 상품 적재 기준(NONE:관계없음, UP:세워서 적재)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String loadingDirection;

    @Schema(
            name = "subMate",
            description = "부자재코드(01:홍보물, 02:출력물, 03:쇼핑백, 04:포장지, 05:고객사테이프, 06:케이스, 07:단상자, 08:세트제작용 부자재, 09:출고용 패킹박스, 10:습자지, 11:고객사인박스)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String subMate;

    @Schema(
            name = "useYn",
            description = "사용여부(Y/N)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String useYn;

    @Schema(
            name = "safetyStock",
            description = "안전재고수량",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String safetyStock;

    @Schema(
            name = "feeYn",
            description = "요금적용여부",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String feeYn;

    @Schema(
            name = "saleUnitQty",
            description = "상품판매단위",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String saleUnitQty;

    @Schema(
            name = "cstGodImgUrl",
            description = "고객상품이미지URL",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String cstGodImgUrl;

    @Schema(
            name = "externalGodImgUrl",
            description = "상품이미지 URL",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String externalGodImgUrl;
}
