package com.personal.marketnote.notification.adapter.in.web.template.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.template.controller.apidocs.*;
import com.personal.marketnote.notification.adapter.in.web.template.request.RegisterNotificationTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.template.request.UpdateNotificationTemplateRequest;
import com.personal.marketnote.notification.adapter.in.web.template.response.GetNotificationTemplateResponse;
import com.personal.marketnote.notification.adapter.in.web.template.response.RegisterNotificationTemplateResponse;
import com.personal.marketnote.notification.adapter.in.web.template.response.UpdateNotificationTemplateResponse;
import com.personal.marketnote.notification.port.in.command.RegisterNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.command.UpdateNotificationTemplateCommand;
import com.personal.marketnote.notification.port.in.result.template.GetNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.result.template.RegisterNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.result.template.UpdateNotificationTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.template.DeleteNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.template.GetNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.template.RegisterNotificationTemplateUseCase;
import com.personal.marketnote.notification.port.in.usecase.template.UpdateNotificationTemplateUseCase;
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
 * 알림 템플릿 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-06-03
 * @Description 관리자용 알림 템플릿 CRUD API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/notification-templates")
@Tag(name = "알림 템플릿 API", description = "관리자용 알림 템플릿 관리 API")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final RegisterNotificationTemplateUseCase registerNotificationTemplateUseCase;
    private final GetNotificationTemplateUseCase getNotificationTemplateUseCase;
    private final UpdateNotificationTemplateUseCase updateNotificationTemplateUseCase;
    private final DeleteNotificationTemplateUseCase deleteNotificationTemplateUseCase;

    /**
     * (관리자) 알림 템플릿 등록
     *
     * @param request 알림 템플릿 등록 요청
     * @return 알림 템플릿 등록 응답 {@link RegisterNotificationTemplateResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 알림 템플릿을 등록합니다.
     */
    @PostMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @RegisterNotificationTemplateApiDocs
    public ResponseEntity<BaseResponse<RegisterNotificationTemplateResponse>> registerNotificationTemplate(
            @Valid @RequestBody RegisterNotificationTemplateRequest request
    ) {
        RegisterNotificationTemplateResult result = registerNotificationTemplateUseCase.registerNotificationTemplate(
                new RegisterNotificationTemplateCommand(
                        request.templateCode(),
                        request.notificationType().name(),
                        request.notificationCategory().name(),
                        request.title(),
                        request.bodyTemplate(),
                        request.urlTemplate()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterNotificationTemplateResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "알림 템플릿 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * (관리자) 알림 템플릿 전체 조회
     *
     * @return 알림 템플릿 목록 응답 {@link GetNotificationTemplateResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 활성화된 알림 템플릿 전체를 조회합니다.
     */
    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @GetNotificationTemplatesApiDocs
    public ResponseEntity<BaseResponse<List<GetNotificationTemplateResponse>>> getNotificationTemplates() {
        List<GetNotificationTemplateResult> results = getNotificationTemplateUseCase.getNotificationTemplates();

        List<GetNotificationTemplateResponse> responses = results.stream()
                .map(GetNotificationTemplateResponse::from)
                .toList();

        return new ResponseEntity<>(
                BaseResponse.of(
                        responses,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 템플릿 전체 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 알림 템플릿 단건 조회
     *
     * @param id 알림 템플릿 ID
     * @return 알림 템플릿 조회 응답 {@link GetNotificationTemplateResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 알림 템플릿을 단건 조회합니다.
     */
    @GetMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetNotificationTemplateApiDocs
    public ResponseEntity<BaseResponse<GetNotificationTemplateResponse>> getNotificationTemplate(
            @PathVariable Long id
    ) {
        GetNotificationTemplateResult result = getNotificationTemplateUseCase.getNotificationTemplate(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetNotificationTemplateResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 템플릿 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 알림 템플릿 수정
     *
     * @param id      알림 템플릿 ID
     * @param request 알림 템플릿 수정 요청
     * @return 알림 템플릿 수정 응답 {@link UpdateNotificationTemplateResponse}
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 알림 템플릿의 제목, 본문 템플릿, URL 템플릿을 수정합니다.
     */
    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdateNotificationTemplateApiDocs
    public ResponseEntity<BaseResponse<UpdateNotificationTemplateResponse>> updateNotificationTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationTemplateRequest request
    ) {
        UpdateNotificationTemplateResult result = updateNotificationTemplateUseCase.updateNotificationTemplate(
                id,
                new UpdateNotificationTemplateCommand(
                        request.title(),
                        request.bodyTemplate(),
                        request.urlTemplate()
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        UpdateNotificationTemplateResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 템플릿 수정 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 알림 템플릿 삭제 (비활성화)
     *
     * @param id 알림 템플릿 ID
     * @return 삭제 완료 응답
     * @Author 성효빈
     * @Date 2026-06-03
     * @Description 알림 템플릿을 비활성화합니다. 실제로 데이터가 삭제되지 않습니다.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @DeleteNotificationTemplateApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteNotificationTemplate(
            @PathVariable Long id
    ) {
        deleteNotificationTemplateUseCase.deleteNotificationTemplate(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "알림 템플릿 삭제 성공"
                ),
                HttpStatus.OK
        );
    }
}
