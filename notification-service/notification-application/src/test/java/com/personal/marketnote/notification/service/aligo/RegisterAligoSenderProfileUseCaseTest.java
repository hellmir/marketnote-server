package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileNotFoundException;
import com.personal.marketnote.notification.port.in.command.RegisterAligoSenderProfileCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoSenderProfileResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.RegisterAligoSenderProfileExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoSenderProfilePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAligoSenderProfileUseCaseTest {

    @InjectMocks
    private RegisterAligoSenderProfileService registerAligoSenderProfileService;

    @Mock
    private FindAligoSenderProfilePort findAligoSenderProfilePort;

    @Mock
    private RegisterAligoSenderProfileExternalPort registerAligoSenderProfileExternalPort;

    @Mock
    private UpdateAligoSenderProfilePort updateAligoSenderProfilePort;

    @Test
    @DisplayName("발신프로필 등록 시 senderKey로 기존 프로필을 조회하고 외부 API를 호출한 뒤 categoryCode를 업데이트한다")
    void shouldFindProfileAndCallExternalApiAndUpdateCategoryCode() {
        // given
        RegisterAligoSenderProfileCommand command = new RegisterAligoSenderProfileCommand(
                "test-sender-key", "category-001"
        );
        AligoSenderProfile profile = AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey("test-sender-key")
                        .plusId("@testChannel")
                        .build()
        );
        when(findAligoSenderProfilePort.findBySenderKey("test-sender-key")).thenReturn(Optional.of(profile));

        // when
        RegisterAligoSenderProfileResult result = registerAligoSenderProfileService
                .registerSenderProfile(command);

        // then
        assertThat(result.senderKey()).isEqualTo("test-sender-key");
        assertThat(result.categoryCode()).isEqualTo("category-001");
        verify(registerAligoSenderProfileExternalPort).registerProfile("test-sender-key", "category-001");
        verify(updateAligoSenderProfilePort).update(any(AligoSenderProfile.class));
    }

    @Test
    @DisplayName("존재하지 않는 senderKey로 등록하면 AligoSenderProfileNotFoundException을 던진다")
    void shouldThrowNotFoundExceptionWhenSenderKeyNotExists() {
        // given
        RegisterAligoSenderProfileCommand command = new RegisterAligoSenderProfileCommand(
                "non-existent-key", "category-001"
        );
        when(findAligoSenderProfilePort.findBySenderKey("non-existent-key")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> registerAligoSenderProfileService.registerSenderProfile(command))
                .isInstanceOf(AligoSenderProfileNotFoundException.class);
        verifyNoInteractions(registerAligoSenderProfileExternalPort);
        verifyNoInteractions(updateAligoSenderProfilePort);
    }
}
