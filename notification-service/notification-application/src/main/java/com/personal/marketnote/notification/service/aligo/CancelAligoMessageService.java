package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageNotFoundException;
import com.personal.marketnote.notification.port.in.usecase.aligo.CancelAligoMessageUseCase;
import com.personal.marketnote.notification.port.out.aligo.CancelAligoMessageExternalPort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
public class CancelAligoMessageService implements CancelAligoMessageUseCase {

    private final FindAligoMessagePort findAligoMessagePort;
    private final CancelAligoMessageExternalPort cancelAligoMessageExternalPort;
    private final UpdateAligoMessagePort updateAligoMessagePort;
    private final Clock clock;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelMessage(Long messageId) {
        AligoMessage message = findAligoMessagePort.findById(messageId)
                .orElseThrow(() -> new AligoMessageNotFoundException(messageId));

        LocalDateTime now = LocalDateTime.now(clock);
        message.validateCancellable(now);

        cancelAligoMessageExternalPort.cancel(message.getAligoMid());
        message.cancel();
        updateAligoMessagePort.update(message);
    }
}
