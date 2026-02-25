package com.personal.marketnote.community.adapter.in.web.like.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.community.adapter.in.web.like.controller.apidocs.UpsertLikeApiDocs;
import com.personal.marketnote.community.adapter.in.web.like.mapper.LikeRequestToCommandMapper;
import com.personal.marketnote.community.adapter.in.web.like.request.UpsertLikeRequest;
import com.personal.marketnote.community.adapter.in.web.like.response.UpsertLikeResponse;
import com.personal.marketnote.community.port.in.result.like.UpsertLikeResult;
import com.personal.marketnote.community.port.in.usecase.like.UpsertLikeUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 좋아요 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-10
 * @Description 좋아요 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/likes")
@Tag(name = "좋아요 API", description = "좋아요 관련 API")
@RequiredArgsConstructor
public class LikeController {
    private final UpsertLikeUseCase upsertLikeUseCase;

    /**
     * 좋아요 등록/취소
     *
     * @param request   좋아요 등록/취소 요청
     * @param principal 인증된 사용자 정보
     * @Author 성효빈
     * @Date 2026-01-10
     * @Description 대상 도메인에 좋아요를 등록/취소합니다.
     */
    @PutMapping
    @UpsertLikeApiDocs
    public ResponseEntity<BaseResponse<UpsertLikeResponse>> upsertLike(
            @Valid @RequestBody UpsertLikeRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        UpsertLikeResult result = upsertLikeUseCase.upsertLike(
                LikeRequestToCommandMapper.mapToCommand(request, ElementExtractor.extractUserId(principal))
        );

        if (result.isNew()) {
            return new ResponseEntity<>(
                    BaseResponse.of(
                            UpsertLikeResponse.from(result),
                            HttpStatus.CREATED,
                            DEFAULT_SUCCESS_CODE,
                            "좋아요 등록 성공"
                    ),
                    HttpStatus.CREATED
            );
        }

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpsertLikeResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "좋아요 활성화/비활성화 성공"
                ),
                HttpStatus.OK
        );
    }
}
