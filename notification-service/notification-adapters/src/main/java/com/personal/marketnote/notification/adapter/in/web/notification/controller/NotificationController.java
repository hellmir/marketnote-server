package com.personal.marketnote.notification.adapter.in.web.notification.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.notification.adapter.in.web.notification.controller.apidocs.GetNotificationHistoryApiDocs;
import com.personal.marketnote.notification.adapter.in.web.notification.response.GetNotificationHistoryResponse;
import com.personal.marketnote.notification.port.in.command.GetNotificationHistoryCommand;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;
import com.personal.marketnote.notification.port.in.usecase.notification.GetNotificationHistoryUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 알림 이력 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-06-03
 * @Description 사용자의 알림 이력 조회 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림 이력 API", description = "사용자 알림 이력 관리 API")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final GetNotificationHistoryUseCase getNotificationHistoryUseCase;

    /**
     * 알림 이력 조회
     *
     * @param principal 인증된 사용자
     * @param cursor    이전 페이지 마지막 항목의 ID
     * @param pageSize  페이지 크기 (기본 20)
     * @return 알림 이력 목록 {@link GetNotificationHistoryResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 인증된 사용자의 알림 발송 이력을 커서 기반 페이징으로 조회합니다.
     */
    @GetMapping
    @GetNotificationHistoryApiDocs
    public ResponseEntity<BaseResponse<GetNotificationHistoryResponse>> getNotificationHistory(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "page-size", required = false, defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        GetNotificationHistoryCommand command = new GetNotificationHistoryCommand(
                ElementExtractor.extractUserId(principal),
                cursor,
                pageSize
        );

        GetNotificationHistoryResult result = getNotificationHistoryUseCase.getNotificationHistory(command);
        GetNotificationHistoryResponse response = GetNotificationHistoryResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(
                        response,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 이력 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
