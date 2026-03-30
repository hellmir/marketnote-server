package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs.*;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper.FulfillmentWarehousingRequestToCommandMapper;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentWarehousingRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentWarehousingRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.*;
import com.personal.marketnote.fulfillment.port.in.result.vendor.*;
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
 * 파스토 상품 입고 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 파스토 상품 입고 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/vendors/fassto/warehousing")
@Tag(name = "파스토 상품 입고 API", description = "파스토 상품 입고 관련 API")
@RequiredArgsConstructor
public class FulfillmentWarehousingController {
    private final RegisterFulfillmentWarehousingUseCase registerFulfillmentWarehousingUseCase;
    private final GetFulfillmentWarehousingUseCase getFulfillmentWarehousingUseCase;
    private final GetFulfillmentWarehousingDetailUseCase getFulfillmentWarehousingDetailUseCase;
    private final GetFulfillmentWarehousingInspecDetailUseCase getFulfillmentWarehousingInspecDetailUseCase;
    private final GetFulfillmentWarehousingAbnormalUseCase getFulfillmentWarehousingAbnormalUseCase;
    private final GetFulfillmentWarehousingAbnormalImageUseCase getFulfillmentWarehousingAbnormalImageUseCase;
    private final UpdateFulfillmentWarehousingUseCase updateFulfillmentWarehousingUseCase;

