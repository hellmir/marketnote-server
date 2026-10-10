package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.ValueMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("마스킹이 필요한 게시판에서 생성하면 writerName이 마스킹된다")
    void shouldMaskWriterNameWhenBoardRequiresMasking() {
        PostCreateState state = createPostCreateState(Board.NOTICE, "ANNOUNCEMENT", "홍길동");

        Post post = Post.from(state);

        assertThat(post.getMaskedWriterName()).isEqualTo(ValueMasker.mask("홍길동"));
        assertThat(post.getMaskedWriterName()).isNotEqualTo("홍길동");
        assertThat(post.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("마스킹이 불필요한 게시판에서 생성하면 writerName이 그대로 유지된다")
    void shouldKeepWriterNameWhenBoardDoesNotRequireMasking() {
        PostCreateState state = createPostCreateState(Board.ONE_ON_ONE_INQUERY, "ORDER_PAYMENT", "홍길동");

        Post post = Post.from(state);

        assertThat(post.getMaskedWriterName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("PostCreateState로 생성하면 postKey가 자동 발급된다")
    void shouldGeneratePostKeyWhenCreatedFromCreateState() {
        PostCreateState state = createPostCreateState(Board.NOTICE, "ANNOUNCEMENT", "홍길동");

        Post post = Post.from(state);

        assertThat(post.getPostKey()).isNotNull();
    }

    @Test
    @DisplayName("PostCreateState로 두 번 생성하면 서로 다른 postKey가 발급된다")
    void shouldGenerateDifferentPostKeysForEachCreation() {
        PostCreateState state = createPostCreateState(Board.NOTICE, "ANNOUNCEMENT", "홍길동");

        Post first = Post.from(state);
        Post second = Post.from(state);

        assertThat(first.getPostKey()).isNotEqualTo(second.getPostKey());
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 postKey가 그대로 유지된다")
    void shouldRestorePostKeyFromSnapshotState() {
        UUID postKey = UUID.randomUUID();
        PostSnapshotState state = PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .postKey(postKey)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("공지 제목")
                .content("공지 내용")
                .status(EntityStatus.ACTIVE)
                .build();

        Post post = Post.from(state);

        assertThat(post.getPostKey()).isEqualTo(postKey);
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 모든 필드가 올바르게 매핑된다")
    void shouldRestoreAllFieldsFromSnapshotState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 4, 1, 12, 0);
        UUID postKey = UUID.randomUUID();
        PostSnapshotState state = PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .postKey(postKey)
                .parentId(null)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("공지 제목")
                .content("공지 내용")
                .isPrivate(false)
                .isPhoto(false)
                .status(EntityStatus.ACTIVE)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .orderNum(1L)
                .build();

        Post post = Post.from(state);

        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getUserId()).isEqualTo(100L);
        assertThat(post.getPostKey()).isEqualTo(postKey);
        assertThat(post.getTitle()).isEqualTo("공지 제목");
        assertThat(post.getContent()).isEqualTo("공지 내용");
        assertThat(post.getMaskedWriterName()).isEqualTo("홍*동");
        assertThat(post.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("updateReplies에 답변이 있으면 isAnswered가 true이다")
    void shouldSetIsAnsweredTrueWhenRepliesExist() {
        Post post = createActivePost();
        Post reply = createActivePost();

        post.updateReplies(List.of(reply));

        assertThat(post.isAnswered()).isTrue();
        assertThat(post.hasReplies()).isTrue();
    }

    @Test
    @DisplayName("updateReplies에 빈 리스트이면 isAnswered가 false이다")
    void shouldSetIsAnsweredFalseWhenRepliesEmpty() {
        Post post = createActivePost();

        post.updateReplies(Collections.emptyList());

        assertThat(post.isAnswered()).isFalse();
        assertThat(post.hasReplies()).isFalse();
    }

    @Test
    @DisplayName("changeExposure를 호출하면 ACTIVE에서 UNEXPOSED로 전환된다")
    void shouldToggleStatusFromActiveToUnexposed() {
        Post post = createActivePost();

        post.changeExposure();

        assertThat(post.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
        assertThat(post.isActive()).isFalse();
    }

    @Test
    @DisplayName("update를 호출하면 title과 content가 변경된다")
    void shouldUpdateTitleAndContent() {
        Post post = createActivePost();

        post.update("수정된 제목", "수정된 내용");

        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("parentId가 있으면 isReply는 true를 반환한다")
    void shouldReturnTrueWhenParentIdExists() {
        PostSnapshotState state = PostSnapshotState.builder()
                .id(2L)
                .userId(100L)
                .parentId(1L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("관리자")
                .maskedWriterName("관*자")
                .title("답변")
                .content("답변 내용")
                .status(EntityStatus.ACTIVE)
                .build();

        Post post = Post.from(state);

        assertThat(post.isReply()).isTrue();
    }

    @Test
    @DisplayName("parentId가 null이면 isReply는 false를 반환한다")
    void shouldReturnFalseWhenParentIdIsNull() {
        Post post = createActivePost();

        assertThat(post.isReply()).isFalse();
    }

    @Test
    @DisplayName("title이 null이면 hasNoTitle은 true를 반환한다")
    void shouldReturnTrueWhenTitleIsNull() {
        Post post = Post.from(PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title(null)
                .content("내용")
                .status(EntityStatus.ACTIVE)
                .build());

        assertThat(post.hasNoTitle()).isTrue();
    }

    @Test
    @DisplayName("title이 빈 문자열이면 hasNoTitle은 true를 반환한다")
    void shouldReturnTrueWhenTitleIsBlank() {
        Post post = Post.from(PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("")
                .content("내용")
                .status(EntityStatus.ACTIVE)
                .build());

        assertThat(post.hasNoTitle()).isTrue();
    }

    @Test
    @DisplayName("title이 존재하면 hasNoTitle은 false를 반환한다")
    void shouldReturnFalseWhenTitleExists() {
        Post post = createActivePost();

        assertThat(post.hasNoTitle()).isFalse();
    }

    @Test
    @DisplayName("targetType이 PRICE_POLICY이면 isPricePolicyTarget은 true를 반환한다")
    void shouldReturnTrueWhenTargetTypeIsPricePolicy() {
        Post post = Post.from(PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("문의")
                .content("내용")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(10L)
                .status(EntityStatus.ACTIVE)
                .build());

        assertThat(post.isPricePolicyTarget()).isTrue();
    }

    @Test
    @DisplayName("targetType이 null이면 isPricePolicyTarget은 false를 반환한다")
    void shouldReturnFalseWhenTargetTypeIsNull() {
        Post post = createActivePost();

        assertThat(post.isPricePolicyTarget()).isFalse();
    }

    @Test
    @DisplayName("targetId가 null이면 hasNoTargetId는 true를 반환한다")
    void shouldReturnTrueWhenTargetIdIsNull() {
        Post post = createActivePost();

        assertThat(post.hasNoTargetId()).isTrue();
    }

    @Test
    @DisplayName("targetId가 존재하면 hasNoTargetId는 false를 반환한다")
    void shouldReturnFalseWhenTargetIdExists() {
        Post post = Post.from(PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("문의")
                .content("내용")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(10L)
                .status(EntityStatus.ACTIVE)
                .build());

        assertThat(post.hasNoTargetId()).isFalse();
    }

    @Test
    @DisplayName("PostCreateState에 isImportant=true 전달 시 공지 게시글이면 isImportant가 true로 저장된다")
    void shouldSetIsImportantTrueWhenNoticeAnnouncementWithImportantTrue() {
        PostCreateState state = PostCreateState.builder()
                .userId(100L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .title("중요 공지")
                .content("내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(true)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isTrue();
    }

    @Test
    @DisplayName("PostCreateState에 isImportant=false 전달 시 isImportant는 false로 저장된다")
    void shouldSetIsImportantFalseWhenRequestedFalse() {
        PostCreateState state = PostCreateState.builder()
                .userId(100L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .title("일반 공지")
                .content("내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(false)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isFalse();
    }

    @Test
    @DisplayName("NOTICE 게시판이지만 ANNOUNCEMENT 카테고리가 아닌 게시글은 isImportant 요청이 true여도 false로 저장된다")
    void shouldForceIsImportantFalseWhenNoticeButNotAnnouncement() {
        PostCreateState state = PostCreateState.builder()
                .userId(100L)
                .board(Board.NOTICE)
                .category("EVENT")
                .writerName("홍길동")
                .title("이벤트")
                .content("이벤트 내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(true)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isFalse();
    }

    @Test
    @DisplayName("공지 게시판이 아닌 게시글(PRODUCT_INQUERY)은 isImportant 요청이 true여도 false로 저장된다")
    void shouldForceIsImportantFalseWhenProductInqueryBoard() {
        PostCreateState state = PostCreateState.builder()
                .userId(100L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .writerName("홍길동")
                .title("상품 문의")
                .content("내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(true)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isFalse();
    }

    @Test
    @DisplayName("공지 게시판이 아닌 게시글(ONE_ON_ONE_INQUERY)은 isImportant 요청이 true여도 false로 저장된다")
    void shouldForceIsImportantFalseWhenOneOnOneInqueryBoard() {
        PostCreateState state = PostCreateState.builder()
                .userId(100L)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .writerName("홍길동")
                .content("1:1 문의")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(true)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isFalse();
    }

    @Test
    @DisplayName("공지 게시판이 아닌 게시글(FAQ)은 isImportant 요청이 true여도 false로 저장된다")
    void shouldForceIsImportantFalseWhenFaqBoard() {
        PostCreateState state = PostCreateState.builder()
                .userId(100L)
                .board(Board.FAQ)
                .category("ORDER_PAYMENT")
                .writerName("관리자")
                .title("자주 묻는 질문")
                .content("FAQ 내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(true)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isFalse();
    }

    @Test
    @DisplayName("PostSnapshotState로 복원 시 isImportant=true가 그대로 복원된다")
    void shouldRestoreIsImportantTrueFromSnapshotState() {
        PostSnapshotState state = PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("중요 공지")
                .content("내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(true)
                .status(EntityStatus.ACTIVE)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isTrue();
    }

    @Test
    @DisplayName("PostSnapshotState로 복원 시 isImportant=false가 그대로 복원된다")
    void shouldRestoreIsImportantFalseFromSnapshotState() {
        PostSnapshotState state = PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("일반 공지")
                .content("내용")
                .isPrivate(false)
                .isPhoto(false)
                .isImportant(false)
                .status(EntityStatus.ACTIVE)
                .build();

        Post post = Post.from(state);

        assertThat(post.isImportant()).isFalse();
    }

    private PostCreateState createPostCreateState(Board board, String category, String writerName) {
        return PostCreateState.builder()
                .userId(100L)
                .board(board)
                .category(category)
                .writerName(writerName)
                .title("테스트 제목")
                .content("테스트 내용")
                .isPrivate(false)
                .isPhoto(false)
                .build();
    }

    private Post createActivePost() {
        return Post.from(PostSnapshotState.builder()
                .id(1L)
                .userId(100L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .writerName("홍길동")
                .maskedWriterName("홍*동")
                .title("테스트 제목")
                .content("테스트 내용")
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
