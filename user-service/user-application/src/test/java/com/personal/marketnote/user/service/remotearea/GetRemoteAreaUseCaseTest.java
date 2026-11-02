package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaSnapshotState;
import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaItemResult;
import com.personal.marketnote.user.port.in.result.remotearea.GetRemoteAreaResult;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRemoteAreaUseCaseTest {

    @InjectMocks
    private GetRemoteAreaService getRemoteAreaService;

    @Mock
    private FindRemoteAreaPort findRemoteAreaPort;

    @Test
    @DisplayName("ACTIVE 상태의 도서산간 지역 목록을 반환한다")
    void shouldReturnActiveRemoteAreas() {
        // given
        RemoteArea jeju = RemoteArea.from(
                RemoteAreaSnapshotState.builder()
                        .id(1L)
                        .province("제주")
                        .district("")
                        .village("")
                        .subarea("")
                        .build()
        );
        RemoteArea chungnam = RemoteArea.from(
                RemoteAreaSnapshotState.builder()
                        .id(2L)
                        .province("충남")
                        .district("보령시")
                        .village("오천면")
                        .subarea("녹도리")
                        .build()
        );

        when(findRemoteAreaPort.findAllActive()).thenReturn(List.of(jeju, chungnam));

        // when
        GetRemoteAreaResult result = getRemoteAreaService.getRemoteAreas();

        // then
        assertThat(result.remoteAreas()).hasSize(2);

        GetRemoteAreaItemResult firstItem = result.remoteAreas().get(0);
        assertThat(firstItem.id()).isEqualTo(1L);
        assertThat(firstItem.province()).isEqualTo("제주");
        assertThat(firstItem.district()).isEmpty();
        assertThat(firstItem.village()).isEmpty();
        assertThat(firstItem.subarea()).isEmpty();

        GetRemoteAreaItemResult secondItem = result.remoteAreas().get(1);
        assertThat(secondItem.id()).isEqualTo(2L);
        assertThat(secondItem.province()).isEqualTo("충남");
        assertThat(secondItem.district()).isEqualTo("보령시");
        assertThat(secondItem.village()).isEqualTo("오천면");
        assertThat(secondItem.subarea()).isEqualTo("녹도리");
    }

    @Test
    @DisplayName("등록된 도서산간 지역이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoRemoteAreas() {
        // given
        when(findRemoteAreaPort.findAllActive()).thenReturn(Collections.emptyList());

        // when
        GetRemoteAreaResult result = getRemoteAreaService.getRemoteAreas();

        // then
        assertThat(result.remoteAreas()).isEmpty();
    }

    @Test
    @DisplayName("FindRemoteAreaPort.findAllActive()가 호출된다")
    void shouldCallFindAllActive() {
        // given
        when(findRemoteAreaPort.findAllActive()).thenReturn(Collections.emptyList());

        // when
        getRemoteAreaService.getRemoteAreas();

        // then
        verify(findRemoteAreaPort).findAllActive();
    }
}
