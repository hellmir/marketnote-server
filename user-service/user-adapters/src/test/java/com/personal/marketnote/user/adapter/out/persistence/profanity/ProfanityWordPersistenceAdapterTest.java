package com.personal.marketnote.user.adapter.out.persistence.profanity;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.user.adapter.out.persistence.profanity.entity.ProfanityWordJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.profanity.repository.ProfanityWordJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({ProfanityWordPersistenceAdapter.class, AuditConfig.class})
class ProfanityWordPersistenceAdapterTest {

    @Autowired
    private ProfanityWordPersistenceAdapter adapter;

    @Autowired
    private ProfanityWordJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("금칙어가 포함된 닉네임이면 containsProfanity가 true를 반환한다")
    void containsProfanity_whenContainsProfanityWord_returnsTrue() {
        // given
        repository.save(ProfanityWordJpaEntity.of("바보"));
        adapter.reload();

        // when
        boolean result = adapter.containsProfanity("나는바보야");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("금칙어가 포함되지 않은 닉네임이면 containsProfanity가 false를 반환한다")
    void containsProfanity_whenNoProfanityWord_returnsFalse() {
        // given
        repository.save(ProfanityWordJpaEntity.of("바보"));
        adapter.reload();

        // when
        boolean result = adapter.containsProfanity("정상닉네임");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("앱 시작 시 ACTIVE 상태의 금칙어를 메모리에 로드한다")
    void reload_loadsActiveProfanityWords() {
        // given
        repository.save(ProfanityWordJpaEntity.of("바보"));
        repository.save(ProfanityWordJpaEntity.of("멍청이"));
        repository.save(ProfanityWordJpaEntity.of("찌질이"));
        adapter.reload();

        // when & then
        assertThat(adapter.containsProfanity("나는바보야")).isTrue();
        assertThat(adapter.containsProfanity("멍청이닉네임")).isTrue();
        assertThat(adapter.containsProfanity("찌질이녀석")).isTrue();
        assertThat(adapter.containsProfanity("정상닉네임")).isFalse();
    }
}
