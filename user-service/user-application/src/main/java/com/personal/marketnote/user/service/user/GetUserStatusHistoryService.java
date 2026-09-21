package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.UserStatusHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetUserStatusHistoryResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserStatusHistoryUseCase;
import com.personal.marketnote.user.port.out.user.FindUserStatusHistoryPort;
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
public class GetUserStatusHistoryService implements GetUserStatusHistoryUseCase {
    private final FindUserStatusHistoryPort findUserStatusHistoryPort;

    @Override
    public Page<GetUserStatusHistoryResult> getUserStatusHistories(
            Long userId,
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            UserStatusHistorySortProperty sortProperty
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue()));
        return findUserStatusHistoryPort.findUserStatusHistoriesByUserId(pageable, userId)
                .map(GetUserStatusHistoryResult::from);
    }
}
