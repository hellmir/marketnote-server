package com.personal.marketnote.fulfillment.domain;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.ParsingLocalDateTimeException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentAccessToken {
    private static final DateTimeFormatter EXPIRE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String value;
    private LocalDateTime expiresAt;

    public static FulfillmentAccessToken of(String value, String expreDatetime) {
        if (FormatValidator.hasNoValue(value)) {
            throw new FulfillmentQueryParameterNoValueException("Fulfillment access token value");
        }
        if (FormatValidator.hasNoValue(expreDatetime)) {
            throw new FulfillmentQueryParameterNoValueException("Fulfillment access token expiration");
        }

        return FulfillmentAccessToken.builder()
                .value(value)
                .expiresAt(parseExpiration(expreDatetime))
                .build();
    }

    public boolean isExpired(LocalDateTime now) {
        if (FormatValidator.hasNoValue(now) || FormatValidator.hasNoValue(expiresAt)) {
            return false;
        }

        return now.isAfter(expiresAt);
    }

    private static LocalDateTime parseExpiration(String expreDatetime) {
        try {
            return LocalDateTime.parse(expreDatetime, EXPIRE_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ParsingLocalDateTimeException("Failed to parse fulfillment expiration datetime: " + expreDatetime);
        }
    }
}
