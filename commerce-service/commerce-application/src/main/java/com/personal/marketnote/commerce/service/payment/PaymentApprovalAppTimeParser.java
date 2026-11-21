package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * PG 응답/관리자 입력의 appTime 문자열(yyyyMMddHHmmss)을 LocalDateTime으로 변환한다.
 *
 * <p>외부 응답 정화(분류 C): 형식 오류 시 현재 시각으로 폴백하고 log.warn으로 운영 관찰 신호를 남긴다.
 * 도메인(PspPaymentEvent)에 logger 의존을 두지 않기 위해 application 레이어에 위치한다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentApprovalAppTimeParser {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Clock clock;

    public LocalDateTime parse(String appTime) {
        if (FormatValidator.hasNoValue(appTime)) {
            return LocalDateTime.now(clock);
        }
        try {
            return LocalDateTime.parse(appTime, FORMATTER);
        } catch (DateTimeParseException dtpe) {
            log.warn("fallback applied: PG appTime 파싱 실패 — 현재 시각으로 대체. appTime={}", appTime, dtpe);
            return LocalDateTime.now(clock);
        }
    }
}
