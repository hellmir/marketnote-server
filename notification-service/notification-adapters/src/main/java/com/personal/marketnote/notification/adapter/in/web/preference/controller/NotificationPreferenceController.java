package com.personal.marketnote.notification.adapter.in.web.preference.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.notification.adapter.in.web.preference.controller.apidocs.GetNotificationPreferenceApiDocs;
import com.personal.marketnote.notification.adapter.in.web.preference.controller.apidocs.UpdateNotificationPreferenceApiDocs;
import com.personal.marketnote.notification.adapter.in.web.preference.request.UpdateNotificationPreferenceRequest;
import com.personal.marketnote.notification.adapter.in.web.preference.response.GetNotificationPreferenceResponse;
import com.personal.marketnote.notification.port.in.command.UpdateNotificationPreferenceCommand;
import com.personal.marketnote.notification.port.in.usecase.preference.GetNotificationPreferenceUseCase;
import com.personal.marketnote.notification.port.in.usecase.preference.UpdateNotificationPreferenceUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final UpdateNotificationPreferenceUseCase updateNotificationPreferenceUseCase;

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

    /**
     * 알림 수신 설정 변경
     *
     * @param request   알림 수신 설정 변경 요청
     * @param principal 인증된 사용자
     * @return 변경 완료 응답
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 인증된 사용자의 특정 알림 타입의 수신 설정을 변경합니다.
     */
    @PutMapping
    @UpdateNotificationPreferenceApiDocs
    public ResponseEntity<BaseResponse<Void>> updateNotificationPreference(
            @Valid @RequestBody UpdateNotificationPreferenceRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        updateNotificationPreferenceUseCase.updateNotificationPreference(
                new UpdateNotificationPreferenceCommand(
                        ElementExtractor.extractUserId(principal),
                        request.notificationType(),
                        request.enabled()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 수신 설정 변경 성공"
                ),
                HttpStatus.OK
        );
    }
}
