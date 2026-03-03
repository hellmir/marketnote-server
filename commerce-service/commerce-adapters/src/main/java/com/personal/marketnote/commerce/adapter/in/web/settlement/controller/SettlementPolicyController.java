package com.personal.marketnote.commerce.adapter.in.web.settlement.controller;

import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.DeleteSettlementPolicyApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.GetSettlementPoliciesApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.GetSettlementPolicyApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.RegisterSettlementPolicyApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs.UpdateSettlementPolicyApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.settlement.mapper.SettlementPolicyRequestToCommandMapper;
import com.personal.marketnote.commerce.adapter.in.web.settlement.request.RegisterSettlementPolicyRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.request.UpdateSettlementPolicyRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.response.GetSettlementPolicyResponse;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.DeleteSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.RegisterSettlementPolicyUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.UpdateSettlementPolicyUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 정산 정책 컨트롤러 (관리자 전용)
 *
 * @author 성효빈
 * @since 2026-03-02
 * @description 판매자별 정산 정책 CRUD API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/settlement-policies")
@Tag(name = "(관리자) 정산 정책 API", description = "관리자 정산 정책 관리 API")
@RequiredArgsConstructor
@Validated
public class SettlementPolicyController {
    private final RegisterSettlementPolicyUseCase registerSettlementPolicyUseCase;
    private final GetSettlementPolicyUseCase getSettlementPolicyUseCase;
    private final UpdateSettlementPolicyUseCase updateSettlementPolicyUseCase;
    private final DeleteSettlementPolicyUseCase deleteSettlementPolicyUseCase;

    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterSettlementPolicyApiDocs
    public ResponseEntity<BaseResponse<GetSettlementPolicyResponse>> registerPolicy(
            @Valid @RequestBody RegisterSettlementPolicyRequest request
    ) {
        GetSettlementPolicyResult result = registerSettlementPolicyUseCase.registerPolicy(
                SettlementPolicyRequestToCommandMapper.mapToRegisterCommand(request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementPolicyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 정책 등록 성공"
                ),
                HttpStatus.OK
        );
    }

    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetSettlementPoliciesApiDocs
    public ResponseEntity<BaseResponse<List<GetSettlementPolicyResponse>>> getAllPolicies() {
        List<GetSettlementPolicyResult> results = getSettlementPolicyUseCase.getAllPolicies();
        List<GetSettlementPolicyResponse> responses = results.stream()
                .map(GetSettlementPolicyResponse::from)
                .toList();

        return new ResponseEntity<>(
                BaseResponse.of(
                        responses,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 정책 전체 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetSettlementPolicyApiDocs
    public ResponseEntity<BaseResponse<GetSettlementPolicyResponse>> getPolicy(
            @PathVariable("id") Long id
    ) {
        GetSettlementPolicyResult result = getSettlementPolicyUseCase.getPolicy(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementPolicyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 정책 단건 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateSettlementPolicyApiDocs
    public ResponseEntity<BaseResponse<GetSettlementPolicyResponse>> updatePolicy(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateSettlementPolicyRequest request
    ) {
        GetSettlementPolicyResult result = updateSettlementPolicyUseCase.updatePolicy(
                SettlementPolicyRequestToCommandMapper.mapToUpdateCommand(id, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetSettlementPolicyResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 정책 수정 성공"
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @DeleteSettlementPolicyApiDocs
    public ResponseEntity<BaseResponse<Void>> deletePolicy(
            @PathVariable("id") Long id
    ) {
        deleteSettlementPolicyUseCase.deletePolicy(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "정산 정책 삭제 성공"
                ),
                HttpStatus.OK
        );
    }
}
