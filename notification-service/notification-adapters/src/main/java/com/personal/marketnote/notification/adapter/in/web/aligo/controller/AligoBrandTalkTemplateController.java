package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.RegisterBrandTalkTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetBrandTalkTemplateListResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.RegisterBrandTalkTemplateResponse;
import com.personal.marketnote.notification.port.in.command.*;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetBrandTalkTemplateListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterBrandTalkTemplateUseCase;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
    private final GetBrandTalkTemplateListUseCase getBrandTalkTemplateListUseCase;

    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<GetBrandTalkTemplateListResponse>> getTemplateList(
            @RequestParam("sender-key") @NotBlank @Size(max = 40) String senderKey,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "page-size", defaultValue = "10") @Min(1) int pageSize
    ) {
        GetBrandTalkTemplateListResult result =
                getBrandTalkTemplateListUseCase.getTemplateList(senderKey, page, pageSize);

        GetBrandTalkTemplateListResponse response = GetBrandTalkTemplateListResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "브랜드메시지 템플릿 목록 조회 성공"),
                HttpStatus.OK
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<RegisterBrandTalkTemplateResponse>> registerTemplate(
            @Valid @RequestPart("request") RegisterBrandTalkTemplateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "carouselItemImages", required = false) List<MultipartFile> carouselItemImages
    ) throws IOException {
        List<BrandTalkButtonCommand> buttonCommands = mapButtons(request.buttons());
        BrandTalkCouponCommand couponCommand = mapCoupon(request.coupon());

        byte[] imageFile = FormatValidator.hasValue(image) ? image.getBytes() : null;
        String imageFileName = FormatValidator.hasValue(image) ? image.getOriginalFilename() : null;

        List<byte[]> carouselItemImageFiles = mapCarouselItemImageFiles(carouselItemImages);
        List<String> carouselItemImageFileNames = mapCarouselItemImageFileNames(carouselItemImages);

        RegisterBrandTalkTemplateCommand command = new RegisterBrandTalkTemplateCommand(
                request.senderKey(), request.templateCode(), request.templateName(),
                request.templateType(), request.templateContent(), request.adult(),
                request.imageLink(), buttonCommands, couponCommand,
                request.templateHeader(),
                mapWideItem(request.mainWideItem()),
                mapWideItems(request.subWideItems()),
                request.carouselHeader(),
                mapCarouselItems(request.carouselItems()),
                mapCarouselCommerces(request.carouselCommerces()),
                mapCarouselTail(request.carouselTail()),
                request.videoUrl(), request.videoThumbnailUrl(),
                mapCommerceInfo(request.commerceInfo()),
                imageFile, imageFileName,
                carouselItemImageFiles, carouselItemImageFileNames
        );

        RegisterBrandTalkTemplateResult result = registerBrandTalkTemplateUseCase.registerTemplate(command);

        RegisterBrandTalkTemplateResponse response = RegisterBrandTalkTemplateResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.CREATED, DEFAULT_SUCCESS_CODE, "브랜드메시지 템플릿 등록 성공"),
                HttpStatus.CREATED
        );
    }

    private List<BrandTalkButtonCommand> mapButtons(List<RegisterBrandTalkTemplateRequest.ButtonRequest> buttons) {
        if (FormatValidator.hasNoValue(buttons)) {
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
        if (FormatValidator.hasNoValue(coupon)) {
            return null;
        }
        return new BrandTalkCouponCommand(
                coupon.title(), coupon.description(),
                coupon.linkM(), coupon.linkP(), coupon.linkI(), coupon.linkA()
        );
    }

    private BrandTalkWideItemCommand mapWideItem(RegisterBrandTalkTemplateRequest.WideItemRequest item) {
        if (FormatValidator.hasNoValue(item)) {
            return null;
        }
        return new BrandTalkWideItemCommand(
                item.title(), item.imageUrl(), item.linkM(), item.linkP(), item.linkI(), item.linkA()
        );
    }

    private List<BrandTalkWideItemCommand> mapWideItems(List<RegisterBrandTalkTemplateRequest.WideItemRequest> items) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream().map(this::mapWideItem).toList();
    }

    private List<BrandTalkCarouselItemCommand> mapCarouselItems(
            List<RegisterBrandTalkTemplateRequest.CarouselItemRequest> items
    ) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream()
                .map(item -> new BrandTalkCarouselItemCommand(
                        item.header(), item.content(),
                        mapButtons(item.buttons()), mapCoupon(item.coupon())
                ))
                .toList();
    }

    private List<BrandTalkCarouselCommerceCommand> mapCarouselCommerces(
            List<RegisterBrandTalkTemplateRequest.CarouselCommerceRequest> commerces
    ) {
        if (FormatValidator.hasNoValue(commerces)) {
            return List.of();
        }
        return commerces.stream()
                .map(c -> new BrandTalkCarouselCommerceCommand(
                        c.title(), c.regularPrice(),
                        c.discountPrice(), c.discountRate(), c.discountFixed()
                ))
                .toList();
    }

    private BrandTalkCarouselTailCommand mapCarouselTail(RegisterBrandTalkTemplateRequest.CarouselTailRequest tail) {
        if (FormatValidator.hasNoValue(tail)) {
            return null;
        }
        return new BrandTalkCarouselTailCommand(tail.linkM());
    }

    private BrandTalkCommerceInfoCommand mapCommerceInfo(RegisterBrandTalkTemplateRequest.CommerceInfoRequest commerceInfo) {
        if (FormatValidator.hasNoValue(commerceInfo)) {
            return null;
        }
        return new BrandTalkCommerceInfoCommand(
                commerceInfo.title(), commerceInfo.regularPrice(),
                commerceInfo.discountPrice(), commerceInfo.discountRate(), commerceInfo.discountFixed()
        );
    }

    private List<byte[]> mapCarouselItemImageFiles(List<MultipartFile> files) throws IOException {
        if (FormatValidator.hasNoValue(files)) {
            return List.of();
        }
        List<byte[]> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(file.getBytes());
        }
        return result;
    }

    private List<String> mapCarouselItemImageFileNames(List<MultipartFile> files) {
        if (FormatValidator.hasNoValue(files)) {
            return List.of();
        }
        return files.stream().map(MultipartFile::getOriginalFilename).toList();
    }
}
