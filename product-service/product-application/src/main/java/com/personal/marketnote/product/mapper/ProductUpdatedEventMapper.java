package com.personal.marketnote.product.mapper;

import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.exception.ProductInfoNoValueException;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;

public class ProductUpdatedEventMapper {

    public static ProductUpdatedEvent mapToEvent(Product product, FulfillmentVendorGoodsOptionCommand options) {
        if (FormatValidator.hasNoValue(product)) {
            throw new ProductInfoNoValueException("%s:: 상품이 존재하지 않습니다.", FIRST_ERROR_CODE);
        }
        if (FormatValidator.hasNoValue(product.getId())) {
            throw new ProductInfoNoValueException("%s:: 상품 ID가 존재하지 않습니다.", SECOND_ERROR_CODE);
        }
        if (FormatValidator.hasNoValue(product.getName())) {
            throw new ProductInfoNoValueException("%s:: 상품명이 존재하지 않습니다.", THIRD_ERROR_CODE);
        }

        return new ProductUpdatedEvent(
                product.getId(),
                product.getName(),
                options.godType(),
                options.giftDiv(),
                options.godOptCd1(),
                options.godOptCd2(),
                options.invGodNmUseYn(),
                options.invGodNm(),
                options.supCd(),
                options.cateCd(),
                options.seasonCd(),
                options.genderCd(),
                options.makeYr(),
                options.godPr(),
                options.inPr(),
                options.salPr(),
                options.dealTemp(),
                options.pickFac(),
                options.godBarcd(),
                options.boxWeight(),
                options.origin(),
                options.distTermMgtYn(),
                options.useTermDay(),
                options.outCanDay(),
                options.inCanDay(),
                options.boxDiv(),
                options.bufGodYn(),
                options.loadingDirection(),
                options.subMate(),
                options.useYn(),
                options.safetyStock(),
                options.feeYn(),
                options.saleUnitQty(),
                options.cstGodImgUrl(),
                options.externalGodImgUrl()
        );
    }
}
