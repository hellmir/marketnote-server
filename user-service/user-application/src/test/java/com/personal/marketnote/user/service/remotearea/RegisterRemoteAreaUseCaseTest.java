package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.exception.RemoteAreaAlreadyExistsException;
import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.SaveRemoteAreaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterRemoteAreaUseCaseTest {

    @InjectMocks
    private RegisterRemoteAreaService registerRemoteAreaService;

    @Mock
    private FindRemoteAreaPort findRemoteAreaPort;

    @Mock
    private SaveRemoteAreaPort saveRemoteAreaPort;

    @Test
    @DisplayName("광역시도만 지정하여 도서산간 지역을 등록한다")
    void shouldRegisterRemoteAreaWithProvinceOnly() {
        // given
        RegisterRemoteAreaCommand command = new RegisterRemoteAreaCommand("인천", null, null, null);
        when(findRemoteAreaPort.existsByAddress("인천", "", "", "")).thenReturn(false);

        // when
        registerRemoteAreaService.registerRemoteArea(command);

        // then
        ArgumentCaptor<RemoteArea> captor = ArgumentCaptor.forClass(RemoteArea.class);
        verify(saveRemoteAreaPort).save(captor.capture());

        RemoteArea savedRemoteArea = captor.getValue();
        assertThat(savedRemoteArea.getProvince()).isEqualTo("인천");
        assertThat(savedRemoteArea.getDistrict()).isEmpty();
        assertThat(savedRemoteArea.getVillage()).isEmpty();
        assertThat(savedRemoteArea.getSubarea()).isEmpty();
    }

    @Test
    @DisplayName("광역시도, 시군구, 읍면동을 지정하여 도서산간 지역을 등록한다")
    void shouldRegisterRemoteAreaWithProvinceDistrictVillage() {
        // given
        RegisterRemoteAreaCommand command = new RegisterRemoteAreaCommand("경남", "통영시", "사량면", null);
        when(findRemoteAreaPort.existsByAddress("경남", "통영시", "사량면", "")).thenReturn(false);

        // when
        registerRemoteAreaService.registerRemoteArea(command);

        // then
        ArgumentCaptor<RemoteArea> captor = ArgumentCaptor.forClass(RemoteArea.class);
        verify(saveRemoteAreaPort).save(captor.capture());

        RemoteArea savedRemoteArea = captor.getValue();
        assertThat(savedRemoteArea.getProvince()).isEqualTo("경남");
        assertThat(savedRemoteArea.getDistrict()).isEqualTo("통영시");
        assertThat(savedRemoteArea.getVillage()).isEqualTo("사량면");
        assertThat(savedRemoteArea.getSubarea()).isEmpty();
    }

    @Test
    @DisplayName("모든 필드를 지정하여 도서산간 지역을 등록한다")
    void shouldRegisterRemoteAreaWithAllFields() {
        // given
        RegisterRemoteAreaCommand command = new RegisterRemoteAreaCommand("충남", "보령시", "오천면", "녹도리");
        when(findRemoteAreaPort.existsByAddress("충남", "보령시", "오천면", "녹도리")).thenReturn(false);

        // when
        registerRemoteAreaService.registerRemoteArea(command);

        // then
        ArgumentCaptor<RemoteArea> captor = ArgumentCaptor.forClass(RemoteArea.class);
        verify(saveRemoteAreaPort).save(captor.capture());

        RemoteArea savedRemoteArea = captor.getValue();
        assertThat(savedRemoteArea.getProvince()).isEqualTo("충남");
        assertThat(savedRemoteArea.getDistrict()).isEqualTo("보령시");
        assertThat(savedRemoteArea.getVillage()).isEqualTo("오천면");
        assertThat(savedRemoteArea.getSubarea()).isEqualTo("녹도리");
    }

    @Test
    @DisplayName("이미 등록된 도서산간 지역이면 RemoteAreaAlreadyExistsException이 발생한다")
    void shouldThrowWhenRemoteAreaAlreadyExists() {
        // given
        RegisterRemoteAreaCommand command = new RegisterRemoteAreaCommand("인천", "옹진군", "덕적", null);
        when(findRemoteAreaPort.existsByAddress("인천", "옹진군", "덕적", "")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> registerRemoteAreaService.registerRemoteArea(command))
                .isInstanceOf(RemoteAreaAlreadyExistsException.class);

        verifyNoInteractions(saveRemoteAreaPort);
    }

    @Test
    @DisplayName("중복 검증 시 FindRemoteAreaPort.existsByAddress가 호출된다")
    void shouldCallExistsByAddress() {
        // given
        RegisterRemoteAreaCommand command = new RegisterRemoteAreaCommand("충남", "보령시", "오천면", "외연도");
        when(findRemoteAreaPort.existsByAddress("충남", "보령시", "오천면", "외연도")).thenReturn(false);

        // when
        registerRemoteAreaService.registerRemoteArea(command);

        // then
        verify(findRemoteAreaPort).existsByAddress("충남", "보령시", "오천면", "외연도");
    }
}
