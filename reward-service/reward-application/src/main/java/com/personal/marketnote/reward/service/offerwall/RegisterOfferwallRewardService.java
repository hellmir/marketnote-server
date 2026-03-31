package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateOfferwallRewardException;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.offerwall.GetOfferwallMapperUseCase;
import com.personal.marketnote.reward.port.in.usecase.offerwall.RegisterOfferwallRewardUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.offerwall.SaveOfferwallMapperPort;
import com.personal.marketnote.reward.port.out.offerwall.ValidateOfferwallSignaturePort;
import com.personal.marketnote.reward.service.point.ModifyUserPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@Transactional(isolation = READ_COMMITTED)
@RequiredArgsConstructor
public class RegisterOfferwallRewardService implements RegisterOfferwallRewardUseCase {
    private static final String UX_OFFERWALL_SUCCESS_UNIQUE_CONSTRAINT = "ux_offerwall_success";

    private final GetUserPointUseCase getUserPointUseCase;
    private final ModifyUserPointService modifyUserPointService;
    private final SaveOfferwallMapperPort saveOfferwallMapperPort;
    private final GetOfferwallMapperUseCase getOfferwallMapperUseCase;
    private final ValidateOfferwallSignaturePort validateOfferwallSignaturePort;

    @Override
    public Long register(RegisterOfferwallRewardCommand command) {
        validateSignature(command);
        validateUser(command);
        validateDuplicate(command);

        OfferwallMapper offerwallMapper;
        try {
            offerwallMapper = saveOfferwallMapperPort.save(
                    OfferwallMapper.from(RewardCommandToStateMapper.mapToOfferwallMapperCreateState(command, true))
            );
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈로 인해 유효성 검사에 실패하여 리워드 키 유니크 제약 조건에 위배되는 경우, DuplicateOfferwallRewardException 예외로 변환
            if (isOfferwallSuccessUniqueViolation(e)) {
                throw new DuplicateOfferwallRewardException(command.rewardKey());
            }

            throw e;
        }

        modifyUserPointService.modify(
                ModifyUserPointCommand.builder()
                        .userKey(command.userKey())
                        .changeType(UserPointChangeType.ACCRUAL)
                        .amount(command.quantity())
                        .sourceType(UserPointSourceType.OFFERWALL)
                        .sourceId(offerwallMapper.getId())
                        .reason(String.format("%s 리워드 보상 적립", command.offerwallType().getDescription()))
                        .build()
        );

        return offerwallMapper.getId();
    }

    private void validateSignature(RegisterOfferwallRewardCommand command) {
        validateOfferwallSignaturePort.validateSignature(
                command.offerwallType(),
                command.userDeviceType(),
                command.signedValue(),
                command.userKey(),
                command.rewardKey(),
                command.quantity(),
                command.campaignKey(),
                command.rewardUnit()
        );
    }

    private void validateUser(RegisterOfferwallRewardCommand command) {
        String userKey = command.userKey();
        if (!getUserPointUseCase.existsUserPoint(userKey)) {
            throw new UserNotFoundException(String.format("회원 정보를 찾을 수 없습니다. userKey: %s", userKey));
        }
    }

    private void validateDuplicate(RegisterOfferwallRewardCommand command) {
        if (getOfferwallMapperUseCase.existsSucceededOfferwallMapper(
                command.offerwallType(),
                command.rewardKey()
        )) {
            throw new DuplicateOfferwallRewardException(command.rewardKey());
        }
    }

    private boolean isOfferwallSuccessUniqueViolation(Throwable cause) {
        while (FormatValidator.hasValue(cause)) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
                return UX_OFFERWALL_SUCCESS_UNIQUE_CONSTRAINT.equals(cve.getConstraintName());
            }

            cause = cause.getCause();
        }

        return false;
    }
}
