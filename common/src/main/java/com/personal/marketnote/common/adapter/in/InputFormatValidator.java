package com.personal.marketnote.common.adapter.in;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIdException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.IdNoValueException;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.regex.Pattern;

import static com.personal.marketnote.common.utility.RegularExpressionConstant.POSITIVE_INTEGER_PATTERN;

public class InputFormatValidator {
    static final String ID_NO_VALUE_EXCEPTION = "ID는 필수값입니다.";

    public static void validateId(String id) {
        checkIdIsNotBlank(id);
        checkIdPattern(id);
    }

    private static void checkIdIsNotBlank(String id) {
        if (FormatValidator.hasNoValue(id)) {
            throw new IdNoValueException(ID_NO_VALUE_EXCEPTION);
        }
    }

    private static void checkIdPattern(String id) {
        if (!FormatValidator.isValid(id, Pattern.compile(POSITIVE_INTEGER_PATTERN))) {
            throw new InvalidIdException(id);
        }
    }
}
