package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.DisconnectFasstoAuthApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.RequestFasstoAuthApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.FasstoAuthTokenResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoAccessTokenResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.DisconnectFasstoAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFasstoAuthUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 파스토 인증 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-24
 * @Description 파스토 인증 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/auth")
@Tag(name = "파스토 인증 API", description = "파스토 인증 관련 API")
@RequiredArgsConstructor
public class FasstoAuthController {
    private final RequestFasstoAuthUseCase requestFasstoAuthUseCase;
    private final DisconnectFasstoAuthUseCase disconnectFasstoAuthUseCase;

    /**
     * (관리자) 파스토 인증 요청
     *
     * @Author 성효빈
     * @Date 2026-01-24
     * @Description 파스토 인증을 요청합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @RequestFasstoAuthApiDocs
    public ResponseEntity<BaseResponse<FasstoAuthTokenResponse>> requestAccessToken() {
        FasstoAccessTokenResult result = FasstoAccessTokenResult.from(requestFasstoAuthUseCase.requestAccessToken());

        return new ResponseEntity<>(
                BaseResponse.of(
                        FasstoAuthTokenResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 인증 요청 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 인증 해제 요청
     *
     * @param accessToken 파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-01-25
     * @Description 파스토 인증 해제를 요청합니다.
     */
    @GetMapping("/disconnect")
    @PreAuthorize(ADMIN_POINTCUT)
    @DisconnectFasstoAuthApiDocs
    public ResponseEntity<BaseResponse<Void>> disconnectAccessToken(
            @RequestHeader("accessToken") String accessToken
    ) {
        disconnectFasstoAuthUseCase.disconnectAccessToken(accessToken);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 인증 해제 성공"
                ),
                HttpStatus.OK
        );
    }
}
