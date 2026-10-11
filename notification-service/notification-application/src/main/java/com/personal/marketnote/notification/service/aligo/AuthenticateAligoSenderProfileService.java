package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileCreateState;
import com.personal.marketnote.notification.port.in.command.AuthenticateAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.AuthenticateAligoSenderProfileResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.AuthenticateAligoSenderProfileUseCase;
import com.personal.marketnote.notification.port.out.aligo.RequestAligoSenderAuthenticationPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoSenderProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class AuthenticateAligoSenderProfileService implements AuthenticateAligoSenderProfileUseCase {

    private final RequestAligoSenderAuthenticationPort requestAligoSenderAuthenticationPort;
    private final SaveAligoSenderProfilePort saveAligoSenderProfilePort;

    @Override
    public AuthenticateAligoSenderProfileResult authenticateSenderProfile(
            AuthenticateAligoSenderProfileCommand command
    ) {
        String senderKey = requestAligoSenderAuthenticationPort.requestAuthentication(
                command.plusId(), command.phoneNumber()
        );

        AligoSenderProfile savedProfile = saveProfile(senderKey, command);

        return new AuthenticateAligoSenderProfileResult(savedProfile.getSenderKey(), savedProfile.getPlusId());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AligoSenderProfile saveProfile(String senderKey, AuthenticateAligoSenderProfileCommand command) {
        AligoSenderProfile profile = AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey(senderKey)
                        .plusId(command.plusId())
                        .phoneNumber(command.phoneNumber())
                        .build()
        );
        return saveAligoSenderProfilePort.save(profile);
    }
}
