package com.personal.marketnote.notification.adapter.in.web.device.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.notification.adapter.in.web.device.controller.apidocs.DeleteDeviceTokenApiDocs;
import com.personal.marketnote.notification.adapter.in.web.device.controller.apidocs.RegisterDeviceTokenApiDocs;
import com.personal.marketnote.notification.adapter.in.web.device.request.RegisterDeviceTokenRequest;
import com.personal.marketnote.notification.adapter.in.web.device.response.RegisterDeviceTokenResponse;
import com.personal.marketnote.notification.port.in.command.DeleteDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.command.RegisterDeviceTokenCommand;
import com.personal.marketnote.notification.port.in.result.device.RegisterDeviceTokenResult;
import com.personal.marketnote.notification.port.in.usecase.device.DeleteDeviceTokenUseCase;
import com.personal.marketnote.notification.port.in.usecase.device.RegisterDeviceTokenUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 디바이스 토큰 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-06-03
 * @Description 사용자 디바이스의 FCM 토큰 등록/삭제 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/device-tokens")
@Tag(name = "디바이스 토큰 API", description = "사용자 FCM 디바이스 토큰 관리 API")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final RegisterDeviceTokenUseCase registerDeviceTokenUseCase;
    private final DeleteDeviceTokenUseCase deleteDeviceTokenUseCase;

    /**
     * 디바이스 토큰 등록/갱신
     *
     * @param request   디바이스 토큰 등록 요청
     * @param principal 인증된 사용자
     * @return 디바이스 토큰 등록 응답 {@link RegisterDeviceTokenResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 사용자의 FCM 디바이스 토큰을 등록하거나 갱신합니다. deviceId 기준 upsert로 동작합니다.
     */
    @PostMapping
    @RegisterDeviceTokenApiDocs
    public ResponseEntity<BaseResponse<RegisterDeviceTokenResponse>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        RegisterDeviceTokenResult result = registerDeviceTokenUseCase.registerDeviceToken(
                new RegisterDeviceTokenCommand(
                        ElementExtractor.extractUserId(principal),
                        request.token(),
                        request.platform(),
                        request.deviceId()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterDeviceTokenResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "디바이스 토큰 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 디바이스 토큰 삭제
     *
     * @param deviceId  삭제할 기기 고유 식별자
     * @param principal 인증된 사용자
     * @return 삭제 완료 응답
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 로그인한 사용자의 특정 기기 FCM 디바이스 토큰을 삭제합니다. 본인 소유 기기만 삭제 가능합니다.
     */
    @DeleteMapping("/{deviceId}")
    @DeleteDeviceTokenApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteDeviceToken(
            @PathVariable String deviceId,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        deleteDeviceTokenUseCase.deleteDeviceToken(
                new DeleteDeviceTokenCommand(
                        ElementExtractor.extractUserId(principal),
                        deviceId
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "디바이스 토큰 삭제 성공"
                ),
                HttpStatus.OK
        );
    }
}
