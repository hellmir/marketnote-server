package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementCreateState;
import com.personal.marketnote.commerce.exception.InvalidFeeRateException;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
import com.personal.marketnote.commerce.exception.SettlementAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ExecuteSettlementUseCase;
import com.personal.marketnote.commerce.port.out.settlement.*;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class ExecuteSettlementService implements ExecuteSettlementUseCase {
    private static final int BASIS_POINT_DENOMINATOR = 10000;

    private final FindPaymentAllocationPort findPaymentAllocationPort;
    private final FindSettlementPort findSettlementPort;
    private final SaveSettlementPort saveSettlementPort;
    private final UpdateSettlementPort updateSettlementPort;
    private final UpdatePaymentAllocationPort updatePaymentAllocationPort;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void executeSettlement(ExecuteSettlementCommand command) {
        validateFeeRates(command);

        List<PaymentAllocation> unsettledAllocations = findPaymentAllocationPort.findUnsettledAllocations(
                command.year(), command.month());

        if (unsettledAllocations.isEmpty()) {
            throw new NoUnsettledAllocationException();
        }

        Map<Long, List<PaymentAllocation>> allocationsBySeller = unsettledAllocations.stream()
                .collect(Collectors.groupingBy(PaymentAllocation::getSellerId));

        for (Map.Entry<Long, List<PaymentAllocation>> entry : allocationsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<PaymentAllocation> sellerAllocations = entry.getValue();

            processSellerSettlement(command, sellerId, sellerAllocations);
        }
    }

    private void validateFeeRates(ExecuteSettlementCommand command) {
        if (FormatValidator.hasNoValue(command.pgFeeRate()) || command.pgFeeRate() < 0) {
            throw new InvalidFeeRateException("PG 수수료율은 0 이상이어야 합니다. pgFeeRate=" + command.pgFeeRate());
        }
        if (FormatValidator.hasNoValue(command.platformFeeRate()) || command.platformFeeRate() < 0) {
            throw new InvalidFeeRateException("플랫폼 수수료율은 0 이상이어야 합니다. platformFeeRate=" + command.platformFeeRate());
        }
        if (command.pgFeeRate() + command.platformFeeRate() > BASIS_POINT_DENOMINATOR) {
            throw new InvalidFeeRateException("수수료율 합계가 100%를 초과합니다. pgFeeRate=" + command.pgFeeRate()
                    + ", platformFeeRate=" + command.platformFeeRate());
        }
    }

    private void processSellerSettlement(ExecuteSettlementCommand command, Long sellerId,
                                          List<PaymentAllocation> sellerAllocations) {
        Integer year = command.year();
        Integer month = command.month();

        if (findSettlementPort.existsBySellerIdAndYearAndMonth(sellerId, year, month)) {
            throw new SettlementAlreadyExistsException(sellerId, year, month);
        }

        long totalAllocatedAmount = sellerAllocations.stream()
                .mapToLong(PaymentAllocation::getAllocatedAmount)
                .sum();

        long pgFeeAmount = Math.multiplyExact(totalAllocatedAmount, command.pgFeeRate()) / BASIS_POINT_DENOMINATOR;
        long platformFeeAmount = Math.multiplyExact(totalAllocatedAmount, command.platformFeeRate()) / BASIS_POINT_DENOMINATOR;
        long sellerPayoutAmount = totalAllocatedAmount - pgFeeAmount - platformFeeAmount;

        Settlement settlement = Settlement.from(SettlementCreateState.builder()
                .sellerId(sellerId)
                .year(year)
                .month(month)
                .totalAllocatedAmount(totalAllocatedAmount)
                .pgFeeAmount(pgFeeAmount)
                .platformFeeAmount(platformFeeAmount)
                .sellerPayoutAmount(sellerPayoutAmount)
                .build());

        Settlement savedSettlement = saveSettlementPort.save(settlement);
        Long settlementId = savedSettlement.getId();

        List<Long> allocationIds = sellerAllocations.stream()
                .map(PaymentAllocation::getId)
                .toList();
        updatePaymentAllocationPort.assignSettlement(allocationIds, settlementId);

        recordLedgerEntryUseCase.recordPgSettlement(settlementId, totalAllocatedAmount, pgFeeAmount);

        long sellerSettlementDebit = sellerPayoutAmount + platformFeeAmount;
        recordLedgerEntryUseCase.recordSellerSettlement(settlementId, sellerSettlementDebit, sellerPayoutAmount, platformFeeAmount);

        savedSettlement.complete();
        updateSettlementPort.update(savedSettlement);

        log.info("정산 완료 - settlementId: {}, sellerId: {}, year: {}, month: {}, total: {}, pgFee: {}, platformFee: {}, sellerPayout: {}",
                settlementId, sellerId, year, month, totalAllocatedAmount, pgFeeAmount, platformFeeAmount, sellerPayoutAmount);
    }
}
