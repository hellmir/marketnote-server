package com.personal.marketnote.community.adapter.out.persistence.post.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostCreateState;
import com.personal.marketnote.community.domain.post.PostSortProperty;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, QuerydslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostHasReplyQueryTest {

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    @Nested
    @DisplayName("findByBoardAndFilters - hasReply 컬럼 정렬 (sortProperty=IS_ANSWERED)")
    class FindByBoardAndFiltersOrderByAnswered {

        @Test
        @DisplayName("내림차순 정렬 시 hasReply=true 게시글이 먼저 나오고 동일 hasReply 내에서는 id DESC로 정렬된다")
        void shouldSortHasReplyTrueFirstThenIdDescWhenDesc() {
            PostJpaEntity noReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            PostJpaEntity noReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE,
                    PostSortProperty.IS_ANSWERED
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(
                            hasReply2.getId(),
                            hasReply1.getId(),
                            noReply2.getId(),
                            noReply1.getId()
                    );
        }

        @Test
        @DisplayName("오름차순 정렬 시 hasReply=false 게시글이 먼저 나오고 동일 hasReply 내에서는 id ASC로 정렬된다")
        void shouldSortHasReplyFalseFirstThenIdAscWhenAsc() {
            PostJpaEntity noReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            PostJpaEntity noReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, false,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE,
                    PostSortProperty.IS_ANSWERED
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(
                            noReply1.getId(),
                            noReply2.getId(),
                            hasReply1.getId(),
                            hasReply2.getId()
                    );
        }
    }

    @Nested
    @DisplayName("findByBoardAndFilters - hasReply 필터")
    class FindByBoardAndFiltersAnsweredOnly {

        @Test
        @DisplayName("isAnsweredOnly=true이면 hasReply=true인 게시글만 반환된다")
        void returnsOnlyHasReplyTrueWhenAnsweredOnly() {
            PostJpaEntity hasReply = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, true, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE,
                    PostSortProperty.ID
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(hasReply.getId());
        }

        @Test
        @DisplayName("isAnsweredOnly=false이면 hasReply 여부와 무관하게 모두 반환된다")
        void returnsAllWhenAnsweredOnlyFalse() {
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE,
                    PostSortProperty.ID
            );

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("countByBoardAndFilters - hasReply 필터")
    class CountByBoardAndFilters {

        @Test
        @DisplayName("isAnsweredOnly=true이면 hasReply=true인 게시글 수만 카운트한다")
        void countsOnlyHasReplyTrueWhenAnsweredOnly() {
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            long count = postJpaRepository.countByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null,
                    null, null, true, true, true, null, EntityStatus.ACTIVE
            );

            assertThat(count).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("existsByParentIdAndStatus")
    class ExistsByParentIdAndStatus {

        @Test
        @DisplayName("부모 게시글에 ACTIVE 상태 답글이 있으면 true를 반환한다")
        void returnsTrueWhenActiveChildExists() {
            PostJpaEntity parent = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            saveReply(parent.getId(), EntityStatus.ACTIVE);
            flushAndClear();

            boolean result = postJpaRepository.existsByParentIdAndStatus(parent.getId(), EntityStatus.ACTIVE);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("부모 게시글에 ACTIVE 상태 답글이 없으면 false를 반환한다")
        void returnsFalseWhenNoActiveChild() {
            PostJpaEntity parent = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            saveReply(parent.getId(), EntityStatus.UNEXPOSED);
            flushAndClear();

            boolean result = postJpaRepository.existsByParentIdAndStatus(parent.getId(), EntityStatus.ACTIVE);

            assertThat(result).isFalse();
        }
    }

    private PostJpaEntity saveParent(Board board, String category, boolean hasReply) {
        Post post = Post.from(PostCreateState.builder()
                .userId(10L)
                .board(board)
                .category(category)
                .writerName("작성자")
                .title("테스트 제목")
                .content("테스트 내용")
                .isPrivate(false)
                .isPhoto(false)
                .build());
        PostJpaEntity entity = PostJpaEntity.from(post);
        PostJpaEntity saved = postJpaRepository.save(entity);
        if (hasReply) {
            entityManager.flush();
            entityManager.createQuery("UPDATE PostJpaEntity p SET p.hasReply = true WHERE p.id = :id")
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        return saved;
    }

    private PostJpaEntity saveReply(Long parentId, EntityStatus status) {
        Post reply = Post.from(PostCreateState.builder()
                .userId(11L)
                .parentId(parentId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("답변자")
                .content("답변 내용")
                .isPrivate(false)
                .isPhoto(false)
                .build());
        PostJpaEntity entity = PostJpaEntity.from(reply);
        PostJpaEntity saved = postJpaRepository.save(entity);
        if (!EntityStatus.ACTIVE.equals(status)) {
            entityManager.flush();
            entityManager.createQuery("UPDATE PostJpaEntity p SET p.status = :status WHERE p.id = :id")
                    .setParameter("status", status)
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        return saved;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
