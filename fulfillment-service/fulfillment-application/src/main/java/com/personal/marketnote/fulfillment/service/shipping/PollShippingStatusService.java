package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryStatusInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.in.usecase.PollShippingStatusUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryStatusesPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@UseCase
public class PollShippingStatusService implements PollShippingStatusUseCase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String OUTBOUND_RELEASE_TYPE = "1";

    private final FindShippingTrackerPort findShippingTrackerPort;
    private final UpdateShippingTrackerPort updateShippingTrackerPort;
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    private final GetFulfillmentDeliveryStatusesPort getDeliveryStatusesPort;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public PollShippingStatusService(
            FindShippingTrackerPort findShippingTrackerPort,
            UpdateShippingTrackerPort updateShippingTrackerPort,
            RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase,
            GetFulfillmentDeliveryStatusesPort getDeliveryStatusesPort,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this.findShippingTrackerPort = findShippingTrackerPort;
        this.updateShippingTrackerPort = updateShippingTrackerPort;
        this.requestFulfillmentAuthUseCase = requestFulfillmentAuthUseCase;
        this.getDeliveryStatusesPort = getDeliveryStatusesPort;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setIsolationLevel(Isolation.READ_COMMITTED.value());
    }

    @Override
    public void pollShippingStatuses(PollShippingStatusCommand command) {
        List<ShippingTracker> activeTrackers = findShippingTrackerPort.findAllPollingActive();
        if (activeTrackers.isEmpty()) {
            log.debug("폴링 대상 ShippingTracker가 없습니다.");
            return;
        }

        FulfillmentAccessToken token = requestFulfillmentAuthUseCase.requestAccessToken();

        LocalDate startDate = resolveStartDate(activeTrackers);
        LocalDate endDate = LocalDate.now(clock);

        GetFulfillmentDeliveryStatusesResult result = getDeliveryStatusesPort.getDeliveryStatuses(
                GetFulfillmentDeliveryStatusesCommand.of(
                        command.customerCode(),
                        token.getValue(),
                        startDate.format(DATE_FORMATTER),
                        endDate.format(DATE_FORMATTER),
                        OUTBOUND_RELEASE_TYPE
                )
        );

        Map<String, FulfillmentDeliveryStatusInfoResult> statusMap = buildStatusMap(result);
        LocalDateTime now = LocalDateTime.now(clock);

        for (ShippingTracker tracker : activeTrackers) {
            try {
                transactionTemplate.executeWithoutResult(status -> processTracker(tracker, statusMap, now));
            } catch (Exception e) {
                log.error("배송 상태 폴링 처리 실패: orderId={}", tracker.getOrderId(), e);
            }
        }
    }

    private void processTracker(
            ShippingTracker tracker,
            Map<String, FulfillmentDeliveryStatusInfoResult> statusMap,
            LocalDateTime now
    ) {
        ShippingTracker reloaded = findShippingTrackerPort.findByOrderId(tracker.getOrderId())
                .orElse(tracker);
        if (!reloaded.isPollingActive()) {
            return;
        }

        String ordNo = String.valueOf(reloaded.getOrderId());
        FulfillmentDeliveryStatusInfoResult deliveryStatus = statusMap.get(ordNo);

        if (FormatValidator.hasNoValue(deliveryStatus)) {
            reloaded.updateLastPolledAt(now);
            updateShippingTrackerPort.update(reloaded);
            return;
        }

        ShippingStatus newStatus = FasstoCargoStatusMapper.toShippingStatus(deliveryStatus.cargoStatusName());

        if (isSameStatusWithNoTrackingUpdate(reloaded, newStatus, deliveryStatus)) {
            reloaded.updateLastPolledAt(now);
            updateShippingTrackerPort.update(reloaded);
            return;
        }

        applyStatusTransition(reloaded, newStatus, deliveryStatus);
        reloaded.updateLastPolledAt(now);
        updateShippingTrackerPort.update(reloaded);
    }

    private boolean isSameStatusWithNoTrackingUpdate(
            ShippingTracker tracker,
            ShippingStatus newStatus,
            FulfillmentDeliveryStatusInfoResult deliveryStatus
    ) {
        if (!tracker.hasStatus(newStatus)) {
            return false;
        }
        if (tracker.isShipping()
                && tracker.hasNoTrackingNumber()
                && FormatValidator.hasValue(deliveryStatus.invoiceNumber())) {
            return false;
        }
        return true;
    }

    private void applyStatusTransition(
            ShippingTracker tracker,
            ShippingStatus newStatus,
            FulfillmentDeliveryStatusInfoResult deliveryStatus
    ) {
        if (newStatus.isShipping() && tracker.isPreparing()) {
            applyShippingTransition(tracker, deliveryStatus);
            return;
        }
        if (newStatus.isShipping() && tracker.isShipping()) {
            applyTrackingInfoUpdate(tracker, deliveryStatus);
            return;
        }
        if (newStatus.isDelivered()) {
            tracker.completeDelivery();
            return;
        }
        if (newStatus.isDeliveryFailed()) {
            tracker.markDeliveryFailed();
        }
    }

    private void applyShippingTransition(ShippingTracker tracker, FulfillmentDeliveryStatusInfoResult deliveryStatus) {
        if (FormatValidator.hasValue(deliveryStatus.invoiceNumber())) {
            tracker.startShipping(deliveryStatus.invoiceNumber(), deliveryStatus.courierCode());
            return;
        }
        tracker.advanceToShipping();
    }

    private void applyTrackingInfoUpdate(ShippingTracker tracker, FulfillmentDeliveryStatusInfoResult deliveryStatus) {
        if (tracker.hasNoTrackingNumber() && FormatValidator.hasValue(deliveryStatus.invoiceNumber())) {
            tracker.updateTrackingInfo(deliveryStatus.invoiceNumber(), deliveryStatus.courierCode());
        }
    }

    private LocalDate resolveStartDate(List<ShippingTracker> trackers) {
        return trackers.stream()
                .map(ShippingTracker::getCreatedAt)
                .filter(FormatValidator::hasValue)
                .min(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.now(clock));
    }

    private Map<String, FulfillmentDeliveryStatusInfoResult> buildStatusMap(GetFulfillmentDeliveryStatusesResult result) {
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.deliveryStatuses())) {
            return Map.of();
        }
        return result.deliveryStatuses().stream()
                .filter(status -> FormatValidator.hasValue(status.orderNumber()))
                .collect(Collectors.toMap(
                        FulfillmentDeliveryStatusInfoResult::orderNumber,
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));
    }
}
