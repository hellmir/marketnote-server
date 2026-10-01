package com.personal.marketnote.community.adapter.out.persistence.profanity;

import com.personal.marketnote.common.domain.EntityStatus;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfanityWordPersistenceAdapter")
class ProfanityWordPersistenceAdapterTest {

    @Mock
    private ProfanityWordJpaRepository profanityWordJpaRepository;

    @InjectMocks
    private ProfanityWordPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        when(profanityWordJpaRepository.findAllByStatus(EntityStatus.ACTIVE))
                .thenReturn(List.of(
                        ProfanityWordJpaEntity.of("Bad"),
                        ProfanityWordJpaEntity.of("욕설"),
                        ProfanityWordJpaEntity.of("hate")
                ));
        adapter.init();
    }

    @Nested
    @DisplayName("init / reload")
    class InitReload {

        @Test
        @DisplayName("init은 ACTIVE 상태의 욕설을 모두 로드한다")
        void initLoadsAllActiveWords() {
            verify(profanityWordJpaRepository).findAllByStatus(EntityStatus.ACTIVE);
            assertThat(adapter.containsProfanity("This is bad")).isTrue();
            assertThat(adapter.containsProfanity("욕설이다")).isTrue();
            assertThat(adapter.containsProfanity("HATE you")).isTrue();
        }

        @Test
        @DisplayName("reload는 욕설 사전을 다시 적재한다")
        void reloadRefreshesDictionary() {
            when(profanityWordJpaRepository.findAllByStatus(EntityStatus.ACTIVE))
                    .thenReturn(List.of(ProfanityWordJpaEntity.of("new")));

            adapter.reload();

            assertThat(adapter.containsProfanity("This is new")).isTrue();
            assertThat(adapter.containsProfanity("This is bad")).isFalse();
        }
    }

    @Nested
    @DisplayName("containsProfanity")
    class ContainsProfanity {

        @Test
        @DisplayName("포함된 단어를 대소문자 무시로 탐지한다")
        void detectsWordCaseInsensitive() {
            assertThat(adapter.containsProfanity("BAD WORD")).isTrue();
            assertThat(adapter.containsProfanity("BaD WoRd")).isTrue();
        }

        @Test
        @DisplayName("부분 일치도 욕설로 판단한다")
        void detectsPartialMatch() {
            assertThat(adapter.containsProfanity("badword")).isTrue();
        }

        @Test
        @DisplayName("등록되지 않은 단어는 탐지하지 않는다")
        void doesNotDetectUnregisteredWord() {
            assertThat(adapter.containsProfanity("clean text")).isFalse();
        }

        @Test
        @DisplayName("한글 욕설도 탐지한다")
        void detectsKoreanWord() {
            assertThat(adapter.containsProfanity("이건 욕설 포함")).isTrue();
        }
    }
}
