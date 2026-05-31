package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs.CancelInternalFulfillmentDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs.GetFulfillmentWorkStatusApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs.RegisterInternalReturnDeliveryApiDocs;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.request.CancelInternalFulfillmentDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.request.RegisterInternalReturnDeliveryRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.CancelInternalFulfillmentDeliveryResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.GetFulfillmentWorkStatusResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.RegisterInternalReturnDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.GetFulfillmentWorkStatusCommand;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryProductCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.GetFulfillmentWorkStatusResult;
import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.GetFulfillmentWorkStatusUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.RegisterInternalReturnDeliveryUseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final CancelInternalFulfillmentDeliveryUseCase cancelInternalFulfillmentDeliveryUseCase;
    private final RegisterInternalReturnDeliveryUseCase registerInternalReturnDeliveryUseCase;

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

    /**
     * 풀필먼트 출고 취소 (서비스 간 통신용)
     *
     * @param request 출고 취소 요청 (orderId)
     */
    @PostMapping("/cancel")
    @CancelInternalFulfillmentDeliveryApiDocs
    public ResponseEntity<BaseResponse<CancelInternalFulfillmentDeliveryResponse>> cancelDelivery(
            @Valid @RequestBody CancelInternalFulfillmentDeliveryRequest request
    ) {
        CancelInternalFulfillmentDeliveryResult result = cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(
                new CancelInternalFulfillmentDeliveryCommand(request.orderId())
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        CancelInternalFulfillmentDeliveryResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "풀필먼트 출고 취소 처리 완료"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 풀필먼트 반품 등록 (서비스 간 통신용)
     *
     * @param request 반품 등록 요청 (orderId, 회수지 정보, 반품 사유, 상품 목록)
     */
    @PostMapping("/return")
    @RegisterInternalReturnDeliveryApiDocs
    public ResponseEntity<BaseResponse<RegisterInternalReturnDeliveryResponse>> registerReturnDelivery(
            @Valid @RequestBody RegisterInternalReturnDeliveryRequest request
    ) {
        RegisterInternalReturnDeliveryCommand command = mapToCommand(request);
        RegisterInternalReturnDeliveryResult result = registerInternalReturnDeliveryUseCase.registerReturnDelivery(command);

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterInternalReturnDeliveryResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "풀필먼트 반품 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    private RegisterInternalReturnDeliveryCommand mapToCommand(RegisterInternalReturnDeliveryRequest request) {
        List<RegisterInternalReturnDeliveryProductCommand> products = mapProducts(request.products());

        return RegisterInternalReturnDeliveryCommand.builder()
                .orderId(request.orderId())
                .orderDate(request.orderDate())
                .recipientName(request.recipientName())
                .recipientPhoneNumber(request.recipientPhoneNumber())
                .recipientAddress(request.recipientAddress())
                .pickupRecipientName(request.pickupRecipientName())
                .pickupRecipientPhoneNumber(request.pickupRecipientPhoneNumber())
                .pickupZipCode(request.pickupZipCode())
                .pickupAddress(request.pickupAddress())
                .pickupAddressDetail(request.pickupAddressDetail())
                .returnReason(request.returnReason())
                .returnDetailReason(request.returnDetailReason())
                .returnShippingRequest(request.returnShippingRequest())
                .products(products)
                .build();
    }

    private List<RegisterInternalReturnDeliveryProductCommand> mapProducts(
            List<RegisterInternalReturnDeliveryRequest.ProductItem> items
    ) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream()
                .map(item -> RegisterInternalReturnDeliveryProductCommand.of(item.productCode(), item.quantity()))
                .toList();
    }
}
