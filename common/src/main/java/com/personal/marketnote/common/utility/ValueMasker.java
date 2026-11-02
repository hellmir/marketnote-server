package com.personal.marketnote.common.utility;

import static com.personal.marketnote.common.utility.CharacterConstant.WILD_CARD;

public class ValueMasker {
    public static String mask(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return value;
        }

        int length = value.length();

        if (length <= 2) {
            return value.substring(0, 1) + WILD_CARD;
        }

        if (length <= 4) {
            return value.substring(0, 2) + WILD_CARD.repeat(2);
        }

        return value.substring(0, 3) + WILD_CARD.repeat(3);
    }
}
