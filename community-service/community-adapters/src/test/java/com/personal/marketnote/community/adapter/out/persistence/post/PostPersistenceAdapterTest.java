package com.personal.marketnote.community.adapter.out.persistence.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.post.repository.PostJpaRepository;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostCreateState;
import com.personal.marketnote.community.domain.post.PostFilterCategory;
import com.personal.marketnote.community.domain.post.PostFilterValue;
import com.personal.marketnote.community.domain.post.PostSearchTarget;
import com.personal.marketnote.community.domain.post.PostSnapshotState;
import com.personal.marketnote.community.domain.post.PostSortProperty;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import com.personal.marketnote.community.domain.post.Posts;
import com.personal.marketnote.community.exception.PostNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostPersistenceAdapter")
class PostPersistenceAdapterTest {

    @Mock
    private PostJpaRepository postJpaRepository;

    @InjectMocks
    private PostPersistenceAdapter adapter;

    private Post samplePost;

    @BeforeEach
    void setUp() {
        samplePost = Post.from(
                PostCreateState.builder()
                        .userId(10L)
                        .parentId(null)
                        .board(Board.PRODUCT_INQUERY)
                        .category("PRODUCT_QUESTION")
                        .targetGroupType(PostTargetGroupType.PRODUCT)
                        .targetGroupId(100L)
                        .writerName("작성자")
                        .title("질문 제목")
                        .content("질문 내용")
                        .isPrivate(false)
                        .isPhoto(false)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("게시글을 저장하고 id를 orderNum에 반영한 도메인을 반환한다")
        void returnsSavedPostWithOrderNumSetToId() {
            PostJpaEntity savedEntity = newPersistedEntity(77L, samplePost, "PRODUCT_QUESTION");
            when(postJpaRepository.save(any(PostJpaEntity.class))).thenReturn(savedEntity);

            Post result = adapter.save(samplePost);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(77L);
            assertThat(result.getOrderNum()).isEqualTo(77L);
            assertThat(savedEntity.getOrderNum()).isEqualTo(77L);
        }
    }

    @Nested
    @DisplayName("existsById")
    class ExistsById {

        @Test
        @DisplayName("리포지토리 존재 여부를 그대로 반환한다")
        void delegatesExistsToRepository() {
            when(postJpaRepository.existsById(5L)).thenReturn(true);

            assertThat(adapter.existsById(5L)).isTrue();
            verify(postJpaRepository).existsById(5L);
        }
    }

    @Nested
    @DisplayName("findPublicPosts")
    class FindPublicPosts {

        private final Pageable pageable = PageRequest.of(0, 20);

        @Test
        @DisplayName("ID 정렬 시 기본 쿼리에 필터/검색 파라미터를 전달한다")
        void delegatesToDefaultQueryForIdSort() {
            when(postJpaRepository.findByBoardAndFilters(
                    eq(Board.PRODUCT_INQUERY),
                    eq("PRODUCT_QUESTION"),
                    eq(PostTargetGroupType.PRODUCT),
                    eq(100L),
                    eq(50L),
                    eq(true),
                    eq(true),
                    eq(null),
                    eq(false),
                    eq(true),
                    eq(false),
                    eq("%keyword%"),
                    eq(EntityStatus.ACTIVE),
                    eq(pageable)
            )).thenReturn(List.of(newPersistedEntity(1L, samplePost, "PRODUCT_QUESTION")));
            when(postJpaRepository.findRepliesByParentIds(anyList(), eq(EntityStatus.ACTIVE)))
                    .thenReturn(List.of());

            Posts result = adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    "PRODUCT_QUESTION",
                    PostTargetGroupType.PRODUCT,
                    100L,
                    50L,
                    pageable,
                    true,
                    PostSortProperty.ID,
                    null,
                    PostFilterCategory.IS_PUBLIC,
                    PostFilterValue.TRUE,
                    PostSearchTarget.TITLE,
                    "KeyWord"
            );

            assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("IS_ANSWERED 정렬 시 답변순 쿼리로 분기한다")
        void branchesToAnsweredSortQuery() {
            when(postJpaRepository.findByBoardAndFiltersOrderByAnswered(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of());

            Posts result = adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    null,
                    null,
                    null,
                    null,
                    pageable,
                    true,
                    PostSortProperty.IS_ANSWERED,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            assertThat(result.size()).isZero();
            verify(postJpaRepository, never()).findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("IS_MINE 필터 시 filterUserId를 현재 userId로 설정한다")
        void setsFilterUserIdWhenIsMine() {
            ArgumentCaptor<Long> filterUserIdCaptor = ArgumentCaptor.forClass(Long.class);
            when(postJpaRepository.findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(),
                    filterUserIdCaptor.capture(),
                    anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of());

            adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    null,
                    null,
                    null,
                    null,
                    pageable,
                    true,
                    PostSortProperty.ID,
                    777L,
                    PostFilterCategory.IS_MINE,
                    PostFilterValue.TRUE,
                    null,
                    null
            );

            assertThat(filterUserIdCaptor.getValue()).isEqualTo(777L);
        }

        @Test
        @DisplayName("IS_ANSWERED 필터 시 isAnsweredOnly true로 전달한다")
        void setsIsAnsweredOnlyFlag() {
            ArgumentCaptor<Boolean> isAnsweredCaptor = ArgumentCaptor.forClass(Boolean.class);
            when(postJpaRepository.findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(),
                    isAnsweredCaptor.capture(),
                    anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of());

            adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    null,
                    null,
                    null,
                    null,
                    pageable,
                    true,
                    PostSortProperty.ID,
                    1L,
                    PostFilterCategory.IS_ANSWERED,
                    PostFilterValue.TRUE,
                    null,
                    null
            );

            assertThat(isAnsweredCaptor.getValue()).isTrue();
        }

        @Test
        @DisplayName("검색어가 없으면 searchKeywordPattern으로 null을 전달한다")
        void passesNullPatternWhenKeywordMissing() {
            ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
            when(postJpaRepository.findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(),
                    patternCaptor.capture(),
                    any(), any()
            )).thenReturn(List.of());

            adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    null, null, null, null,
                    pageable,
                    true,
                    PostSortProperty.ID,
                    null, null, null, null,
                    null
            );

            assertThat(patternCaptor.getValue()).isNull();
        }

