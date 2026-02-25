package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.*;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FasstoGoodsRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFasstoGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFasstoGoodsElementsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFasstoGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFasstoGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFasstoGoodsResponse;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoGoodsElementsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoGoodsResult;
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
public class FasstoGoodsController {
    private final RegisterFasstoGoodsUseCase registerFasstoGoodsUseCase;
    private final GetFasstoGoodsUseCase getFasstoGoodsUseCase;
    private final GetFasstoGoodsDetailUseCase getFasstoGoodsDetailUseCase;
    private final UpdateFasstoGoodsUseCase updateFasstoGoodsUseCase;
    private final GetFasstoGoodsElementsUseCase getFasstoGoodsElementsUseCase;

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
    @RegisterFasstoGoodsApiDocs
    public ResponseEntity<BaseResponse<RegisterFasstoGoodsResponse>> registerGoods(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFasstoGoodsRequest> request
    ) {
        RegisterFasstoGoodsResult result = registerFasstoGoodsUseCase.registerGoods(
                FasstoGoodsRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFasstoGoodsResponse.from(result),
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
    @GetFasstoGoodsApiDocs
    public ResponseEntity<BaseResponse<GetFasstoGoodsResponse>> getGoods(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFasstoGoodsResult result = getFasstoGoodsUseCase.getGoods(
                FasstoGoodsRequestToCommandMapper.mapToGoodsCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoGoodsResponse.from(result),
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
    @GetFasstoGoodsDetailApiDocs
    public ResponseEntity<BaseResponse<GetFasstoGoodsResponse>> getGoodsDetail(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam("godNm") String godNm
    ) {
        GetFasstoGoodsResult result = getFasstoGoodsDetailUseCase.getGoodsDetail(
                FasstoGoodsRequestToCommandMapper.mapToGoodsDetailCommand(customerCode, accessToken, godNm)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoGoodsResponse.from(result),
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
    @GetFasstoGoodsElementsApiDocs
    public ResponseEntity<BaseResponse<GetFasstoGoodsElementsResponse>> getGoodsElements(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFasstoGoodsElementsResult result = getFasstoGoodsElementsUseCase.getGoodsElements(
                FasstoGoodsRequestToCommandMapper.mapToGoodsElementsCommand(customerCode, accessToken)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFasstoGoodsElementsResponse.from(result),
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
    @UpdateFasstoGoodsApiDocs
    public ResponseEntity<BaseResponse<UpdateFasstoGoodsResponse>> updateGoods(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFasstoGoodsRequest> request
    ) {
        UpdateFasstoGoodsResult result = updateFasstoGoodsUseCase.updateGoods(
                FasstoGoodsRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFasstoGoodsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
