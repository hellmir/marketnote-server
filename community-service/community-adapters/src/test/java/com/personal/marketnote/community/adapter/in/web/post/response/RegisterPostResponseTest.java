package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterPostResponseTest {

    @Test
    @DisplayName("RegisterPostResult로부터 id와 postKey가 Response에 매핑된다")
    void shouldMapIdAndPostKeyFromResult() {
        Long id = 10L;
        UUID postKey = UUID.randomUUID();
        RegisterPostResult result = new RegisterPostResult(id, postKey);

        RegisterPostResponse response = RegisterPostResponse.from(result);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.postKey()).isEqualTo(postKey);
    }
}
