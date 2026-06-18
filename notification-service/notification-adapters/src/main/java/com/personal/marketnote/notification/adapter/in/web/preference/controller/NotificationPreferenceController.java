package com.personal.marketnote.notification.adapter.in.web.preference.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.notification.adapter.in.web.preference.controller.apidocs.GetNotificationPreferenceApiDocs;
import com.personal.marketnote.notification.adapter.in.web.preference.response.GetNotificationPreferenceResponse;
import com.personal.marketnote.notification.port.in.usecase.preference.GetNotificationPreferenceUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 알림 수신 설정 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-06-03
 * @Description 사용자의 알림 수신 설정 조회/변경 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/notifications/preferences")
@Tag(name = "알림 수신 설정 API", description = "사용자 알림 수신 설정 관리 API")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final GetNotificationPreferenceUseCase getNotificationPreferenceUseCase;

    /**
     * 알림 수신 설정 조회
     *
     * @param principal 인증된 사용자
     * @return 알림 수신 설정 목록 {@link GetNotificationPreferenceResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 인증된 사용자의 모든 알림 타입별 수신 설정을 조회합니다.
     */
    @GetMapping
    @GetNotificationPreferenceApiDocs
    public ResponseEntity<BaseResponse<List<GetNotificationPreferenceResponse>>> getNotificationPreferences(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        List<GetNotificationPreferenceResponse> responses = getNotificationPreferenceUseCase
                .getNotificationPreferences(ElementExtractor.extractUserId(principal))
                .stream()
                .map(GetNotificationPreferenceResponse::from)
                .toList();

        return new ResponseEntity<>(
                BaseResponse.of(
                        responses,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 수신 설정 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
