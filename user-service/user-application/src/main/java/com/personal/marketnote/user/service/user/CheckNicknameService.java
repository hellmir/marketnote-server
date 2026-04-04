package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.user.port.in.result.CheckNicknameResult;
import com.personal.marketnote.user.port.in.usecase.user.CheckNicknameUseCase;
import com.personal.marketnote.user.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class CheckNicknameService implements CheckNicknameUseCase {
    private final FindUserPort findUserPort;
    private final FindProfanityWordPort findProfanityWordPort;

    @Override
    public CheckNicknameResult checkNickname(String nickname) {
        boolean isDuplicated = findUserPort.existsByNickname(nickname);
        boolean containsProfanity = findProfanityWordPort.containsProfanity(nickname);
        return CheckNicknameResult.of(isDuplicated, containsProfanity);
    }
}
