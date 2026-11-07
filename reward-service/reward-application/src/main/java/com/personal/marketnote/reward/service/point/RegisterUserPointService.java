package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.exception.DuplicateUserPointException;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.RegisterUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterUserPointService implements RegisterUserPointUseCase {
    private final SaveUserPointPort saveUserPointPort;
    private final SaveUserPointHistoryPort saveUserPointHistoryPort;
    private final FindUserPointPort findUserPointPort;

    @Override
    public void register(RegisterUserPointCommand command) {
        validateDuplicate(command.userId());

        UserPoint savedUserPoint = saveUserPointPort.save(
                UserPoint.from(RewardCommandToStateMapper.mapToUserPointCreateState(command))
        );

        LocalDateTime accumulatedAt = savedUserPoint.getCreatedAt();
        saveUserPointHistoryPort.save(
                UserPointHistory.from(
                        RewardCommandToStateMapper.mapToUserPointHistoryCreateState(
                                command,
                                accumulatedAt
                        )
                )
        );
    }

    private void validateDuplicate(Long userId) {
        if (findUserPointPort.existsByUserId(userId)) {
            log.warn("이미 회원 포인트 정보가 생성되어 있습니다. userId={}", userId);
            throw new DuplicateUserPointException();
        }
    }
}
