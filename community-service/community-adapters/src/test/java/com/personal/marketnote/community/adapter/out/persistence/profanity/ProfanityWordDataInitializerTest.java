package com.personal.marketnote.community.adapter.out.persistence.profanity;

import com.personal.marketnote.community.adapter.out.persistence.profanity.entity.ProfanityWordJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.profanity.repository.ProfanityWordJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfanityWordDataInitializer")
class ProfanityWordDataInitializerTest {

    @Mock
    private ProfanityWordJpaRepository profanityWordJpaRepository;

    @InjectMocks
    private ProfanityWordDataInitializer initializer;

    private final List<List<ProfanityWordJpaEntity>> savedBatches = new ArrayList<>();

    @BeforeEach
    void setUp() {
        savedBatches.clear();
    }

    private void recordSaveAllCopies() {
        doAnswer(invocation -> {
            List<ProfanityWordJpaEntity> arg = invocation.getArgument(0);
            savedBatches.add(new ArrayList<>(arg));
            return arg;
        }).when(profanityWordJpaRepository).saveAll(anyList());
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("이미 데이터가 존재하면 초기화를 건너뛴다")
        void skipsWhenDataAlreadyExists() throws Exception {
            when(profanityWordJpaRepository.count()).thenReturn(10L);

            initializer.run(new DefaultApplicationArguments());

            verify(profanityWordJpaRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("데이터가 없으면 classpath 파일을 읽어 모두 저장한다")
        void loadsAllWordsWhenEmpty() throws Exception {
            when(profanityWordJpaRepository.count()).thenReturn(0L);
            recordSaveAllCopies();

            initializer.run(new DefaultApplicationArguments());

            int totalSaved = savedBatches.stream().mapToInt(List::size).sum();
            assertThat(totalSaved).isPositive();
        }

        @Test
        @DisplayName("저장된 단어는 모두 소문자로 변환된다")
        void persistsLowercase() throws Exception {
            when(profanityWordJpaRepository.count()).thenReturn(0L);
            recordSaveAllCopies();

            initializer.run(new DefaultApplicationArguments());

            savedBatches.stream()
                    .flatMap(List::stream)
                    .map(ProfanityWordJpaEntity::getWord)
                    .forEach(word -> assertThat(word).isEqualTo(word.toLowerCase()));
        }

        @Test
        @DisplayName("BATCH_SIZE(500) 단위로 분할 저장한다")
        void persistsInBatchesOfFiveHundred() throws Exception {
            when(profanityWordJpaRepository.count()).thenReturn(0L);
            recordSaveAllCopies();

            initializer.run(new DefaultApplicationArguments());

            assertThat(savedBatches).isNotEmpty();
            for (int i = 0; i < savedBatches.size() - 1; i++) {
                assertThat(savedBatches.get(i)).hasSize(500);
            }
            assertThat(savedBatches.getLast().size()).isLessThanOrEqualTo(500);
        }
    }
}
