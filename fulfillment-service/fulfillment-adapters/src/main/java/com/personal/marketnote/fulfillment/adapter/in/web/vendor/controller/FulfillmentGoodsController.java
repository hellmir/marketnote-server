package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.*;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentGoodsRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentGoodsElementsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsElementsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.*;
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

/**
 * 파스토 상품 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-29
 * @Description 파스토 상품 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/goods")
@Tag(name = "파스토 상품 API", description = "파스토 상품 관련 API")
@RequiredArgsConstructor
public class FulfillmentGoodsController {
    private final RegisterFulfillmentGoodsUseCase registerFulfillmentGoodsUseCase;
    private final GetFulfillmentGoodsUseCase getFulfillmentGoodsUseCase;
    private final GetFulfillmentGoodsDetailUseCase getFulfillmentGoodsDetailUseCase;
    private final UpdateFulfillmentGoodsUseCase updateFulfillmentGoodsUseCase;
    private final GetFulfillmentGoodsElementsUseCase getFulfillmentGoodsElementsUseCase;

    /**
     * (관리자) 파스토 상품 등록 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      상품 등록 요청 정보
     * @Author 성효빈
     * @Date 2026-01-29
     * @Description 파스토 상품 등록을 요청합니다.
     */
    @PostMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentGoodsApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentGoodsResponse>> registerGoods(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentGoodsRequest> request
    ) {
        RegisterFulfillmentGoodsResult result = registerFulfillmentGoodsUseCase.registerGoods(
                FulfillmentGoodsRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentGoodsResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 상품 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-01-30
     * @Description 파스토 상품 목록을 조회합니다.
     */
    @GetMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentGoodsApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentGoodsResponse>> getGoods(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentGoodsResult result = getFulfillmentGoodsUseCase.getGoods(
                FulfillmentGoodsRequestToCommandMapper.mapToGoodsCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 단일 상품 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param godNm        조회 대상 상품 식별자(고객사상품코드)
     * @Author 성효빈
     * @Date 2026-02-05
     * @Description 파스토 단일 상품을 조회합니다.
     */
    @GetMapping("/detail/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentGoodsDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentGoodsResponse>> getGoodsDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("godNm") String godNm
    ) {
        GetFulfillmentGoodsResult result = getFulfillmentGoodsDetailUseCase.getGoodsDetail(
                FulfillmentGoodsRequestToCommandMapper.mapToGoodsDetailCommand(customerCode, accessToken, godNm)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 단일 상품 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 모음상품 상세 정보 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-01-31
     * @Description 파스토 모음상품 상세 정보를 조회합니다.
     */
    @GetMapping("/element/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentGoodsElementsApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentGoodsElementsResponse>> getGoodsElements(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentGoodsElementsResult result = getFulfillmentGoodsElementsUseCase.getGoodsElements(
                FulfillmentGoodsRequestToCommandMapper.mapToGoodsElementsCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentGoodsElementsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 모음상품 상세 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 상품 수정 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      상품 수정 요청 정보
     * @Author 성효빈
     * @Date 2026-01-30
     * @Description 파스토 상품 정보를 수정합니다.
     */
    @PutMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateFulfillmentGoodsApiDocs
    public ResponseEntity<BaseResponse<UpdateFulfillmentGoodsResponse>> updateGoods(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFulfillmentGoodsRequest> request
    ) {
        UpdateFulfillmentGoodsResult result = updateFulfillmentGoodsUseCase.updateGoods(
                FulfillmentGoodsRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFulfillmentGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
