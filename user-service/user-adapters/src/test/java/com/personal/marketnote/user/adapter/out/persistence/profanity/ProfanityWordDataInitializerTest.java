package com.personal.marketnote.user.adapter.out.persistence.profanity;

import com.personal.marketnote.user.adapter.out.persistence.profanity.entity.ProfanityWordJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.profanity.repository.ProfanityWordJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfanityWordDataInitializerTest {

    @InjectMocks
    private ProfanityWordDataInitializer initializer;

    @Mock
    private ProfanityWordJpaRepository profanityWordJpaRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @Nested
    @DisplayName("run - 욕설 단어 초기화 실행")
    class RunTest {

        @Test
        @DisplayName("이미 데이터가 존재하면 초기화를 건너뛴다")
        void shouldSkipInitializationWhenDataExists() throws Exception {
            // given
            when(profanityWordJpaRepository.count()).thenReturn(1L);

            // when
            initializer.run(applicationArguments);

            // then
            verify(profanityWordJpaRepository).count();
            verify(profanityWordJpaRepository, never()).saveAll(anyList());
            verifyNoMoreInteractions(profanityWordJpaRepository);
        }

        @Test
        @DisplayName("데이터가 없으면 classpath 리소스를 읽어 saveAll로 저장한다")
        void shouldLoadAndSaveProfanityWordsWhenEmpty() throws Exception {
            // given
            when(profanityWordJpaRepository.count()).thenReturn(0L);

            // when
            initializer.run(applicationArguments);

            // then
            verify(profanityWordJpaRepository, atLeastOnce()).saveAll(anyList());
        }

        @Test
        @DisplayName("저장된 단어는 모두 소문자로 변환된다")
        void shouldStoreWordsInLowerCase() throws Exception {
            // given - saveAll의 인수 List는 호출 후 clear()되므로 호출 시점에 스냅샷을 떠야 한다
            when(profanityWordJpaRepository.count()).thenReturn(0L);

            List<List<ProfanityWordJpaEntity>> snapshots = new java.util.ArrayList<>();
            when(profanityWordJpaRepository.saveAll(anyList())).thenAnswer(invocation -> {
                List<ProfanityWordJpaEntity> arg = invocation.getArgument(0);
                snapshots.add(new java.util.ArrayList<>(arg));
                return arg;
            });

            // when
            initializer.run(applicationArguments);

            // then
            assertThat(snapshots).isNotEmpty();
            snapshots.forEach(batch ->
                batch.forEach(entity ->
                    assertThat(entity.getWord()).isEqualTo(entity.getWord().toLowerCase())
                )
            );
        }

        @Test
        @DisplayName("배치 크기 500으로 분할 저장한다 (전체 단어 수가 500 초과인 경우 다회 호출)")
        void shouldSaveInBatchesOf500() throws Exception {
            // given - saveAll의 인수 List는 호출 후 clear()되므로 호출 시점에 스냅샷을 떠야 한다
            when(profanityWordJpaRepository.count()).thenReturn(0L);

            List<List<ProfanityWordJpaEntity>> snapshots = new java.util.ArrayList<>();
            when(profanityWordJpaRepository.saveAll(anyList())).thenAnswer(invocation -> {
                List<ProfanityWordJpaEntity> arg = invocation.getArgument(0);
                snapshots.add(new java.util.ArrayList<>(arg));
                return arg;
            });

            // when
            initializer.run(applicationArguments);

            // then
            assertThat(snapshots).isNotEmpty();
            // 마지막 배치를 제외한 나머지는 정확히 500
            for (int i = 0; i < snapshots.size() - 1; i++) {
                assertThat(snapshots.get(i)).hasSize(500);
            }
            // 마지막 배치는 1 ~ 500 사이
            assertThat(snapshots.get(snapshots.size() - 1)).hasSizeBetween(1, 500);
        }
    }
}
