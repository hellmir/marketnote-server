package com.personal.marketnote.fulfillment.domain;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.ParsingLocalDateTimeException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FasstoAccessToken {
    private static final DateTimeFormatter EXPIRE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String value;
    private LocalDateTime expiresAt;

    public static FasstoAccessToken of(String value, String expreDatetime) {
        if (FormatValidator.hasNoValue(value)) {
            throw new FasstoQueryParameterNoValueException("Fassto access token value");
        }
        if (FormatValidator.hasNoValue(expreDatetime)) {
            throw new FasstoQueryParameterNoValueException("Fassto access token expiration");
        }

        return FasstoAccessToken.builder()
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
            throw new ParsingLocalDateTimeException("Failed to parse Fassto expiration datetime: " + expreDatetime);
        }
    }
}
