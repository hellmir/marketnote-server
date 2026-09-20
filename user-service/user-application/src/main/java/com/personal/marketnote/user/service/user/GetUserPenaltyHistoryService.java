package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetUserPenaltyHistoryResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserPenaltyHistoryUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPenaltyHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserPenaltyHistoryService implements GetUserPenaltyHistoryUseCase {
    private final FindUserPenaltyHistoryPort findUserPenaltyHistoryPort;

    @Override
    public Page<GetUserPenaltyHistoryResult> getUserPenaltyHistories(
            Long userId,
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            UserPenaltyHistorySortProperty sortProperty
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue()));
        return findUserPenaltyHistoryPort.findUserPenaltyHistoriesByUserId(pageable, userId)
                .map(GetUserPenaltyHistoryResult::from);
    }
}
