package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserPointService implements GetUserPointUseCase {
    private final FindUserPointPort findUserPointPort;

    @Override
    public boolean existsUserPoint(String userKey) {
        return findUserPointPort.existsByUserKey(userKey);
    }

    @Override
    public UserPoint getUserPoint(Long userId) {
        return findUserPointPort.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("회원 포인트 정보를 찾을 수 없습니다. userId=" + userId));
    }

    @Override
    public UserPoint getUserPoint(String userKey) {
        return findUserPointPort.findByUserKey(userKey)
                .orElseThrow(() -> new UserNotFoundException("회원 포인트 정보를 찾을 수 없습니다. userKey=" + userKey));
    }
}
