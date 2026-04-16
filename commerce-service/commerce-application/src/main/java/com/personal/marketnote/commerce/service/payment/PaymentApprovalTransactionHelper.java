package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentApprovalInfo;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.inventory.ReserveInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext;
import com.personal.marketnote.commerce.port.in.result.payment.ApprovePaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReserveInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.*;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.service.saga.OrderPaymentSagaStarter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * 결제 승인 트랜잭션 헬퍼
 *
 * <p>결제 승인 플로우의 각 단계를 독립 트랜잭션(REQUIRES_NEW)으로 분리하여
 * 실패/UNKNOWN 상태 반영이 RuntimeException throw에 의해 롤백되지 않도록 보장한다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentApprovalTransactionHelper {
    private final FindOrderPort findOrderPort;
    private final FindPaymentPort findPaymentPort;
    private final UpdatePaymentPort updatePaymentPort;
    private final FindPspPaymentEventPort findPspPaymentEventPort;
    private final SavePspPaymentEventPort savePspPaymentEventPort;
    private final UpdatePspPaymentEventPort updatePspPaymentEventPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final PublishPaymentEventPort publishPaymentEventPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final Optional<OrderPaymentSagaStarter> orderPaymentSagaStarter;

    /**
     * TX-1: 검증 + EXECUTING 상태 커밋
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public PaymentApprovalContext prepareExecution(ApprovePaymentCommand command) {
        UUID orderKey = UUID.fromString(command.orderKey());
        Payment payment = findPaymentPort.findByOrderKey(orderKey)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        Order order = findVerifiedOrder(payment.getOrderId(), command.buyerId());
        verifyPaymentAmount(order, payment);

        reserveInventory(order);

        PspPaymentEvent event = findPspPaymentEventPort.findByOrderKey(command.orderKey())
                .orElseThrow(() -> new PaymentEventNotFoundException(command.orderKey()));
        event.startExecution();
        updatePspPaymentEventPort.update(event);

        return PaymentApprovalContext.of(payment, event);
    }

    /**
     * TX-1: 빠른결제용 검증 + PspPaymentEvent 생성 + EXECUTING 상태 커밋
     *
     * <p>빠른결제에는 거래등록(ReadyPayment) 단계가 없어 PspPaymentEvent가 사전에 존재하지 않으므로,
     * 이 메서드에서 직접 생성한다.</p>
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public PaymentApprovalContext prepareExecutionForQuickPayment(
            Long buyerId, String orderKey, String pgCompanyKey, String pgShopKey
    ) {
        UUID orderKeyUuid = UUID.fromString(orderKey);
        Payment payment = findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(orderKey));

        Order order = findVerifiedOrder(payment.getOrderId(), buyerId);
        verifyPaymentAmount(order, payment);

        findPspPaymentEventPort.findByOrderKey(orderKey)
                .filter(PspPaymentEvent::isUnresolved)
                .ifPresent(event -> {
                    log.warn("빠른결제 중복 결제 시도 - orderKey: {}, 기존 이벤트 상태: {}", orderKey, event.getPoStatus());
                    throw new DuplicatePaymentReadyException(orderKey);
                });

        reserveInventory(order);

        PspPaymentEvent event = PspPaymentEvent.createReady(payment, pgCompanyKey, pgShopKey, "PACA");
        event.startExecution();
        PspPaymentEvent savedEvent = savePspPaymentEventPort.save(event);

        return PaymentApprovalContext.of(payment, savedEvent);
    }

    /**
     * TX-2: 성공 상태 커밋 (COMPLETE + PAID + 분개)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public ApprovePaymentResult commitSuccess(
            PaymentApprovalContext context, PaymentApprovalVendorResult vendorResult, Short installment
    ) {
        Payment payment = context.payment();
        PspPaymentEvent event = context.event();

        payment.markAsSuccess(vendorResult.transactionId());
        updatePaymentPort.update(payment);

        PaymentApprovalInfo approvalInfo = PaymentApprovalInfo.builder()
                .pgPaymentKey(vendorResult.transactionId())
                .method(vendorResult.payMethod())
                .cardNumber(vendorResult.cardNumber())
                .approvalNumber(vendorResult.approvalNumber())
                .installment(installment)
                .issueCompanyCode(vendorResult.cardCode())
                .issueCompanyName(vendorResult.cardName())
                .resultCode(vendorResult.resultCode())
                .resultMessage(vendorResult.resultMessage())
                .pgApprovalResult(vendorResult.rawResponse())
                .appTime(vendorResult.approvalTime())
                .build();
        event.completeWithApproval(approvalInfo);
        updatePspPaymentEventPort.update(event);

        Order order = findOrderPort.findById(payment.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(payment.getOrderId()));

        if (orderPaymentSagaStarter.isPresent()) {
            changeOrderStatusUseCase.changeOrderStatus(
                    ChangeOrderStatusCommand.builder()
                            .id(payment.getOrderId())
                            .orderStatus(OrderStatus.PAID)
                            .skipSubsequentProcesses(true)
                            .build()
            );
            startOrderPaymentSaga(order, payment);
            return buildApprovePaymentResult(payment, vendorResult);
        }

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.PAID)
                        .build()
        );

        // [#929][#1033] 결제 승인 분개는 Kafka Consumer(PaymentApprovedLedgerConsumer)로 전환 완료
        publishPaymentApprovedEvent(payment);

        return buildApprovePaymentResult(payment, vendorResult);
    }

    private ApprovePaymentResult buildApprovePaymentResult(Payment payment,
                                                           PaymentApprovalVendorResult vendorResult) {
        return ApprovePaymentResult.builder()
                .orderId(payment.getOrderId())
                .orderKey(payment.getOrderKey().toString())
                .pgPaymentKey(vendorResult.transactionId())
                .amount(payment.getPaymentAmount())
                .resultCode(vendorResult.resultCode())
                .resultMessage(vendorResult.resultMessage())
                .payMethod(vendorResult.payMethod())
                .build();
    }

    /**
     * TX-2: 실패 상태 커밋 (FAILED + order FAILED)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitFailure(PaymentApprovalContext context, String resultCode, String resultMessage) {
        Payment payment = context.payment();
        PspPaymentEvent event = context.event();

        payment.markAsFailed();
        updatePaymentPort.update(payment);

        event.failExecution(resultCode, resultMessage);
        updatePspPaymentEventPort.update(event);

        changeOrderStatusUseCase.changeOrderStatus(
                ChangeOrderStatusCommand.builder()
                        .id(payment.getOrderId())
                        .orderStatus(OrderStatus.FAILED)
                        .build()
        );

        publishPaymentFailedEvent(payment, resultCode, resultMessage);
    }

    /**
     * TX-2: UNKNOWN 상태 커밋 (이벤트만 UNKNOWN, payment/order 변경 없음)
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitUnknown(PaymentApprovalContext context, String resultCode, String resultMessage) {
        PspPaymentEvent event = context.event();

        event.markUnknown(resultCode, resultMessage);
        updatePspPaymentEventPort.update(event);
    }

    private void reserveInventory(Order order) {
        List<ReserveInventoryCommand.OrderProductItem> orderProductItems = order.getOrderProducts().stream()
                .map(op -> ReserveInventoryCommand.OrderProductItem.builder()
                        .pricePolicyId(op.getPricePolicyId())
                        .quantity(op.getQuantity())
                        .build())
                .toList();

        reserveInventoryUseCase.reserveInventory(
                ReserveInventoryCommand.builder()
                        .orderId(order.getId())
                        .orderProducts(orderProductItems)
                        .build()
        );
    }

    private void verifyPaymentAmount(Order order, Payment payment) {
        Long couponAmount = FormatValidator.hasValue(order.getAmount().getCouponAmount())
                ? order.getAmount().getCouponAmount()
                : 0L;
        Long pointAmount = FormatValidator.hasValue(order.getAmount().getPointAmount())
                ? order.getAmount().getPointAmount()
                : 0L;
        Long expectedAmount = Math.subtractExact(
                Math.subtractExact(order.getAmount().getTotalAmount(), couponAmount),
                pointAmount
        );

        if (FormatValidator.notEquals(expectedAmount, payment.getPaymentAmount())) {
            log.error("결제 금액 불일치: orderId={}, 주문금액={}, 쿠폰={}, 포인트={}, 예상결제금액={}, 실제결제금액={}",
                    order.getId(), order.getAmount().getTotalAmount(), couponAmount, pointAmount,
                    expectedAmount, payment.getPaymentAmount());
            throw new PaymentAmountMismatchException(expectedAmount, payment.getPaymentAmount());
        }
    }

    private Order findVerifiedOrder(Long orderId, Long buyerId) {
        Order order = findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.isBuyer(buyerId)) {
            log.warn("결제 승인 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    orderId, order.getBuyerId(), buyerId);
            throw new UnauthorizedOrderAccessException();
        }
        return order;
    }

    private void startOrderPaymentSaga(Order order, Payment payment) {
        List<OrderProduct> orderProducts = order.getOrderProducts();
        List<Long> pricePolicyIds = orderProducts.stream()
                .map(OrderProduct::getPricePolicyId)
                .toList();
        Long totalAccumulatedPoint = calculateTotalAccumulatedPoint(orderProducts, pricePolicyIds);

        List<OrderPaymentSagaContext.OrderProductItem> sagaOrderProducts = orderProducts.stream()
                .map(op -> new OrderPaymentSagaContext.OrderProductItem(
                        op.getPricePolicyId(), op.getSharerKey(), op.getQuantity(), op.getUnitAmount()))
                .toList();

        Long pointAmount = FormatValidator.hasValue(order.getAmount().getPointAmount())
                ? order.getAmount().getPointAmount()
                : 0L;

        OrderPaymentSagaContext sagaContext = new OrderPaymentSagaContext(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                payment.getPaymentAmount(),
                order.getAmount().getTotalAmount(),
                pointAmount,
                totalAccumulatedPoint,
                sagaOrderProducts
        );

        orderPaymentSagaStarter.get().start(sagaContext);
    }

    private Long calculateTotalAccumulatedPoint(List<OrderProduct> orderProducts,
                                                List<Long> pricePolicyIds) {
        Map<Long, ProductInfoResult> productInfoMap = findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);

        long totalAccumulatedPoint = 0L;
        for (OrderProduct orderProduct : orderProducts) {
            ProductInfoResult productInfo = productInfoMap.get(orderProduct.getPricePolicyId());
            if (FormatValidator.hasNoValue(productInfo)) {
                continue;
            }
            if (FormatValidator.hasNoValue(productInfo.accumulatedPoint())) {
                continue;
            }
            totalAccumulatedPoint = Math.addExact(totalAccumulatedPoint,
                    Math.multiplyExact(productInfo.accumulatedPoint(), orderProduct.getQuantity()));
        }

        return totalAccumulatedPoint;
    }

    private void publishPaymentApprovedEvent(Payment payment) {
        try {
            publishPaymentEventPort.publishPaymentApprovedEvent(
                    payment.getOrderId(),
                    payment.getOrderKey().toString(),
                    payment.getPaymentAmount()
            );
        } catch (Exception e) {
            log.error("결제 승인 이벤트 발행 실패 - orderId: {}, error: {}", payment.getOrderId(), e.getMessage(), e);
        }
    }

    private void publishPaymentFailedEvent(Payment payment, String resultCode, String resultMessage) {
        try {
            publishPaymentEventPort.publishPaymentFailedEvent(
                    payment.getOrderId(),
                    payment.getOrderKey().toString(),
                    resultCode,
                    resultMessage
            );
        } catch (Exception e) {
            log.error("결제 실패 이벤트 발행 실패 - orderId: {}, error: {}", payment.getOrderId(), e.getMessage(), e);
        }
    }

}
