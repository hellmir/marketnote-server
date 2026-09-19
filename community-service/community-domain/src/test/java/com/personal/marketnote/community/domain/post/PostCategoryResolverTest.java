package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.community.domain.post.exception.BoardOrCategoryNoValueException;
import com.personal.marketnote.community.domain.post.exception.InvalidPostCategoryForBoardException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostCategoryResolverTest {

    @Test
    @DisplayName("board가 null이면 BoardOrCategoryNoValueException이 발생한다")
    void shouldThrowExceptionWhenBoardIsNull() {
        assertThatThrownBy(() -> PostCategoryResolver.resolve(null, "ANNOUNCEMENT"))
                .isInstanceOf(BoardOrCategoryNoValueException.class);
    }

    @Test
    @DisplayName("categoryCode가 null이면 BoardOrCategoryNoValueException이 발생한다")
    void shouldThrowExceptionWhenCategoryCodeIsNull() {
        assertThatThrownBy(() -> PostCategoryResolver.resolve(Board.NOTICE, null))
                .isInstanceOf(BoardOrCategoryNoValueException.class);
    }

    @Test
    @DisplayName("categoryCode가 빈 문자열이면 BoardOrCategoryNoValueException이 발생한다")
    void shouldThrowExceptionWhenCategoryCodeIsEmpty() {
        assertThatThrownBy(() -> PostCategoryResolver.resolve(Board.NOTICE, ""))
                .isInstanceOf(BoardOrCategoryNoValueException.class);
    }

    @Test
    @DisplayName("유효한 board와 categoryCode로 올바른 PostCategory를 반환한다")
    void shouldResolvePostCategoryWhenBoardAndCategoryCodeAreValid() {
        PostCategory result = PostCategoryResolver.resolve(Board.NOTICE, "ANNOUNCEMENT");

        assertThat(result).isEqualTo(NoticePostCategory.ANNOUNCEMENT);
    }

    @Test
    @DisplayName("유효한 board에 잘못된 categoryCode이면 InvalidPostCategoryForBoardException이 발생한다")
    void shouldThrowExceptionWhenCategoryCodeIsInvalidForBoard() {
        assertThatThrownBy(() -> PostCategoryResolver.resolve(Board.NOTICE, "INVALID_CODE"))
                .isInstanceOf(InvalidPostCategoryForBoardException.class);
    }
}
