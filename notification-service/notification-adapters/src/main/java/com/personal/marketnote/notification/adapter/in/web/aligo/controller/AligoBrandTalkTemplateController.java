package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterBrandTalkTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterBrandTalkTemplateResponse;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCouponCommand;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterBrandTalkTemplateUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 브랜드메시지 템플릿", description = "알리고 브랜드메시지 템플릿 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/brandtalk/templates")
@RequiredArgsConstructor
public class AligoBrandTalkTemplateController {

    private final RegisterBrandTalkTemplateUseCase registerBrandTalkTemplateUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<RegisterBrandTalkTemplateResponse>> registerTemplate(
            @Valid @RequestPart("request") RegisterBrandTalkTemplateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        List<BrandTalkButtonCommand> buttonCommands = mapButtons(request.buttons());
        BrandTalkCouponCommand couponCommand = mapCoupon(request.coupon());

        byte[] imageFile = image != null ? image.getBytes() : null;
        String imageFileName = image != null ? image.getOriginalFilename() : null;

        RegisterBrandTalkTemplateCommand command = new RegisterBrandTalkTemplateCommand(
                request.senderKey(), request.templateCode(), request.templateName(),
                request.templateType(), request.templateContent(), request.adult(),
                request.imageLink(), buttonCommands, couponCommand,
                imageFile, imageFileName
        );

        RegisterBrandTalkTemplateResult result = registerBrandTalkTemplateUseCase.registerTemplate(command);

        RegisterBrandTalkTemplateResponse response = RegisterBrandTalkTemplateResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.CREATED, DEFAULT_SUCCESS_CODE, "브랜드메시지 템플릿 등록 성공"),
                HttpStatus.CREATED
        );
    }

    private List<BrandTalkButtonCommand> mapButtons(List<RegisterBrandTalkTemplateRequest.ButtonRequest> buttons) {
        if (buttons == null) {
            return List.of();
        }
        return buttons.stream()
                .map(btn -> new BrandTalkButtonCommand(
                        btn.name(), btn.linkType(),
                        btn.linkM(), btn.linkP(), btn.linkI(), btn.linkA()
                ))
                .toList();
    }

    private BrandTalkCouponCommand mapCoupon(RegisterBrandTalkTemplateRequest.CouponRequest coupon) {
        if (coupon == null) {
            return null;
        }
        return new BrandTalkCouponCommand(
                coupon.title(), coupon.description(),
                coupon.linkM(), coupon.linkP(), coupon.linkI(), coupon.linkA()
        );
    }
}
