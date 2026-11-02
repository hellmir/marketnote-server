package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.LoginHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetLoginHistoryResult;
import com.personal.marketnote.user.port.in.usecase.user.GetLoginHistoryUseCase;
import com.personal.marketnote.user.port.out.user.FindLoginHistoryPort;
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
public class GetLoginHistoryService implements GetLoginHistoryUseCase {
    private final FindLoginHistoryPort findLoginHistoryPort;

    @Override
    public Page<GetLoginHistoryResult> getLoginHistories(
            Long userId,
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            LoginHistorySortProperty sortProperty
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue()));
        return findLoginHistoryPort.findLoginHistoriesByUserId(pageable, userId)
                .map(GetLoginHistoryResult::from);
    }
}
