package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.email.Email;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.UpdateTargetNoValueException;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.user.domain.user.Nickname;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.*;
import static com.personal.marketnote.user.exception.ExceptionMessage.*;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
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
            Nickname newNickname = Nickname.of(updateUserInfoCommand.nickname());
            user.validateDifferentNickname(newNickname);
            validateNicknameProfanity(newNickname.getValue());
            validateDuplicateNickname(newNickname.getValue());
            user.updateNickname(newNickname);

            return;
        }

        if (updateUserInfoCommand.hasPassword()) {
            String newPassword = updateUserInfoCommand.password();
            user.updatePassword(newPassword, passwordEncoder);
            return;
        }

        if (updateUserInfoCommand.hasEmail()) {
            Email newEmail = Email.of(updateUserInfoCommand.email());
            user.validateDifferentEmail(newEmail);
            validateDuplicateEmail(newEmail.getValue());
            user.updateEmail(newEmail);

            return;
        }

        if (updateUserInfoCommand.hasPhoneNumber()) {
            PhoneNumber newPhoneNumber = PhoneNumber.of(updateUserInfoCommand.phoneNumber());
            user.validateDifferentPhoneNumber(newPhoneNumber);
            validateDuplicatePhoneNumber(newPhoneNumber.getValue());
            user.updatePhoneNumber(newPhoneNumber);

            return;
        }

        throw new UpdateTargetNoValueException();
    }

    private void validateNicknameProfanity(String nickname) {
        if (findProfanityWordPort.containsProfanity(nickname)) {
            log.warn("닉네임에 부적절한 단어가 포함되어 있습니다. nickname={}", nickname);
            throw new InvalidNicknameContainsProfanityException();
        }
    }

    private void validateDuplicateEmail(String email) {
        if (findUserPort.existsByEmail(email)) {
            log.warn("이미 가입된 이메일 주소입니다. email={}", email);
            throw new UserExistsException(String.format(EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE, FOURTH_ERROR_CODE));
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (findUserPort.existsByNickname(nickname)) {
            log.warn("이미 가입된 닉네임입니다. nickname={}", nickname);
            throw new UserExistsException(String.format(NICKNAME_ALREADY_EXISTS_EXCEPTION_MESSAGE, FIFTH_ERROR_CODE));
        }
    }

    private void validateDuplicatePhoneNumber(String phoneNumber) {
        if (findUserPort.existsByPhoneNumber(phoneNumber)) {
            log.warn("이미 가입된 전화번호입니다. phoneNumber={}", phoneNumber);
            throw new UserExistsException(String.format(PHONE_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, SIXTH_ERROR_CODE));
        }
    }
}
