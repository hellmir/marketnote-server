package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.exception.InvalidPointHistoryDateRangeException;
import com.personal.marketnote.reward.port.in.command.point.GetUserPointHistoryCommand;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointHistoryUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserPointHistoryService implements GetUserPointHistoryUseCase {
    static final LocalDate DEFAULT_START_DATE = LocalDate.of(2000, 1, 1);
    static final LocalDate DEFAULT_END_DATE = LocalDate.of(2999, 12, 31);

    private final FindUserPointHistoryPort findUserPointHistoryPort;

    @Override
    public GetUserPointHistoryResult getUserPointHistories(GetUserPointHistoryCommand command) {
        UserPointHistoryFilter targetFilter = FormatValidator.hasValue(command.filter())
                ? command.filter()
                : UserPointHistoryFilter.ALL;

        LocalDate startDate = FormatValidator.hasValue(command.startDate())
                ? command.startDate()
                : DEFAULT_START_DATE;

        LocalDate endDate = FormatValidator.hasValue(command.endDate())
                ? command.endDate()
                : DEFAULT_END_DATE;

        if (startDate.isAfter(endDate)) {
            throw new InvalidPointHistoryDateRangeException(startDate, endDate);
        }

        return GetUserPointHistoryResult.from(
                findUserPointHistoryPort.findByUserId(command.userId(), targetFilter, startDate, endDate)
        );
    }
}
