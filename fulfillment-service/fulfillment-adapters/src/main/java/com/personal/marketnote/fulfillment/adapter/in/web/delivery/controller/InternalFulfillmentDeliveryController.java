package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs.GetFulfillmentWorkStatusApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.GetFulfillmentWorkStatusResponse;
import com.personal.marketnote.fulfillment.port.in.command.GetFulfillmentWorkStatusCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;
import com.personal.marketnote.fulfillment.port.in.usecase.GetFulfillmentWorkStatusUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

/**
 * 내부 풀필먼트 출고 컨트롤러 (서비스 간 통신용)
 */
@RestController
@RequestMapping("/api/v1/internal/fulfillment/deliveries")
@Tag(
        name = "내부 풀필먼트 출고 API",
        description = "서비스 간 통신용 풀필먼트 출고 API"
)
@RequiredArgsConstructor
public class InternalFulfillmentDeliveryController {
    private final GetFulfillmentWorkStatusUseCase getFulfillmentWorkStatusUseCase;

    /**
     * 풀필먼트 작업 상태 조회 (서비스 간 통신용)
     *
     * @param orderId 주문 ID
     */
    @GetMapping("/work-status")
    @GetFulfillmentWorkStatusApiDocs
    public ResponseEntity<BaseResponse<GetFulfillmentWorkStatusResponse>> getWorkStatus(
            @RequestParam("order-id") Long orderId
    ) {
        GetFulfillmentWorkStatusResult result = getFulfillmentWorkStatusUseCase.getWorkStatus(
                new GetFulfillmentWorkStatusCommand(orderId)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetFulfillmentWorkStatusResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "풀필먼트 작업 상태 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
