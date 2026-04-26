package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaSnapshotState;
import com.personal.marketnote.user.exception.RemoteAreaNotFoundException;
import com.personal.marketnote.user.port.out.remotearea.DeleteRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteRemoteAreaUseCaseTest {

    @InjectMocks
    private DeleteRemoteAreaService deleteRemoteAreaService;

    @Mock
    private FindRemoteAreaPort findRemoteAreaPort;

    @Mock
    private DeleteRemoteAreaPort deleteRemoteAreaPort;

    @Test
    @DisplayName("도서산간 지역 ID로 삭제하면 deactivate가 호출된다")
    void shouldDeactivateRemoteAreaById() {
        // given
        Long id = 1L;
        RemoteArea remoteArea = RemoteArea.from(
                RemoteAreaSnapshotState.builder()
                        .id(id)
                        .province("충남")
                        .district("보령시")
                        .village("오천면")
                        .subarea("녹도리")
                        .build()
        );
        when(findRemoteAreaPort.findActiveById(id)).thenReturn(Optional.of(remoteArea));

        // when
        deleteRemoteAreaService.deleteRemoteArea(id);

        // then
        verify(deleteRemoteAreaPort).deactivate(remoteArea);
    }

    @Test
    @DisplayName("존재하지 않는 도서산간 지역 ID로 삭제하면 RemoteAreaNotFoundException이 발생한다")
    void shouldThrowWhenRemoteAreaNotFound() {
        // given
        Long id = 999L;
        when(findRemoteAreaPort.findActiveById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteRemoteAreaService.deleteRemoteArea(id))
                .isInstanceOf(RemoteAreaNotFoundException.class);

        verifyNoInteractions(deleteRemoteAreaPort);
    }

    @Test
    @DisplayName("삭제 시 RemoteArea.deactivate()가 호출된 후 DeleteRemoteAreaPort.deactivate에 전달된다")
    void shouldDeactivateDomainBeforePassingToPort() {
        // given
        Long id = 1L;
        RemoteArea remoteArea = RemoteArea.from(
                RemoteAreaSnapshotState.builder()
                        .id(id)
                        .province("인천")
                        .district("옹진군")
                        .village("덕적면")
                        .subarea("")
                        .build()
        );
        when(findRemoteAreaPort.findActiveById(id)).thenReturn(Optional.of(remoteArea));

        // when
        deleteRemoteAreaService.deleteRemoteArea(id);

        // then
        ArgumentCaptor<RemoteArea> captor = ArgumentCaptor.forClass(RemoteArea.class);
        verify(deleteRemoteAreaPort).deactivate(captor.capture());

        RemoteArea deactivated = captor.getValue();
        assertThat(deactivated.isInactive()).isTrue();
    }
}
