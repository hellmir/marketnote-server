package com.personal.marketnote.community.adapter.in.web.post.mapper;

import com.personal.marketnote.community.adapter.in.web.post.request.RegisterPostRequest;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PostRequestToCommandMapperTest {

    @Test
    @DisplayName("RegisterPostRequest의 isImportant=true가 RegisterPostCommand에 그대로 전달된다")
    void mapToCommand_isImportantTrue_passedToCommand() {
        RegisterPostRequest request = buildRequest(true);

        RegisterPostCommand command = PostRequestToCommandMapper.mapToCommand(request, 10L);

        assertThat(command.isImportant()).isTrue();
    }

    @Test
    @DisplayName("RegisterPostRequest의 isImportant=false가 RegisterPostCommand에 그대로 전달된다")
    void mapToCommand_isImportantFalse_passedToCommand() {
        RegisterPostRequest request = buildRequest(false);

        RegisterPostCommand command = PostRequestToCommandMapper.mapToCommand(request, 11L);

        assertThat(command.isImportant()).isFalse();
    }

    @Test
    @DisplayName("RegisterPostRequest의 isImportant가 null이면 RegisterPostCommand는 false로 전달된다")
    void mapToCommand_isImportantNull_defaultsToFalse() {
        RegisterPostRequest request = buildRequest(null);

        RegisterPostCommand command = PostRequestToCommandMapper.mapToCommand(request, 12L);

        assertThat(command.isImportant()).isFalse();
    }

    @Test
    @DisplayName("RegisterPostRequest의 필드들이 RegisterPostCommand에 그대로 전달된다")
    void mapToCommand_allFieldsPassed() {
        RegisterPostRequest request = buildRequest(true);

        RegisterPostCommand command = PostRequestToCommandMapper.mapToCommand(request, 13L);

        assertThat(command.userId()).isEqualTo(13L);
        assertThat(command.board()).isEqualTo(Board.NOTICE);
        assertThat(command.category()).isEqualTo("ANNOUNCEMENT");
        assertThat(command.title()).isEqualTo("제목");
        assertThat(command.content()).isEqualTo("내용");
        assertThat(command.writerName()).isEqualTo("작성자");
    }

    private RegisterPostRequest buildRequest(Boolean isImportant) {
        RegisterPostRequest request = new RegisterPostRequest();
        ReflectionTestUtils.setField(request, "board", Board.NOTICE);
        ReflectionTestUtils.setField(request, "category", "ANNOUNCEMENT");
        ReflectionTestUtils.setField(request, "writerName", "작성자");
        ReflectionTestUtils.setField(request, "title", "제목");
        ReflectionTestUtils.setField(request, "content", "내용");
        ReflectionTestUtils.setField(request, "isPrivate", false);
        ReflectionTestUtils.setField(request, "isPhoto", false);
        ReflectionTestUtils.setField(request, "isImportant", isImportant);
        return request;
    }
}
