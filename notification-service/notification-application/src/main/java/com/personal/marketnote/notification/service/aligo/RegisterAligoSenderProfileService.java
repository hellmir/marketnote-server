package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileNotFoundException;
import com.personal.marketnote.notification.port.in.command.RegisterAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoSenderProfileResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterAligoSenderProfileUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.RegisterAligoSenderProfileExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoSenderProfilePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class RegisterAligoSenderProfileService implements RegisterAligoSenderProfileUseCase {

    private final FindAligoSenderProfilePort findAligoSenderProfilePort;
    private final RegisterAligoSenderProfileExternalPort registerAligoSenderProfileExternalPort;
    private final UpdateAligoSenderProfilePort updateAligoSenderProfilePort;

    @Override
    public RegisterAligoSenderProfileResult registerSenderProfile(RegisterAligoSenderProfileCommand command) {
        AligoSenderProfile profile = findProfile(command.senderKey());

        registerAligoSenderProfileExternalPort.registerProfile(command.senderKey(), command.categoryCode());

        updateCategoryCode(profile, command.categoryCode());

        return new RegisterAligoSenderProfileResult(profile.getId(), profile.getSenderKey(), command.categoryCode());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public AligoSenderProfile findProfile(String senderKey) {
        return findAligoSenderProfilePort.findBySenderKey(senderKey)
                .orElseThrow(() -> new AligoSenderProfileNotFoundException(senderKey));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateCategoryCode(AligoSenderProfile profile, String categoryCode) {
        profile.updateCategoryCode(categoryCode);
        updateAligoSenderProfilePort.update(profile);
    }
}
