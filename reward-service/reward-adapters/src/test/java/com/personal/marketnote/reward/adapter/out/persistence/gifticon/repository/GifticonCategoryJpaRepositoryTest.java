package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryJpaEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("GifticonCategoryJpaRepository 테스트")
class GifticonCategoryJpaRepositoryTest {

    @Autowired
    private GifticonCategoryJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private void persistCategory(String code, boolean exposed, Integer orderNum) {
        GifticonCategory domain = GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .categoryCode(code)
                .categoryName("카테고리 " + code)
                .displayName("표시 " + code)
                .iconUrl("http://img.test.com/icon.png")
                .exposed(exposed)
                .orderNum(orderNum)
                .build());
        em.persist(GifticonCategoryJpaEntity.from(domain));
        em.flush();
    }

    @Test
    @DisplayName("findByCategoryCode는 categoryCode로 카테고리를 조회한다")
    void shouldFindByCategoryCode() {
        persistCategory("C001", true, 1);
        em.clear();
        Optional<GifticonCategoryJpaEntity> result = repository.findByCategoryCode("C001");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findAllByOrderByOrderNumAsc는 orderNum 오름차순으로 모든 카테고리를 반환한다")
    void shouldFindAllOrderByOrderNumAsc() {
        persistCategory("C001", true, 3);
        persistCategory("C002", true, 1);
        persistCategory("C003", true, 2);
        em.clear();

        List<GifticonCategoryJpaEntity> result = repository.findAllByOrderByOrderNumAsc();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCategoryCode()).isEqualTo("C002");
        assertThat(result.get(2).getCategoryCode()).isEqualTo("C001");
    }

    @Test
    @DisplayName("findAllExposed는 노출 카테고리만 반환하며 orderNum null은 뒤로 정렬한다")
    void shouldFindAllExposed() {
        persistCategory("C001", true, 1);
        persistCategory("C002", true, null);
        persistCategory("C003", false, 2);
        persistCategory("C004", true, 3);
        em.clear();

        List<GifticonCategoryJpaEntity> result = repository.findAllExposed();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCategoryCode()).isEqualTo("C001");
        assertThat(result.get(2).getCategoryCode()).isEqualTo("C002");
    }
}