        @Test
        @DisplayName("searchTarget 미지정 시 title/content 양쪽 검색 플래그 모두 true로 전달한다")
        void searchesBothWhenTargetOmitted() {
            ArgumentCaptor<Boolean> inTitle = ArgumentCaptor.forClass(Boolean.class);
            ArgumentCaptor<Boolean> inContent = ArgumentCaptor.forClass(Boolean.class);
            when(postJpaRepository.findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(), anyBoolean(),
                    inTitle.capture(), inContent.capture(),
                    any(), any(), any()
            )).thenReturn(List.of());

            adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    null, null, null, null,
                    pageable,
                    true,
                    PostSortProperty.ID,
                    null, null, null,
                    null,
                    "abc"
            );

            assertThat(inTitle.getValue()).isTrue();
            assertThat(inContent.getValue()).isTrue();
        }

        @Test
        @DisplayName("searchTarget=CONTENT 지정 시 content 검색만 true로 전달한다")
        void searchesOnlyContentWhenTargetContent() {
            ArgumentCaptor<Boolean> inTitle = ArgumentCaptor.forClass(Boolean.class);
            ArgumentCaptor<Boolean> inContent = ArgumentCaptor.forClass(Boolean.class);
            when(postJpaRepository.findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(), anyBoolean(),
                    inTitle.capture(), inContent.capture(),
                    any(), any(), any()
            )).thenReturn(List.of());

            adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY,
                    null, null, null, null,
                    pageable,
                    true,
                    PostSortProperty.ID,
                    null, null, null,
                    PostSearchTarget.CONTENT,
                    "abc"
            );

            assertThat(inTitle.getValue()).isFalse();
            assertThat(inContent.getValue()).isTrue();
        }

        @Test
        @DisplayName("부모 게시글이 조회되면 대댓글을 parentId 기준으로 묶어 posts에 추가한다")
        void attachesRepliesByParentId() {
            PostJpaEntity parent = newPersistedEntity(1L, samplePost, "PRODUCT_QUESTION");
            PostJpaEntity reply = replyEntity(2L, 1L);

            when(postJpaRepository.findByBoardAndFilters(
                    any(), any(), any(), any(), any(), anyBoolean(), any(), any(),
                    anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of(parent));
            when(postJpaRepository.findRepliesByParentIds(List.of(1L), EntityStatus.ACTIVE))
                    .thenReturn(List.of(reply));

            Posts result = adapter.findPublicPosts(
                    Board.PRODUCT_INQUERY, null, null, null, null, pageable,
                    true, PostSortProperty.ID, null, null, null, null, null
            );

            assertThat(result.size()).isEqualTo(1);
            Post parentDomain = result.subList(0, 1).getFirst();
            assertThat(parentDomain.hasReplies()).isTrue();
            assertThat(parentDomain.getReplies()).hasSize(1);
            assertThat(parentDomain.getReplies().getFirst().getId()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("findUserPosts")
    class FindUserPosts {

        private final Pageable pageable = PageRequest.of(0, 20);

        @Test
        @DisplayName("IS_ANSWERED 정렬 시 유저 답변순 쿼리로 분기한다")
        void branchesToUserAnsweredSortQuery() {
            when(postJpaRepository.findByUserIdAndBoardOrderByAnswered(
                    eq(9L), eq(Board.PRODUCT_INQUERY), any(), anyBoolean(),
                    anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of());

            Posts result = adapter.findUserPosts(
                    9L, Board.PRODUCT_INQUERY, null, pageable, true,
                    PostSortProperty.IS_ANSWERED,
                    PostFilterCategory.IS_ANSWERED, PostFilterValue.TRUE,
                    null, null
            );

            assertThat(result.size()).isZero();
            verify(postJpaRepository, never()).findByUserIdAndBoard(
                    any(), any(), any(), anyBoolean(), anyBoolean(),
                    anyBoolean(), anyBoolean(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("ID 정렬 시 유저 기본 쿼리에 필터 파라미터를 전달한다")
        void delegatesToUserDefaultQueryForIdSort() {
            ArgumentCaptor<Boolean> isAnsweredCaptor = ArgumentCaptor.forClass(Boolean.class);
            when(postJpaRepository.findByUserIdAndBoard(
                    eq(9L), eq(Board.PRODUCT_INQUERY), eq(null), eq(true),
                    isAnsweredCaptor.capture(),
                    anyBoolean(), anyBoolean(), any(), any(), any()
            )).thenReturn(List.of());

            adapter.findUserPosts(
                    9L, Board.PRODUCT_INQUERY, null, pageable, true,
                    PostSortProperty.ID,
                    PostFilterCategory.IS_ANSWERED, PostFilterValue.TRUE,
                    null, null
            );

            assertThat(isAnsweredCaptor.getValue()).isTrue();
        }
    }

    @Nested
    @DisplayName("countPublicPosts")
    class CountPublicPosts {

        @Test
        @DisplayName("필터 조건을 분해해 count 쿼리에 전달한다")
        void delegatesCountToRepository() {
            ArgumentCaptor<Boolean> isPublicOnlyCaptor = ArgumentCaptor.forClass(Boolean.class);
            ArgumentCaptor<Long> filterUserIdCaptor = ArgumentCaptor.forClass(Long.class);
            when(postJpaRepository.countByBoardAndFilters(
                    eq(Board.PRODUCT_INQUERY), eq("PRODUCT_QUESTION"),
                    eq(PostTargetGroupType.PRODUCT), eq(100L),
                    isPublicOnlyCaptor.capture(),
                    filterUserIdCaptor.capture(),
                    anyBoolean(), anyBoolean(), anyBoolean(), any(), eq(EntityStatus.ACTIVE)
            )).thenReturn(5L);

            long count = adapter.countPublicPosts(
                    Board.PRODUCT_INQUERY, "PRODUCT_QUESTION",
                    PostTargetGroupType.PRODUCT, 100L,
                    777L,
                    PostFilterCategory.IS_PUBLIC, PostFilterValue.TRUE,
                    PostSearchTarget.TITLE, "word"
            );

            assertThat(count).isEqualTo(5L);
            assertThat(isPublicOnlyCaptor.getValue()).isTrue();
            assertThat(filterUserIdCaptor.getValue()).isNull();
        }
    }

    @Nested
    @DisplayName("countUserPosts")
    class CountUserPosts {

        @Test
        @DisplayName("사용자 및 보드 조건으로 count 쿼리를 호출한다")
        void delegatesUserCountToRepository() {
            when(postJpaRepository.countByUserIdAndBoard(
                    eq(9L), eq(Board.PRODUCT_INQUERY), anyBoolean(),
                    anyBoolean(), anyBoolean(), any(), eq(EntityStatus.ACTIVE)
            )).thenReturn(3L);

            long count = adapter.countUserPosts(
                    9L, Board.PRODUCT_INQUERY,
                    null, null,
                    null, null
            );

            assertThat(count).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("findUserPostsByOffset")
    class FindUserPostsByOffset {

        @Test
        @DisplayName("내림차순 정렬 시 DESC PageRequest로 쿼리를 호출한다")
        void buildsDescPageableWhenIsDesc() {
            PostJpaEntity entity = newPersistedEntity(1L, samplePost, "PRODUCT_QUESTION");
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(postJpaRepository.findByUserIdAndBoardByOffset(
                    eq(9L), eq(Board.PRODUCT_INQUERY), eq(EntityStatus.ACTIVE), pageableCaptor.capture()
            )).thenReturn(List.of(entity));
            when(postJpaRepository.findRepliesByParentIds(anyList(), eq(EntityStatus.ACTIVE)))
                    .thenReturn(List.of());

            Posts result = adapter.findUserPostsByOffset(
                    9L, Board.PRODUCT_INQUERY, 2, 30, true, PostSortProperty.ID
            );

            assertThat(result.size()).isEqualTo(1);
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(30);
            Sort.Order order = pageable.getSort().getOrderFor("id");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("오름차순 정렬 시 ASC PageRequest로 쿼리를 호출한다")
        void buildsAscPageableWhenIsNotDesc() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(postJpaRepository.findByUserIdAndBoardByOffset(
                    any(), any(), any(), pageableCaptor.capture()
            )).thenReturn(List.of());

            adapter.findUserPostsByOffset(
                    9L, Board.PRODUCT_INQUERY, 1, 10, false, PostSortProperty.ORDER_NUM
            );

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            Sort.Order order = pageable.getSort().getOrderFor("orderNum");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("findByIdWithReplies")
    class FindByIdWithReplies {

        @Test
        @DisplayName("게시글이 존재하면 대댓글 목록을 로드해 addReplies로 부착한다")
        void loadsRepliesForExistingPost() {
            PostJpaEntity parent = newPersistedEntity(10L, samplePost, "PRODUCT_QUESTION");
            PostJpaEntity reply = replyEntity(20L, 10L);
            when(postJpaRepository.findById(10L)).thenReturn(Optional.of(parent));
            when(postJpaRepository.findRepliesByParentIds(List.of(10L), EntityStatus.ACTIVE))
                    .thenReturn(List.of(reply));

            Optional<Post> result = adapter.findByIdWithReplies(10L);

            assertThat(result).isPresent();
            assertThat(result.get().getReplies()).hasSize(1);
            assertThat(result.get().getReplies().getFirst().getId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("게시글이 없으면 빈 Optional을 반환하고 대댓글을 조회하지 않는다")
        void returnsEmptyAndSkipsReplyLookup() {
            when(postJpaRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Post> result = adapter.findByIdWithReplies(999L);

            assertThat(result).isEmpty();
            verify(postJpaRepository, never()).findRepliesByParentIds(anyList(), any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("엔티티가 존재하면 도메인으로 매핑해 반환한다")
        void mapsEntityToDomain() {
            PostJpaEntity entity = newPersistedEntity(1L, samplePost, "PRODUCT_QUESTION");
            when(postJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Optional<Post> result = adapter.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("엔티티가 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            when(postJpaRepository.findById(2L)).thenReturn(Optional.empty());

            assertThat(adapter.findById(2L)).isEmpty();
        }

        @Test
        @DisplayName("중요 공지 엔티티를 조회하면 isImportant=true로 매핑된다")
        void mapsIsImportantTrue() {
            Post importantNotice = Post.from(
                    PostCreateState.builder()
                            .userId(10L)
                            .board(Board.NOTICE)
                            .category("ANNOUNCEMENT")
                            .writerName("관리자")
                            .title("중요 공지")
                            .content("내용")
                            .isPrivate(false)
                            .isPhoto(false)
                            .isImportant(true)
                            .build()
            );
            PostJpaEntity entity = newPersistedEntity(30L, importantNotice, "ANNOUNCEMENT");
            when(postJpaRepository.findById(30L)).thenReturn(Optional.of(entity));

            Optional<Post> result = adapter.findById(30L);

            assertThat(result).isPresent();
            assertThat(result.get().isImportant()).isTrue();
        }

        @Test
        @DisplayName("일반 공지 엔티티를 조회하면 isImportant=false로 매핑된다")
        void mapsIsImportantFalseForRegularNotice() {
            Post regularNotice = Post.from(
                    PostCreateState.builder()
                            .userId(10L)
                            .board(Board.NOTICE)
                            .category("ANNOUNCEMENT")
                            .writerName("관리자")
                            .title("일반 공지")
                            .content("내용")
                            .isPrivate(false)
                            .isPhoto(false)
                            .isImportant(false)
                            .build()
            );
            PostJpaEntity entity = newPersistedEntity(31L, regularNotice, "ANNOUNCEMENT");
            when(postJpaRepository.findById(31L)).thenReturn(Optional.of(entity));

            Optional<Post> result = adapter.findById(31L);

            assertThat(result).isPresent();
            assertThat(result.get().isImportant()).isFalse();
        }
    }

    @Nested
    @DisplayName("save - isImportant")
    class SaveIsImportant {

        @Test
        @DisplayName("중요 공지 Post를 저장하면 엔티티에 isImportant=true가 매핑된다")
        void savesIsImportantTrueIntoEntity() {
            Post importantNotice = Post.from(
                    PostCreateState.builder()
                            .userId(10L)
                            .board(Board.NOTICE)
                            .category("ANNOUNCEMENT")
                            .writerName("관리자")
                            .title("중요 공지")
                            .content("내용")
                            .isPrivate(false)
                            .isPhoto(false)
                            .isImportant(true)
                            .build()
            );
            ArgumentCaptor<PostJpaEntity> entityCaptor = ArgumentCaptor.forClass(PostJpaEntity.class);
            PostJpaEntity savedEntity = newPersistedEntity(50L, importantNotice, "ANNOUNCEMENT");
            when(postJpaRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            adapter.save(importantNotice);

            assertThat(entityCaptor.getValue().isImportant()).isTrue();
        }

        @Test
        @DisplayName("일반 공지 Post를 저장하면 엔티티에 isImportant=false가 매핑된다")
        void savesIsImportantFalseIntoEntity() {
            Post regularNotice = Post.from(
                    PostCreateState.builder()
                            .userId(10L)
                            .board(Board.NOTICE)
                            .category("ANNOUNCEMENT")
                            .writerName("관리자")
                            .title("일반 공지")
                            .content("내용")
                            .isPrivate(false)
                            .isPhoto(false)
                            .isImportant(false)
                            .build()
            );
            ArgumentCaptor<PostJpaEntity> entityCaptor = ArgumentCaptor.forClass(PostJpaEntity.class);
            PostJpaEntity savedEntity = newPersistedEntity(51L, regularNotice, "ANNOUNCEMENT");
            when(postJpaRepository.save(entityCaptor.capture())).thenReturn(savedEntity);

            adapter.save(regularNotice);

            assertThat(entityCaptor.getValue().isImportant()).isFalse();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("엔티티를 조회해 도메인 상태/본문으로 업데이트한다")
        void updatesEntityFromDomain() {
            PostJpaEntity entity = newPersistedEntity(1L, samplePost, "PRODUCT_QUESTION");
            when(postJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            Post persisted = Post.from(
                    PostSnapshotState.builder()
                            .id(1L)
                            .userId(samplePost.getUserId())
                            .postKey(samplePost.getPostKey())
                            .board(samplePost.getBoard())
                            .category("PRODUCT_QUESTION")
                            .writerName(samplePost.getWriterName())
                            .maskedWriterName(samplePost.getMaskedWriterName())
                            .title("updated title")
                            .content("updated content")
                            .isPrivate(false)
                            .isPhoto(false)
                            .status(EntityStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .modifiedAt(LocalDateTime.now())
                            .orderNum(1L)
                            .build()
            );
            persisted.update("updated title", "updated content");

            adapter.update(persisted);

            assertThat(entity.getTitle()).isEqualTo("updated title");
            assertThat(entity.getContent()).isEqualTo("updated content");
        }

        @Test
        @DisplayName("엔티티가 없으면 PostNotFoundException을 던진다")
        void throwsWhenEntityMissing() {
            when(postJpaRepository.findById(404L)).thenReturn(Optional.empty());

            Post missing = Post.from(
                    PostSnapshotState.builder()
                            .id(404L)
                            .userId(samplePost.getUserId())
                            .postKey(UUID.randomUUID())
                            .board(samplePost.getBoard())
                            .category("PRODUCT_QUESTION")
                            .writerName(samplePost.getWriterName())
                            .maskedWriterName(samplePost.getMaskedWriterName())
                            .title("t")
                            .content("c")
                            .isPrivate(false)
                            .isPhoto(false)
                            .status(EntityStatus.ACTIVE)
                            .build()
            );

            assertThatThrownBy(() -> adapter.update(missing))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    private PostJpaEntity newPersistedEntity(Long id, Post post, String category) {
        PostJpaEntity entity = PostJpaEntity.from(post);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "status", EntityStatus.ACTIVE);
        ReflectionTestUtils.setField(entity, "category", category);
        ReflectionTestUtils.setField(entity, "writerName", post.getWriterName());
        ReflectionTestUtils.setField(entity, "maskedWriterName", post.getMaskedWriterName());
        return entity;
    }

    private PostJpaEntity replyEntity(Long id, Long parentId) {
        Post reply = Post.from(
                PostCreateState.builder()
                        .userId(11L)
                        .parentId(parentId)
                        .board(Board.PRODUCT_INQUERY)
                        .category("PRODUCT_QUESTION")
                        .targetGroupType(PostTargetGroupType.PRODUCT)
                        .targetGroupId(100L)
                        .writerName("답변자")
                        .title("대댓글")
                        .content("대댓글 내용")
                        .isPrivate(false)
                        .isPhoto(false)
                        .build()
        );
        return newPersistedEntity(id, reply, "PRODUCT_QUESTION");
    }
}
