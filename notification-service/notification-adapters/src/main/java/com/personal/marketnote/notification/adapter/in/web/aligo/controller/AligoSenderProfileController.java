package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.AuthenticateAligoSenderProfileRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterAligoSenderProfileRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.AuthenticateAligoSenderProfileResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoSenderProfileListResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterAligoSenderProfileResponse;
import com.personal.marketnote.notification.port.in.command.AuthenticateAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.command.RegisterAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.AuthenticateAligoSenderProfileResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoSenderProfileListResult;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoSenderProfileResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.AuthenticateAligoSenderProfileUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoSenderProfileListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterAligoSenderProfileUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 발신프로필", description = "알리고 카카오채널 발신프로필 관리 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/sender-profiles")
@RequiredArgsConstructor
public class AligoSenderProfileController {

    private final AuthenticateAligoSenderProfileUseCase authenticateAligoSenderProfileUseCase;
    private final RegisterAligoSenderProfileUseCase registerAligoSenderProfileUseCase;
    private final GetAligoSenderProfileListUseCase getAligoSenderProfileListUseCase;

    @PostMapping("/auth")
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<AuthenticateAligoSenderProfileResponse>> authenticateSenderProfile(
            @Valid @RequestBody AuthenticateAligoSenderProfileRequest request
    ) {
        AuthenticateAligoSenderProfileResult result = authenticateAligoSenderProfileUseCase
                .authenticateSenderProfile(new AuthenticateAligoSenderProfileCommand(
                        request.plusId(), request.phoneNumber()
                ));

        return new ResponseEntity<>(
                BaseResponse.of(
                        AuthenticateAligoSenderProfileResponse.from(result),
                        HttpStatus.CREATED, DEFAULT_SUCCESS_CODE, "발신프로필 인증 요청 성공"
                ),
                HttpStatus.CREATED
        );
    }

    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<RegisterAligoSenderProfileResponse>> registerSenderProfile(
            @Valid @RequestBody RegisterAligoSenderProfileRequest request
    ) {
        RegisterAligoSenderProfileResult result = registerAligoSenderProfileUseCase
                .registerSenderProfile(new RegisterAligoSenderProfileCommand(
                        request.senderKey(), request.categoryCode()
                ));

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterAligoSenderProfileResponse.from(result),
                        HttpStatus.CREATED, DEFAULT_SUCCESS_CODE, "발신프로필 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<GetAligoSenderProfileListResponse>> getSenderProfileList() {
        GetAligoSenderProfileListResult result = getAligoSenderProfileListUseCase.getSenderProfileList();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAligoSenderProfileListResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "발신프로필 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
