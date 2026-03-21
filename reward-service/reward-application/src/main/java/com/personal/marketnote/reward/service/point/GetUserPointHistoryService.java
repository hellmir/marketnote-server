package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.exception.InvalidPointHistoryDateRangeException;
import com.personal.marketnote.reward.exception.InvalidPointHistoryPageSizeException;
import com.personal.marketnote.reward.port.in.command.point.GetUserPointHistoryCommand;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointHistoryUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserPointHistoryService implements GetUserPointHistoryUseCase {
    static final LocalDate DEFAULT_START_DATE = LocalDate.of(2000, 1, 1);
    static final LocalDate DEFAULT_END_DATE = LocalDate.of(2999, 12, 31);
    static final int MAX_PAGE_SIZE = 100;

    private final FindUserPointHistoryPort findUserPointHistoryPort;

    @Override
    public GetUserPointHistoryResult getUserPointHistories(GetUserPointHistoryCommand command) {
        validatePageSize(command.pageSize());

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

        int fetchSize = command.pageSize() + 1;
        List<UserPointHistory> histories = findUserPointHistoryPort.findByUserId(
                command.userId(), targetFilter, startDate, endDate, command.cursor(), fetchSize
        );

        boolean hasNext = histories.size() > command.pageSize();
        List<UserPointHistory> pagedHistories = hasNext
                ? histories.subList(0, command.pageSize())
                : histories;

        Long nextCursor = pagedHistories.isEmpty() ? null : pagedHistories.getLast().getId();

        Long totalElements = resolveTotalElements(
                command, targetFilter, startDate, endDate, hasNext, pagedHistories.size()
        );

        return GetUserPointHistoryResult.from(totalElements, hasNext, nextCursor, pagedHistories);
    }

    private void validatePageSize(int pageSize) {
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new InvalidPointHistoryPageSizeException(pageSize);
        }
    }

    private Long resolveTotalElements(GetUserPointHistoryCommand command,
                                      UserPointHistoryFilter filter,
                                      LocalDate startDate, LocalDate endDate,
                                      boolean hasNext, int currentSize) {
        if (FormatValidator.hasValue(command.cursor())) {
            return null;
        }
        if (!hasNext) {
            return (long) currentSize;
        }
        return findUserPointHistoryPort.countByUserId(command.userId(), filter, startDate, endDate);
    }
}
