package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.DeleteAligoTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterAligoTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.UpdateAligoTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterAligoTemplateResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.UpdateAligoTemplateResponse;
import com.personal.marketnote.notification.port.in.command.AligoTemplateButtonCommand;
import com.personal.marketnote.notification.port.in.command.DeleteAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.command.RegisterAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoTemplateResult;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.DeleteAligoTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterAligoTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.UpdateAligoTemplateUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 알림톡 템플릿", description = "알리고 알림톡 템플릿 CRUD API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/templates")
@RequiredArgsConstructor
public class AligoTemplateController {

    private final RegisterAligoTemplateUseCase registerAligoTemplateUseCase;
    private final UpdateAligoTemplateUseCase updateAligoTemplateUseCase;
    private final DeleteAligoTemplateUseCase deleteAligoTemplateUseCase;

    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<RegisterAligoTemplateResponse>> registerTemplate(
            @Valid @RequestBody RegisterAligoTemplateRequest request
    ) {
        RegisterAligoTemplateResult result = registerAligoTemplateUseCase.registerTemplate(
                new RegisterAligoTemplateCommand(
                        request.senderKey(), request.templateCode(),
                        request.templateName(), request.templateContent(),
                        request.templateTitle(), request.templateSubtitle(),
                        request.templateType(), request.templateEmType(),
                        request.templateSecure(), request.templateAdvert(),
                        request.imageUrl(), request.imageName(),
                        mapButtons(request.buttons())
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterAligoTemplateResponse.from(result),
                        HttpStatus.CREATED, DEFAULT_SUCCESS_CODE, "템플릿 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{templateCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<UpdateAligoTemplateResponse>> updateTemplate(
            @PathVariable String templateCode,
            @Valid @RequestBody UpdateAligoTemplateRequest request
    ) {
        UpdateAligoTemplateResult result = updateAligoTemplateUseCase.updateTemplate(
                new UpdateAligoTemplateCommand(
                        templateCode, request.senderKey(),
                        request.templateName(), request.templateContent(),
                        request.templateTitle(), request.templateSubtitle(),
                        mapButtons(request.buttons())
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateAligoTemplateResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "템플릿 수정 성공"
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{templateCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<Void>> deleteTemplate(
            @PathVariable String templateCode,
            @Valid @RequestBody DeleteAligoTemplateRequest request
    ) {
        deleteAligoTemplateUseCase.deleteTemplate(new DeleteAligoTemplateCommand(templateCode, request.senderKey()));

        return new ResponseEntity<>(
                BaseResponse.of(null, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "템플릿 삭제 성공"),
                HttpStatus.OK
        );
    }

    private List<AligoTemplateButtonCommand> mapButtons(
            List<RegisterAligoTemplateRequest.ButtonRequest> buttons
    ) {
        if (buttons == null) {
            return List.of();
        }
        return buttons.stream()
                .map(btn -> new AligoTemplateButtonCommand(
                        btn.ordering(), btn.name(), btn.linkType(),
                        btn.linkMo(), btn.linkPc(), btn.linkIos(), btn.linkAnd()
                ))
                .toList();
    }
}
