package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoSenderProfileListResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoSenderProfileListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoSenderProfilePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoSenderProfilePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAligoSenderProfileListUseCaseTest {

    @InjectMocks
    private GetAligoSenderProfileListService getAligoSenderProfileListService;

    @Mock
    private GetAligoSenderProfileListExternalPort getAligoSenderProfileListExternalPort;

    @Mock
    private FindAligoSenderProfilePort findAligoSenderProfilePort;

    @Mock
    private SaveAligoSenderProfilePort saveAligoSenderProfilePort;

    @Mock
    private UpdateAligoSenderProfilePort updateAligoSenderProfilePort;

    @Test
    @DisplayName("외부 API에서 발신프로필 목록을 조회하고 로컬에 신규 저장한다")
    void shouldFetchFromExternalAndSaveNewProfile() {
        // given
        AligoSenderProfile externalProfile = AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey("sender-key-1")
                        .plusId("@channel1")
                        .name("채널1")
                        .categoryCode("cat-001")
                        .profileInspectionStatus(AligoProfileInspectionStatus.A)
                        .senderProfileStatus(AligoSenderProfileStatus.A)
                        .alimUseYn(true)
                        .build()
        );
        when(getAligoSenderProfileListExternalPort.getProfileList()).thenReturn(List.of(externalProfile));
        when(findAligoSenderProfilePort.findAll()).thenReturn(List.of());
        when(saveAligoSenderProfilePort.save(any(AligoSenderProfile.class))).thenReturn(externalProfile);

        // when
        GetAligoSenderProfileListResult result = getAligoSenderProfileListService.getSenderProfileList();

        // then
        assertThat(result.profiles()).hasSize(1);
        assertThat(result.profiles().get(0).senderKey()).isEqualTo("sender-key-1");
        verify(saveAligoSenderProfilePort).save(any(AligoSenderProfile.class));
        verify(updateAligoSenderProfilePort, never()).update(any());
    }

    @Test
    @DisplayName("이미 존재하는 senderKey는 업데이트한다")
    void shouldUpdateExistingProfile() {
        // given
        AligoSenderProfile externalProfile = AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey("existing-key")
                        .plusId("@existingChannel")
                        .name("기존채널")
                        .profileInspectionStatus(AligoProfileInspectionStatus.A)
                        .senderProfileStatus(AligoSenderProfileStatus.A)
                        .alimUseYn(true)
                        .build()
        );
        AligoSenderProfile localProfile = AligoSenderProfile.from(
                AligoSenderProfileCreateState.builder()
                        .senderKey("existing-key")
                        .plusId("@existingChannel")
                        .profileInspectionStatus(AligoProfileInspectionStatus.C)
                        .senderProfileStatus(AligoSenderProfileStatus.S)
                        .alimUseYn(false)
                        .build()
        );
        when(getAligoSenderProfileListExternalPort.getProfileList()).thenReturn(List.of(externalProfile));
        when(findAligoSenderProfilePort.findAll()).thenReturn(List.of(localProfile));

        // when
        GetAligoSenderProfileListResult result = getAligoSenderProfileListService.getSenderProfileList();

        // then
        assertThat(result.profiles()).hasSize(1);
        verify(updateAligoSenderProfilePort).update(any(AligoSenderProfile.class));
        verify(saveAligoSenderProfilePort, never()).save(any());
    }
}
