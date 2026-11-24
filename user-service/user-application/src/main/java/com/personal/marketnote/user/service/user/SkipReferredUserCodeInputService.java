package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.SkipReferredUserCodeInputUseCase;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class SkipReferredUserCodeInputService implements SkipReferredUserCodeInputUseCase {
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserPort updateUserPort;

    @Override
    public void skipReferredUserCodeInput(Long requestUserId) {
        User requestUser = getUserUseCase.getUser(requestUserId);
        requestUser.skipReferredUserCodeInput(LocalDateTime.now());
        updateUserPort.update(requestUser);
    }
}
