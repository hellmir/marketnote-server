package com.personal.marketnote.user.adapter.out.persistence.remotearea;

import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteAreaDataInitializerTest {

    @InjectMocks
    private RemoteAreaDataInitializer remoteAreaDataInitializer;

    @Mock
    private RemoteAreaJpaRepository remoteAreaJpaRepository;

    @Captor
    private ArgumentCaptor<List<RemoteAreaJpaEntity>> batchCaptor;

    @Test
    @DisplayName("도서산간 지역 데이터가 이미 존재하면 초기화를 건너뛴다")
    void shouldSkipInitializationWhenDataAlreadyExists() throws Exception {
        // given
        when(remoteAreaJpaRepository.count()).thenReturn(100L);

        // when
        remoteAreaDataInitializer.run(null);

        // then
        verify(remoteAreaJpaRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("CSV 파일을 파싱하여 도서산간 지역 데이터를 저장한다")
    void shouldParseAndSaveRemoteAreaData() throws Exception {
        // given
        when(remoteAreaJpaRepository.count()).thenReturn(0L);

        // when
        remoteAreaDataInitializer.run(null);

        // then
        verify(remoteAreaJpaRepository).saveAll(batchCaptor.capture());
        List<RemoteAreaJpaEntity> savedEntities = batchCaptor.getValue();

        assertThat(savedEntities).isNotEmpty();

        // 첫 번째 행: 인천,중구,무의,
        RemoteAreaJpaEntity first = savedEntities.get(0);
        assertThat(first.getProvince()).isEqualTo("인천");
        assertThat(first.getDistrict()).isEqualTo("중구");
        assertThat(first.getVillage()).isEqualTo("무의");
        assertThat(first.getSubarea()).isEqualTo("");
    }

    @Test
    @DisplayName("빈 필드는 빈 문자열로 정규화한다")
    void shouldNormalizeEmptyFieldsToEmptyString() throws Exception {
        // given
        when(remoteAreaJpaRepository.count()).thenReturn(0L);

        // when
        remoteAreaDataInitializer.run(null);

        // then
        verify(remoteAreaJpaRepository).saveAll(batchCaptor.capture());
        List<RemoteAreaJpaEntity> savedEntities = batchCaptor.getValue();

        // 충남,태안군,,나암도 (village 빈 필드)
        RemoteAreaJpaEntity taeanNaamdo = savedEntities.stream()
                .filter(entity -> "태안군".equals(entity.getDistrict()) && "나암도".equals(entity.getSubarea()))
                .findFirst()
                .orElseThrow();

        assertThat(taeanNaamdo.getProvince()).isEqualTo("충남");
        assertThat(taeanNaamdo.getVillage()).isEqualTo("");
    }

    @Test
    @DisplayName("괄호로 감싸진 세부지역은 빈 문자열로 정규화한다")
    void shouldNormalizeParenthesizedSubareaToEmptyString() throws Exception {
        // given
        when(remoteAreaJpaRepository.count()).thenReturn(0L);

        // when
        remoteAreaDataInitializer.run(null);

        // then
        verify(remoteAreaJpaRepository).saveAll(batchCaptor.capture());
        List<RemoteAreaJpaEntity> savedEntities = batchCaptor.getValue();

        // 전남,여수시,삼산면,(전체) → subarea ""
        RemoteAreaJpaEntity yeosuSamsan = savedEntities.stream()
                .filter(entity -> "여수시".equals(entity.getDistrict()) && "삼산면".equals(entity.getVillage()))
                .findFirst()
                .orElseThrow();

        assertThat(yeosuSamsan.getSubarea()).isEqualTo("");
    }

    @Test
    @DisplayName("CSV 헤더 행은 데이터로 저장하지 않는다")
    void shouldSkipCsvHeaderRow() throws Exception {
        // given
        when(remoteAreaJpaRepository.count()).thenReturn(0L);

        // when
        remoteAreaDataInitializer.run(null);

        // then
        verify(remoteAreaJpaRepository).saveAll(batchCaptor.capture());
        List<RemoteAreaJpaEntity> savedEntities = batchCaptor.getValue();

        boolean hasHeader = savedEntities.stream()
                .anyMatch(entity -> "광역시도".equals(entity.getProvince()));

        assertThat(hasHeader).isFalse();
    }

    @Test
    @DisplayName("CSV 파일의 모든 유효한 행이 저장된다")
    void shouldSaveAllValidRows() throws Exception {
        // given
        when(remoteAreaJpaRepository.count()).thenReturn(0L);

        // when
        remoteAreaDataInitializer.run(null);

        // then
        verify(remoteAreaJpaRepository).saveAll(batchCaptor.capture());
        List<RemoteAreaJpaEntity> savedEntities = batchCaptor.getValue();

        // 114줄 - 1 헤더 - 1 마지막 빈 줄 = 113건 (단, (전체)→"" 정규화로 행 수 변화 없음)
        assertThat(savedEntities).hasSize(113);
    }
}
