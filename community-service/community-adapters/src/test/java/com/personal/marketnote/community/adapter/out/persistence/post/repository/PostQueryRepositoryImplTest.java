package com.personal.marketnote.community.adapter.out.persistence.post.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostCreateState;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, QuerydslConfig.class, PostQueryRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostQueryRepositoryImplTest {

    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    @Nested
    @DisplayName("findByBoardAndFilters - 기본 커서 페이지네이션")
    class FindByBoardAndFilters {

        @Test
        @DisplayName("isDesc=true일 때 cursor보다 작은 id의 게시글만 id DESC로 반환된다")
        void returnsCursorLessThanDesc() {
            PostJpaEntity first = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity second = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity third = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, third.getId(), true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(second.getId(), first.getId());
        }

        @Test
        @DisplayName("isDesc=false일 때 cursor보다 큰 id의 게시글만 id ASC로 반환된다")
        void returnsCursorGreaterThanAsc() {
            PostJpaEntity first = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity second = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity third = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, first.getId(), false,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(second.getId(), third.getId());
        }

        @Test
        @DisplayName("cursor가 null이면 전체 결과를 id DESC 순으로 반환한다")
        void returnsAllWhenCursorIsNull() {
            PostJpaEntity first = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity second = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(second.getId(), first.getId());
        }

        @Test
        @DisplayName("category가 null이면 필터 조건에서 제외되고, 값이 있으면 해당 카테고리만 반환된다")
        void filtersByCategory() {
            PostJpaEntity matched = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            saveParent(Board.PRODUCT_INQUERY, "SHIPPING", false);
            flushAndClear();

            List<PostJpaEntity> filtered = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );
            List<PostJpaEntity> all = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(filtered).extracting(PostJpaEntity::getId).containsExactly(matched.getId());
            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("targetGroupType/targetGroupId 필터가 적용된다")
        void filtersByTargetGroup() {
            PostJpaEntity matched = saveParentWithTargetGroup(
                    Board.PRODUCT_INQUERY, PostTargetGroupType.PRODUCT, 99L
            );
            saveParentWithTargetGroup(
                    Board.PRODUCT_INQUERY, PostTargetGroupType.PRODUCT, 100L
            );
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, PostTargetGroupType.PRODUCT, 99L, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(matched.getId());
        }

        @Test
        @DisplayName("isPublicOnly=true이면 isPrivate=false 게시글만 반환된다")
        void filtersByPublicOnlyTrue() {
            PostJpaEntity publicPost = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            savePrivateParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    true, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(publicPost.getId());
        }

        @Test
        @DisplayName("isPublicOnly=false이면 isPrivate 여부와 무관하게 모두 반환된다")
        void noFilterWhenPublicOnlyFalse() {
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            savePrivateParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    false, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("filterUserId 값이 있으면 해당 사용자 게시글만 반환된다")
        void filtersByUserId() {
            PostJpaEntity mine = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            saveParentWithUserId(Board.PRODUCT_INQUERY, 20L);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, 10L, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(mine.getId());
        }

        @Test
        @DisplayName("status=INACTIVE로 조회 시 비활성 게시글만 반환된다")
        void filtersByStatus() {
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity inactive = saveInactiveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION");
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.INACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(inactive.getId());
        }

        @Test
        @DisplayName("parentId가 있는 답글은 결과에 포함되지 않는다")
        void excludesRepliesFromParentQuery() {
            PostJpaEntity parent = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            saveReply(parent.getId(), EntityStatus.ACTIVE);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(parent.getId());
        }

        @Test
        @DisplayName("searchInTitle=true일 때 title에 keyword가 포함된 게시글만 반환된다")
        void searchInTitle() {
            PostJpaEntity titleMatched = saveParentWithTitleAndContent(
                    Board.PRODUCT_INQUERY, "키워드타이틀", "본문"
            );
            saveParentWithTitleAndContent(Board.PRODUCT_INQUERY, "다른 제목", "키워드본문");
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, false, "%키워드%", EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(titleMatched.getId());
        }

        @Test
        @DisplayName("searchInContent=true일 때 content에 keyword가 포함된 게시글만 반환된다")
        void searchInContent() {
            saveParentWithTitleAndContent(Board.PRODUCT_INQUERY, "제목", "일반본문");
            PostJpaEntity contentMatched = saveParentWithTitleAndContent(
                    Board.PRODUCT_INQUERY, "다른 제목", "내용키워드본문"
            );
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, false, true, "%키워드%", EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(contentMatched.getId());
        }

        @Test
        @DisplayName("searchInTitle=true, searchInContent=true일 때 제목/본문 둘 중 하나라도 일치하면 반환된다")
        void searchInTitleOrContent() {
            PostJpaEntity titleMatched = saveParentWithTitleAndContent(
                    Board.PRODUCT_INQUERY, "키워드타이틀", "본문"
            );
            PostJpaEntity contentMatched = saveParentWithTitleAndContent(
                    Board.PRODUCT_INQUERY, "다른 제목", "키워드본문"
            );
            saveParentWithTitleAndContent(Board.PRODUCT_INQUERY, "일반 제목", "일반 본문");
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, "%키워드%", EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactlyInAnyOrder(titleMatched.getId(), contentMatched.getId());
        }

        @Test
        @DisplayName("searchKeywordPattern이 null이면 검색 조건이 적용되지 않는다")
        void noSearchFilterWhenPatternIsNull() {
            saveParentWithTitleAndContent(Board.PRODUCT_INQUERY, "A 제목", "A 본문");
            saveParentWithTitleAndContent(Board.PRODUCT_INQUERY, "B 제목", "B 본문");
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByUserIdAndBoard - 사용자 게시글 커서 페이지네이션")
    class FindByUserIdAndBoard {

        @Test
        @DisplayName("isDesc=true일 때 cursor보다 작은 id의 게시글만 id DESC로 반환된다")
        void cursorLessThanDesc() {
            PostJpaEntity first = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity second = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity third = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoard(
                    10L, Board.PRODUCT_INQUERY, third.getId(), true, false,
                    true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(second.getId(), first.getId());
        }

        @Test
        @DisplayName("다른 사용자의 게시글은 반환되지 않는다")
        void excludesOtherUser() {
            PostJpaEntity mine = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            saveParentWithUserId(Board.PRODUCT_INQUERY, 20L);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoard(
                    10L, Board.PRODUCT_INQUERY, null, true, false,
                    true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(mine.getId());
        }
    }

    @Nested
    @DisplayName("findByBoardAndFiltersOrderByAnswered - hasReply 기반 동적 정렬")
    class FindByBoardAndFiltersOrderByAnswered {

        @Test
        @DisplayName("isDesc=true일 때 hasReply=true 먼저, 동일 hasReply 내에서는 id DESC로 정렬된다")
        void sortHasReplyTrueFirstIdDesc() {
            PostJpaEntity noReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            PostJpaEntity noReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFiltersOrderByAnswered(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(hasReply2.getId(), hasReply1.getId(), noReply2.getId(), noReply1.getId());
        }

        @Test
        @DisplayName("isDesc=false일 때 hasReply=false 먼저, 동일 hasReply 내에서는 id ASC로 정렬된다")
        void sortHasReplyFalseFirstIdAsc() {
            PostJpaEntity noReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            PostJpaEntity noReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity hasReply2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByBoardAndFiltersOrderByAnswered(
                    Board.PRODUCT_INQUERY, null, null, null, null, false,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(noReply1.getId(), noReply2.getId(), hasReply1.getId(), hasReply2.getId());
        }
    }

    @Nested
    @DisplayName("findByUserIdAndBoardOrderByAnswered - 사용자 게시글 hasReply 정렬")
    class FindByUserIdAndBoardOrderByAnswered {

        @Test
        @DisplayName("isDesc=true일 때 hasReply=true 먼저, 동일 hasReply 내에서는 id DESC로 정렬된다")
        void sortHasReplyTrueFirstIdDesc() {
            PostJpaEntity noReply = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity hasReply = saveParentWithUserIdAndHasReply(Board.PRODUCT_INQUERY, 10L);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoardOrderByAnswered(
                    10L, Board.PRODUCT_INQUERY, null, true, false,
                    true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(hasReply.getId(), noReply.getId());
        }

        @Test
        @DisplayName("isDesc=false일 때 hasReply=false 먼저, 동일 hasReply 내에서는 id ASC로 정렬된다")
        void sortHasReplyFalseFirstIdAsc() {
            PostJpaEntity noReply = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity hasReply = saveParentWithUserIdAndHasReply(Board.PRODUCT_INQUERY, 10L);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoardOrderByAnswered(
                    10L, Board.PRODUCT_INQUERY, null, false, false,
                    true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(noReply.getId(), hasReply.getId());
        }
    }

    @Nested
    @DisplayName("countByBoardAndFilters - 카운트")
    class CountByBoardAndFilters {

        @Test
        @DisplayName("동일 조건에서 findByBoardAndFilters 크기와 카운트 값이 일치한다")
        void equalsListSize() {
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", true);
            saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            flushAndClear();

            List<PostJpaEntity> list = postJpaRepository.findByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null, null, true,
                    null, null, false, true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );
            long count = postJpaRepository.countByBoardAndFilters(
                    Board.PRODUCT_INQUERY, null, null, null,
                    null, null, false, true, true, null, EntityStatus.ACTIVE
            );

            assertThat(count).isEqualTo(list.size());
        }

        @Test
        @DisplayName("isAnsweredOnly=true이면 hasReply=true 게시글 수만 카운트한다")
        void countsOnlyAnswered() {
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
    @DisplayName("countByUserIdAndBoard - 사용자 게시글 카운트")
    class CountByUserIdAndBoard {

        @Test
        @DisplayName("동일 조건에서 findByUserIdAndBoard 크기와 카운트 값이 일치한다")
        void equalsListSize() {
            saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            saveParentWithUserId(Board.PRODUCT_INQUERY, 20L);
            flushAndClear();

            List<PostJpaEntity> list = postJpaRepository.findByUserIdAndBoard(
                    10L, Board.PRODUCT_INQUERY, null, true, false,
                    true, true, null, EntityStatus.ACTIVE, DEFAULT_PAGEABLE
            );
            long count = postJpaRepository.countByUserIdAndBoard(
                    10L, Board.PRODUCT_INQUERY, false, true, true, null, EntityStatus.ACTIVE
            );

            assertThat(count).isEqualTo(list.size());
        }
    }

    @Nested
    @DisplayName("findByUserIdAndBoardByOffset - offset 기반 조회")
    class FindByUserIdAndBoardByOffset {

        @Test
        @DisplayName("Pageable sort에 따라 id DESC 정렬되어 반환된다")
        void sortsByPageableDesc() {
            PostJpaEntity first = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity second = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity third = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            flushAndClear();

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoardByOffset(
                    10L, Board.PRODUCT_INQUERY, EntityStatus.ACTIVE, pageable
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(third.getId(), second.getId(), first.getId());
        }

        @Test
        @DisplayName("Pageable sort에 따라 id ASC 정렬되어 반환된다")
        void sortsByPageableAsc() {
            PostJpaEntity first = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            PostJpaEntity second = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            flushAndClear();

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoardByOffset(
                    10L, Board.PRODUCT_INQUERY, EntityStatus.ACTIVE, pageable
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(first.getId(), second.getId());
        }

        @Test
        @DisplayName("parentId가 있는 답글은 결과에 포함되지 않는다")
        void excludesReplies() {
            PostJpaEntity parent = saveParentWithUserId(Board.PRODUCT_INQUERY, 10L);
            saveReplyWithUserId(parent.getId(), 10L);
            flushAndClear();

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
            List<PostJpaEntity> results = postJpaRepository.findByUserIdAndBoardByOffset(
                    10L, Board.PRODUCT_INQUERY, EntityStatus.ACTIVE, pageable
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(parent.getId());
        }
    }

    @Nested
    @DisplayName("findRepliesByParentIds - 부모별 최신 답글 조회")
    class FindRepliesByParentIds {

        @Test
        @DisplayName("부모별 MAX(id) 답글만 반환되고 id DESC로 정렬된다")
        void returnsLatestReplyPerParentOrderedByIdDesc() {
            PostJpaEntity parent1 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity parent2 = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            saveReply(parent1.getId(), EntityStatus.ACTIVE);
            PostJpaEntity parent1LatestReply = saveReply(parent1.getId(), EntityStatus.ACTIVE);
            PostJpaEntity parent2LatestReply = saveReply(parent2.getId(), EntityStatus.ACTIVE);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findRepliesByParentIds(
                    List.of(parent1.getId(), parent2.getId()), EntityStatus.ACTIVE
            );

            assertThat(results)
                    .extracting(PostJpaEntity::getId)
                    .containsExactly(parent2LatestReply.getId(), parent1LatestReply.getId());
        }

        @Test
        @DisplayName("status가 일치하지 않는 답글은 결과에 포함되지 않는다")
        void filtersByStatus() {
            PostJpaEntity parent = saveParent(Board.PRODUCT_INQUERY, "PRODUCT_QUESTION", false);
            PostJpaEntity activeReply = saveReply(parent.getId(), EntityStatus.ACTIVE);
            saveReply(parent.getId(), EntityStatus.UNEXPOSED);
            flushAndClear();

            List<PostJpaEntity> results = postJpaRepository.findRepliesByParentIds(
                    List.of(parent.getId()), EntityStatus.ACTIVE
            );

            assertThat(results).extracting(PostJpaEntity::getId).containsExactly(activeReply.getId());
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
        PostJpaEntity saved = postJpaRepository.save(PostJpaEntity.from(post));
        if (hasReply) {
            entityManager.flush();
            entityManager.createQuery("UPDATE PostJpaEntity p SET p.hasReply = true WHERE p.id = :id")
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        return saved;
    }

    private PostJpaEntity savePrivateParent(Board board, String category) {
        Post post = Post.from(PostCreateState.builder()
                .userId(10L)
                .board(board)
                .category(category)
                .writerName("작성자")
                .title("비공개 제목")
                .content("비공개 내용")
                .isPrivate(true)
                .isPhoto(false)
                .build());
        return postJpaRepository.save(PostJpaEntity.from(post));
    }

    private PostJpaEntity saveParentWithTargetGroup(
            Board board,
            PostTargetGroupType targetGroupType,
            Long targetGroupId
    ) {
        Post post = Post.from(PostCreateState.builder()
                .userId(10L)
                .board(board)
                .category("PRODUCT_QUESTION")
                .targetGroupType(targetGroupType)
                .targetGroupId(targetGroupId)
                .writerName("작성자")
                .title("타깃 그룹 제목")
                .content("타깃 그룹 내용")
                .isPrivate(false)
                .isPhoto(false)
                .build());
        return postJpaRepository.save(PostJpaEntity.from(post));
    }

    private PostJpaEntity saveParentWithUserId(Board board, Long userId) {
        Post post = Post.from(PostCreateState.builder()
                .userId(userId)
                .board(board)
                .category("PRODUCT_QUESTION")
                .writerName("작성자")
                .title("사용자 제목")
                .content("사용자 내용")
                .isPrivate(false)
                .isPhoto(false)
                .build());
        return postJpaRepository.save(PostJpaEntity.from(post));
    }

    private PostJpaEntity saveParentWithUserIdAndHasReply(Board board, Long userId) {
        PostJpaEntity saved = saveParentWithUserId(board, userId);
        entityManager.flush();
        entityManager.createQuery("UPDATE PostJpaEntity p SET p.hasReply = true WHERE p.id = :id")
                .setParameter("id", saved.getId())
                .executeUpdate();
        return saved;
    }

    private PostJpaEntity saveInactiveParent(Board board, String category) {
        PostJpaEntity saved = saveParent(board, category, false);
        entityManager.flush();
        entityManager.createQuery("UPDATE PostJpaEntity p SET p.status = :status WHERE p.id = :id")
                .setParameter("status", EntityStatus.INACTIVE)
                .setParameter("id", saved.getId())
                .executeUpdate();
        return saved;
    }

    private PostJpaEntity saveParentWithTitleAndContent(Board board, String title, String content) {
        Post post = Post.from(PostCreateState.builder()
                .userId(10L)
                .board(board)
                .category("PRODUCT_QUESTION")
                .writerName("작성자")
                .title(title)
                .content(content)
                .isPrivate(false)
                .isPhoto(false)
                .build());
        return postJpaRepository.save(PostJpaEntity.from(post));
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
        PostJpaEntity saved = postJpaRepository.save(PostJpaEntity.from(reply));
        if (!EntityStatus.ACTIVE.equals(status)) {
            entityManager.flush();
            entityManager.createQuery("UPDATE PostJpaEntity p SET p.status = :status WHERE p.id = :id")
                    .setParameter("status", status)
                    .setParameter("id", saved.getId())
                    .executeUpdate();
        }
        return saved;
    }

    private PostJpaEntity saveReplyWithUserId(Long parentId, Long userId) {
        Post reply = Post.from(PostCreateState.builder()
                .userId(userId)
                .parentId(parentId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("답변자")
                .content("답변 내용")
                .isPrivate(false)
                .isPhoto(false)
                .build());
        return postJpaRepository.save(PostJpaEntity.from(reply));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
