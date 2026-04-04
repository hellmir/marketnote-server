package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.UpdateTargetNoValueException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.InvalidNicknameContainsProfanityException;
import com.personal.marketnote.user.exception.UserExistsException;
import com.personal.marketnote.user.port.in.command.UpdateUserInfoCommand;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.in.usecase.user.UpdateUserUseCase;
import com.personal.marketnote.user.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;
import static com.personal.marketnote.user.exception.ExceptionMessage.*;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class UpdateUserService implements UpdateUserUseCase {
    private final GetUserUseCase getUserUseCase;
    private final FindUserPort findUserPort;
    private final UpdateUserPort updateUserPort;
    private final PasswordEncoder passwordEncoder;
    private final FindProfanityWordPort findProfanityWordPort;

    @Override
    public void updateUserInfo(boolean isAdmin, Long id, UpdateUserInfoCommand updateUserInfoCommand) {
        User user = isAdmin
                ? getUserUseCase.getAllStatusUser(id)
                : getUserUseCase.getUser(id);
        updateTarget(isAdmin, updateUserInfoCommand, user);
        updateUserPort.update(user);
    }

    private void updateTarget(boolean isAdmin, UpdateUserInfoCommand updateUserInfoCommand, User user) {
        if (isAdmin && updateUserInfoCommand.hasIsActive()) {
            user.updateStatus(updateUserInfoCommand.isActive());
            return;
        }

        if (updateUserInfoCommand.hasNickname()) {
            String newNickname = updateUserInfoCommand.nickname();
            user.validateDifferentNickname(newNickname);
            validateNicknameProfanity(newNickname);
            validateDuplicateNickname(newNickname);
            user.updateNickname(newNickname);

            return;
        }

        if (updateUserInfoCommand.hasPassword()) {
            String newPassword = updateUserInfoCommand.password();
            user.updatePassword(newPassword, passwordEncoder);
            return;
        }

        if (updateUserInfoCommand.hasEmail()) {
            String newEmail = updateUserInfoCommand.email();
            user.validateDifferentEmail(newEmail);
            validateDuplicateEmail(newEmail);
            user.updateEmail(newEmail);

            return;
        }

        if (updateUserInfoCommand.hasPhoneNumber()) {
            String newPhoneNumber = updateUserInfoCommand.phoneNumber();
            user.validateDifferentPhoneNumber(newPhoneNumber);
            validateDuplicatePhoneNumber(newPhoneNumber);
            user.updatePhoneNumber(newPhoneNumber);

            return;
        }

        throw new UpdateTargetNoValueException();
    }

    private void validateNicknameProfanity(String nickname) {
        if (findProfanityWordPort.containsProfanity(nickname)) {
            throw new InvalidNicknameContainsProfanityException(FIFTH_ERROR_CODE, nickname);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (findUserPort.existsByEmail(email)) {
            throw new UserExistsException(
                    String.format(EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE, FOURTH_ERROR_CODE, email)
            );
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (findUserPort.existsByNickname(nickname)) {
            throw new UserExistsException(
                    String.format(NICKNAME_ALREADY_EXISTS_EXCEPTION_MESSAGE, FIFTH_ERROR_CODE, nickname)
            );
        }
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (findUserPort.existsByPhoneNumber(phoneNumber)) {
            throw new UserExistsException(
                    String.format(PHONE_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, SIXTH_ERROR_CODE, phoneNumber)
            );
        }
    }
}
