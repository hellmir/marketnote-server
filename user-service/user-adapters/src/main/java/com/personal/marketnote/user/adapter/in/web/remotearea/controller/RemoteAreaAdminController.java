package com.personal.marketnote.user.adapter.in.web.remotearea.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.user.adapter.in.web.remotearea.controller.apidocs.RegisterRemoteAreaApiDocs;
import com.personal.marketnote.user.adapter.in.web.remotearea.request.RegisterRemoteAreaRequest;
import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;
import com.personal.marketnote.user.port.in.usecase.remotearea.RegisterRemoteAreaUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@RequestMapping("/api/v1/admin/remote-areas")
@Tag(
        name = "도서산간 지역 관리자 API",
        description = "도서산간 지역 관련 관리자 API"
)
@RequiredArgsConstructor
public class RemoteAreaAdminController {

    private final RegisterRemoteAreaUseCase registerRemoteAreaUseCase;

    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterRemoteAreaApiDocs
    public ResponseEntity<BaseResponse<Void>> registerRemoteArea(
            @Valid @RequestBody RegisterRemoteAreaRequest request
    ) {
        RegisterRemoteAreaCommand command = new RegisterRemoteAreaCommand(
                request.getProvince(),
                request.getDistrict(),
                request.getVillage(),
                request.getSubarea()
        );

        registerRemoteAreaUseCase.registerRemoteArea(command);

        return new ResponseEntity<>(
                BaseResponse.of(null, HttpStatus.CREATED, DEFAULT_SUCCESS_CODE, "도서산간 지역 등록 성공"),
                HttpStatus.CREATED
        );
    }
}
