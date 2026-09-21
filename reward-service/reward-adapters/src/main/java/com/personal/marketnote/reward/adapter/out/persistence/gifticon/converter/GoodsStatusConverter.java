package com.personal.marketnote.reward.adapter.out.persistence.gifticon.converter;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GoodsStatusConverter implements AttributeConverter<GoodsStatus, String> {

    @Override
    public String convertToDatabaseColumn(GoodsStatus attribute) {
        if (FormatValidator.hasNoValue(attribute)) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public GoodsStatus convertToEntityAttribute(String dbData) {
        if (FormatValidator.hasNoValue(dbData)) {
            return null;
        }
        return GoodsStatus.from(dbData);
    }
}
