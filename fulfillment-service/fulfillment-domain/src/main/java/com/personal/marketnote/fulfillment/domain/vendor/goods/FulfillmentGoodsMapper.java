package com.personal.marketnote.fulfillment.domain.vendor.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentGoodsMapper {
    private String customerCode;
    private String accessToken;
    private List<FulfillmentGoodsItemMapper> goods;

    public static FulfillmentGoodsMapper register(
            String customerCode,
            String accessToken,
            List<FulfillmentGoodsItemMapper> goods
    ) {
        FulfillmentGoodsMapper mapper = FulfillmentGoodsMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .goods(goods)
                .build();
        mapper.validate();

        return mapper;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FulfillmentQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FulfillmentQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(goods)) {
            throw new FulfillmentQueryParameterNoValueException("goods");
        }
    }

    public List<Map<String, Object>> toPayload() {
        return goods.stream()
                .map(FulfillmentGoodsItemMapper::toPayload)
                .toList();
    }

    public String getProductId() {
        return goods.getFirst().getCstGodCd();
    }
}