    /**
     * (관리자) 파스토 상품 입고 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      입고 요청 정보
     * @Author 성효빈
     * @Date 2026-01-31
     * @Description 파스토 상품 입고 요청을 등록합니다.
     */
    @PostMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterFulfillmentWarehousingApiDocs
    public ResponseEntity<BaseResponse<RegisterFulfillmentWarehousingResponse>> registerWarehousing(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<RegisterFulfillmentWarehousingRequest> request
    ) {
        RegisterFulfillmentWarehousingResult result = registerFulfillmentWarehousingUseCase.registerWarehousing(
                FulfillmentWarehousingRequestToCommandMapper.mapToRegisterCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterFulfillmentWarehousingResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 입고 요청 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 파스토 상품 입고 목록 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param startDate    조회 시작일(YYYYMMDD)
     * @param endDate      조회 종료일(YYYYMMDD)
     * @param inWay        입고방법(비어있으면:전체,01:택배,02:차량)
     * @param ordNo        주문번호
     * @param wrkStat      작업상태(비어있으면:전체,1:입고요청,2:센터도착,3:입고검수,4:입고확정,5:입고완료)
     * @Author 성효빈
     * @Date 2026-02-03
     * @Description 파스토 상품 입고 목록을 조회합니다.
     */
    @GetMapping("/{customerCode}/{startDate}/{endDate}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentWarehousingApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentWarehousingResponse>> getWarehousing(
            @PathVariable String customerCode,
            @PathVariable String startDate,
            @PathVariable String endDate,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String inWay,
            @RequestParam(required = false) String ordNo,
            @RequestParam(required = false) String wrkStat
    ) {
        GetFulfillmentWarehousingResult result = getFulfillmentWarehousingUseCase.getWarehousing(
                FulfillmentWarehousingRequestToCommandMapper.mapToWarehousingQuery(
                        customerCode,
                        accessToken,
                        startDate,
                        endDate,
                        inWay,
                        ordNo,
                        wrkStat
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentWarehousingResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 입고 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 상품 입고 상세 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param slipNo       파스토 입고요청번호
     * @param accessToken  파스토 액세스 토큰
     * @param ordNo        주문번호
     * @Author 성효빈
     * @Date 2026-02-14
     * @Description 파스토 상품 입고 상세 정보를 조회합니다.
     */
    @GetMapping("/detail/{customerCode}/{slipNo}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentWarehousingDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentWarehousingDetailResponse>> getWarehousingDetail(
            @PathVariable String customerCode,
            @PathVariable String slipNo,
            @RequestHeader("accessToken") String accessToken,
            @RequestParam(required = false) String ordNo
    ) {
        GetFulfillmentWarehousingDetailResult result = getFulfillmentWarehousingDetailUseCase.getWarehousingDetail(
                FulfillmentWarehousingRequestToCommandMapper.mapToWarehousingDetailCommand(
                        customerCode,
                        accessToken,
                        slipNo,
                        ordNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentWarehousingDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 입고 상세 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 입고 검수 상세 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param slipNo       파스토 입고요청번호
     * @param whCd         센터
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-02-17
     * @Description 파스토 입고 검수 상세 정보를 조회합니다.
     */
    @GetMapping("/inspec/{customerCode}/{slipNo}/{whCd}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentWarehousingInspecDetailApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentWarehousingInspecDetailResponse>> getWarehousingInspecDetail(
            @PathVariable String customerCode,
            @PathVariable String slipNo,
            @PathVariable String whCd,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentWarehousingInspecDetailResult result = getFulfillmentWarehousingInspecDetailUseCase.getWarehousingInspecDetail(
                FulfillmentWarehousingRequestToCommandMapper.mapToWarehousingInspecDetailCommand(
                        customerCode,
                        accessToken,
                        slipNo,
                        whCd
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentWarehousingInspecDetailResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 입고 검수 상세 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 비정상 입고 상품 정보 조회
     *
     * @param customerCode 파스토 고객사 코드
     * @param whCd         센터
     * @param slipNo       파스토 입고요청번호
     * @param accessToken  파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-02-14
     * @Description 파스토 비정상 입고 상품 정보를 조회합니다.
     */
    @GetMapping("/abnormal/{customerCode}/{whCd}/{slipNo}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentWarehousingAbnormalApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentWarehousingAbnormalResponse>> getWarehousingAbnormal(
            @PathVariable String customerCode,
            @PathVariable String whCd,
            @PathVariable String slipNo,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentWarehousingAbnormalResult result = getFulfillmentWarehousingAbnormalUseCase.getWarehousingAbnormal(
                FulfillmentWarehousingRequestToCommandMapper.mapToWarehousingAbnormalCommand(
                        customerCode,
                        accessToken,
                        whCd,
                        slipNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentWarehousingAbnormalResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 비정상 입고 상품 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 비정상 입고 상품 이미지 조회
     *
     * @param slipNo        입고요청번호
     * @param godCd         상품코드
     * @param goodsSerialNo 상품일련번호
     * @param fileSeq       파일seq
     * @param imgNo         이미지순서
     * @param accessToken   파스토 액세스 토큰
     * @Author 성효빈
     * @Date 2026-02-17
     * @Description 파스토 비정상 입고 상품 이미지를 조회합니다.
     */
    @GetMapping("/abnormal/image/{slipNo}/{godCd}/{goodsSerialNo}/{fileSeq}/{imgNo}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetFulfillmentWarehousingAbnormalImageApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentWarehousingAbnormalImageResponse>> getWarehousingAbnormalImage(
            @PathVariable String slipNo,
            @PathVariable String godCd,
            @PathVariable String goodsSerialNo,
            @PathVariable String fileSeq,
            @PathVariable String imgNo,
            @RequestHeader("accessToken") String accessToken
    ) {
        GetFulfillmentWarehousingAbnormalImageResult result = getFulfillmentWarehousingAbnormalImageUseCase.getWarehousingAbnormalImage(
                FulfillmentWarehousingRequestToCommandMapper.mapToWarehousingAbnormalImageCommand(
                        accessToken,
                        slipNo,
                        godCd,
                        goodsSerialNo,
                        fileSeq,
                        imgNo
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentWarehousingAbnormalImageResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 비정상 입고 상품 이미지 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 파스토 상품 입고 수정 요청
     *
     * @param customerCode 파스토 고객사 코드
     * @param accessToken  파스토 액세스 토큰
     * @param request      입고 수정 요청 정보
     * @Author 성효빈
     * @Date 2026-02-05
     * @Description 파스토 상품 입고 요청을 수정합니다.
     */
    @PutMapping("/{customerCode}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateFulfillmentWarehousingApiDocs
    public ResponseEntity<BaseResponse<UpdateFulfillmentWarehousingResponse>> updateWarehousing(
            @PathVariable String customerCode,
            @RequestHeader("accessToken") String accessToken,
            @Valid @RequestBody List<UpdateFulfillmentWarehousingRequest> request
    ) {
        UpdateFulfillmentWarehousingResult result = updateFulfillmentWarehousingUseCase.updateWarehousing(
                FulfillmentWarehousingRequestToCommandMapper.mapToUpdateCommand(customerCode, accessToken, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateFulfillmentWarehousingResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파스토 상품 입고 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
