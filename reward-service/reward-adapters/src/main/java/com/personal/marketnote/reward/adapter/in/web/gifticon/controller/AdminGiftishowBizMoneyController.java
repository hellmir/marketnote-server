package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs.GetGiftishowBizMoneyBalanceApiDocs;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGiftishowBizMoneyBalanceResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonVendorBalanceResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonVendorBalanceUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@RequestMapping("/api/v1/admin/giftishow/biz-money")
@Tag(name = "관리자 기프티쇼 비즈머니 API", description = "관리자 기프티쇼 비즈머니 관련 API")
@RequiredArgsConstructor
public class AdminGiftishowBizMoneyController {

    private final GetGifticonVendorBalanceUseCase getGifticonVendorBalanceUseCase;

    /**
     * (관리자) 기프티쇼 비즈머니 잔액 조회
     *
     * @Author 성효빈
     * @Date 2026-04-06
     * @Description 관리자가 기프티쇼 비즈 계정의 비즈머니 잔액을 조회합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetGiftishowBizMoneyBalanceApiDocs
    public ResponseEntity<BaseResponse<GetGiftishowBizMoneyBalanceResponse>> getBalance() {
        GetGifticonVendorBalanceResult result = getGifticonVendorBalanceUseCase.getBalance();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetGiftishowBizMoneyBalanceResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "기프티쇼 비즈머니 잔액 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
