package com.personal.marketnote.product.adapter.out.persistence.category.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.category.entity.CategoryJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
class CategoryJpaRepositoryTest {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("findActiveByParentId")
    class FindActiveByParentId {

        @Test
        @DisplayName("parentId가 null이면 최상위 카테고리를 조회한다")
        void returnsRootCategoriesWhenParentIdIsNull() {
            CategoryJpaEntity root1 = saveCategory(null, "루트1");
            CategoryJpaEntity root2 = saveCategory(null, "루트2");
            saveCategory(root1.getId(), "자식1");
            entityManager.flush();
            entityManager.clear();

            List<CategoryJpaEntity> results = categoryJpaRepository.findActiveByParentId(null);

            assertThat(results).extracting(CategoryJpaEntity::getId)
                    .containsExactly(root1.getId(), root2.getId());
        }

        @Test
        @DisplayName("parentId가 지정되면 해당 부모의 자식 카테고리만 조회한다")
        void returnsChildrenOfSpecifiedParent() {
            CategoryJpaEntity root = saveCategory(null, "루트");
            CategoryJpaEntity child1 = saveCategory(root.getId(), "자식1");
            CategoryJpaEntity child2 = saveCategory(root.getId(), "자식2");
            saveCategory(null, "다른 루트");
            entityManager.flush();
            entityManager.clear();

            List<CategoryJpaEntity> results = categoryJpaRepository.findActiveByParentId(root.getId());

            assertThat(results).extracting(CategoryJpaEntity::getId)
                    .containsExactly(child1.getId(), child2.getId());
        }

        @Test
        @DisplayName("INACTIVE 상태의 카테고리는 제외한다")
        void excludesInactiveCategories() {
            CategoryJpaEntity active = saveCategory(null, "활성");
            CategoryJpaEntity inactive = saveCategory(null, "비활성");
            entityManager.createNativeQuery("UPDATE category SET status = 'INACTIVE' WHERE id = :id")
                    .setParameter("id", inactive.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            List<CategoryJpaEntity> results = categoryJpaRepository.findActiveByParentId(null);

            assertThat(results).extracting(CategoryJpaEntity::getId)
                    .containsExactly(active.getId());
        }

        @Test
        @DisplayName("ID 오름차순으로 정렬한다")
        void sortsByIdAsc() {
            CategoryJpaEntity c1 = saveCategory(null, "카테고리A");
            CategoryJpaEntity c2 = saveCategory(null, "카테고리B");
            CategoryJpaEntity c3 = saveCategory(null, "카테고리C");
            entityManager.flush();
            entityManager.clear();

            List<CategoryJpaEntity> results = categoryJpaRepository.findActiveByParentId(null);

            assertThat(results).extracting(CategoryJpaEntity::getId)
                    .containsExactly(c1.getId(), c2.getId(), c3.getId());
        }
    }

    @Nested
    @DisplayName("findAllByIdInAndStatus")
    class FindAllByIdInAndStatus {

        @Test
        @DisplayName("ID 목록과 상태가 일치하는 카테고리를 조회한다")
        void returnsMatchingCategories() {
            CategoryJpaEntity active1 = saveCategory(null, "활성1");
            CategoryJpaEntity active2 = saveCategory(null, "활성2");
            entityManager.flush();
            entityManager.clear();

            List<CategoryJpaEntity> results = categoryJpaRepository.findAllByIdInAndStatus(
                    List.of(active1.getId(), active2.getId()), EntityStatus.ACTIVE
            );

            assertThat(results).extracting(CategoryJpaEntity::getId)
                    .containsExactlyInAnyOrder(active1.getId(), active2.getId());
        }

        @Test
        @DisplayName("상태가 다른 카테고리는 제외한다")
        void excludesDifferentStatus() {
            CategoryJpaEntity active = saveCategory(null, "활성");
            CategoryJpaEntity inactive = saveCategory(null, "비활성");
            entityManager.createNativeQuery("UPDATE category SET status = 'INACTIVE' WHERE id = :id")
                    .setParameter("id", inactive.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            List<CategoryJpaEntity> results = categoryJpaRepository.findAllByIdInAndStatus(
                    List.of(active.getId(), inactive.getId()), EntityStatus.ACTIVE
            );

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo(active.getId());
        }
    }

    @Nested
    @DisplayName("existsByParentCategoryIdAndStatus")
    class ExistsByParentCategoryIdAndStatus {

        @Test
        @DisplayName("자식 카테고리가 존재하면 true를 반환한다")
        void returnsTrueWhenChildExists() {
            CategoryJpaEntity parent = saveCategory(null, "부모");
            saveCategory(parent.getId(), "자식");
            entityManager.flush();
            entityManager.clear();

            boolean result = categoryJpaRepository
                    .existsByParentCategoryIdAndStatus(parent.getId(), EntityStatus.ACTIVE);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("자식 카테고리가 없으면 false를 반환한다")
        void returnsFalseWhenNoChild() {
            CategoryJpaEntity parent = saveCategory(null, "부모");
            entityManager.flush();
            entityManager.clear();

            boolean result = categoryJpaRepository
                    .existsByParentCategoryIdAndStatus(parent.getId(), EntityStatus.ACTIVE);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("INACTIVE 자식만 존재하면 ACTIVE 검색에서 false를 반환한다")
        void returnsFalseWhenOnlyInactiveChildren() {
            CategoryJpaEntity parent = saveCategory(null, "부모");
            CategoryJpaEntity child = saveCategory(parent.getId(), "자식");
            entityManager.createNativeQuery("UPDATE category SET status = 'INACTIVE' WHERE id = :id")
                    .setParameter("id", child.getId())
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();

            boolean result = categoryJpaRepository
                    .existsByParentCategoryIdAndStatus(parent.getId(), EntityStatus.ACTIVE);

            assertThat(result).isFalse();
        }
    }

    private CategoryJpaEntity saveCategory(Long parentId, String name) {
        return categoryJpaRepository.save(CategoryJpaEntity.of(parentId, name));
    }
}
