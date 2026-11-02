package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.shipping.CarrierCode;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.TrackingNumber;
import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentDeliveryStatusInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.in.usecase.PollShippingStatusUseCase;
import com.personal.marketnote.fulfillment.port.out.event.PublishShippingStatusChangedEventPort;
import com.personal.marketnote.fulfillment.port.out.notification.SendDeliveryFailureSlackAlertPort;
import com.personal.marketnote.fulfillment.port.out.shipping.FindShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.shipping.UpdateShippingTrackerPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryStatusesPort;
import com.personal.marketnote.fulfillment.utility.FulfillmentAuthenticatedCaller;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@UseCase
public class PollShippingStatusService implements PollShippingStatusUseCase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String OUTBOUND_RELEASE_TYPE = "1";
    private static final int CHUNK_SIZE = 100;
    private static final long INITIAL_CURSOR = 0L;

    private final FindShippingTrackerPort findShippingTrackerPort;
    private final UpdateShippingTrackerPort updateShippingTrackerPort;
    private final FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;
    private final GetFulfillmentDeliveryStatusesPort getDeliveryStatusesPort;
    private final PublishShippingStatusChangedEventPort publishShippingStatusChangedEventPort;
    private final SendDeliveryFailureSlackAlertPort sendDeliveryFailureSlackAlertPort;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public PollShippingStatusService(
            FindShippingTrackerPort findShippingTrackerPort,
            UpdateShippingTrackerPort updateShippingTrackerPort,
            FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller,
            GetFulfillmentDeliveryStatusesPort getDeliveryStatusesPort,
            PublishShippingStatusChangedEventPort publishShippingStatusChangedEventPort,
            SendDeliveryFailureSlackAlertPort sendDeliveryFailureSlackAlertPort,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this.findShippingTrackerPort = findShippingTrackerPort;
        this.updateShippingTrackerPort = updateShippingTrackerPort;
        this.fulfillmentAuthenticatedCaller = fulfillmentAuthenticatedCaller;
        this.getDeliveryStatusesPort = getDeliveryStatusesPort;
        this.publishShippingStatusChangedEventPort = publishShippingStatusChangedEventPort;
        this.sendDeliveryFailureSlackAlertPort = sendDeliveryFailureSlackAlertPort;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setIsolationLevel(Isolation.READ_COMMITTED.value());
    }

    @Override
    public void pollShippingStatuses(PollShippingStatusCommand command) {
        Optional<LocalDateTime> minCreatedAt = findShippingTrackerPort.findMinCreatedAtOfPollingActive();
        if (minCreatedAt.isEmpty()) {
            logCompletion(0L);
            return;
        }

        Map<String, FulfillmentDeliveryStatusInfoResult> statusMap = fetchDeliveryStatusMap(
                command,
                minCreatedAt.get().toLocalDate()
        );
        LocalDateTime now = LocalDateTime.now(clock);

        long lastId = INITIAL_CURSOR;
        long totalProcessed = 0L;

        while (true) {
            List<ShippingTracker> chunk = findShippingTrackerPort.findPollingActiveChunk(lastId, CHUNK_SIZE);
            if (chunk.isEmpty()) {
                logCompletion(totalProcessed);
                return;
            }

            log.info("배송 상태 폴링 청크 처리 시작 - lastId: {}, size: {}", lastId, chunk.size());
            processChunk(chunk, statusMap, now);
            totalProcessed += chunk.size();
            lastId = chunk.get(chunk.size() - 1).getId();

            if (chunk.size() < CHUNK_SIZE) {
                logCompletion(totalProcessed);
                return;
            }
        }
    }

    private Map<String, FulfillmentDeliveryStatusInfoResult> fetchDeliveryStatusMap(
            PollShippingStatusCommand command,
            LocalDate startDate
    ) {
        LocalDate endDate = LocalDate.now(clock);

        GetFulfillmentDeliveryStatusesResult result = fulfillmentAuthenticatedCaller.call(token ->
                getDeliveryStatusesPort.getDeliveryStatuses(
                        GetFulfillmentDeliveryStatusesCommand.of(
                                command.customerCode(),
                                token,
                                startDate.format(DATE_FORMATTER),
                                endDate.format(DATE_FORMATTER),
                                OUTBOUND_RELEASE_TYPE
                        )
                )
        );

        return buildStatusMap(result);
    }

    private void processChunk(
            List<ShippingTracker> chunk,
            Map<String, FulfillmentDeliveryStatusInfoResult> statusMap,
            LocalDateTime now
    ) {
        for (ShippingTracker tracker : chunk) {
            try {
                transactionTemplate.executeWithoutResult(status -> processTracker(tracker, statusMap, now));
            } catch (Exception e) {
                log.error("배송 상태 폴링 처리 실패: orderId={}", tracker.getOrderId(), e);
            }
        }
    }

    private void logCompletion(long totalProcessed) {
        if (totalProcessed == 0L) {
            log.debug("폴링 대상 ShippingTracker가 없습니다.");
            return;
        }
        log.info("배송 상태 폴링 처리 완료 - 총 {}건", totalProcessed);
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
            publishStatusChanged(tracker);
            return;
        }
        if (newStatus.isShipping() && tracker.isShipping()) {
            applyTrackingInfoUpdate(tracker, deliveryStatus);
            return;
        }
        if (newStatus.isDelivered()) {
            tracker.completeDelivery();
            publishStatusChanged(tracker);
            return;
        }
        if (newStatus.isDeliveryFailed()) {
            tracker.markDeliveryFailed();
            publishStatusChanged(tracker);
            sendDeliveryFailureSlackAlertPort.sendDeliveryFailureAlert(
                    tracker.getOrderId(),
                    tracker.getTrackingNumberValue(),
                    tracker.getCarrierCodeValue(),
                    LocalDateTime.now(clock)
            );
        }
    }

    private void publishStatusChanged(ShippingTracker tracker) {
        publishShippingStatusChangedEventPort.publish(new ShippingStatusChangedEvent(
                tracker.getOrderId(),
                tracker.getBuyerId(),
                tracker.getShippingStatus().name(),
                tracker.getTrackingNumberValue(),
                tracker.getCarrierCodeValue(),
                LocalDateTime.now(clock),
                null,
                null
        ));
    }

    private void applyShippingTransition(ShippingTracker tracker, FulfillmentDeliveryStatusInfoResult deliveryStatus) {
        if (FormatValidator.hasValue(deliveryStatus.invoiceNumber())) {
            tracker.startShipping(
                    TrackingNumber.of(deliveryStatus.invoiceNumber()),
                    CarrierCode.of(deliveryStatus.courierCode())
            );
            return;
        }
        tracker.advanceToShipping();
    }

    private void applyTrackingInfoUpdate(ShippingTracker tracker, FulfillmentDeliveryStatusInfoResult deliveryStatus) {
        if (tracker.hasNoTrackingNumber() && FormatValidator.hasValue(deliveryStatus.invoiceNumber())) {
            tracker.updateTrackingInfo(
                    TrackingNumber.of(deliveryStatus.invoiceNumber()),
                    CarrierCode.of(deliveryStatus.courierCode())
            );
        }
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
