package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoTemplateRendererTest {

    @Test
    @DisplayName("치환자를 변수 맵으로 렌더링한다")
    void shouldRenderTemplateWithVariables() {
        // given
        String template = "안녕하세요 #{이름}님, #{상품명} 주문이 완료되었습니다.";
        Map<String, String> variables = Map.of("이름", "홍길동", "상품명", "테스트상품");

        // when
        String result = AligoTemplateRenderer.render(template, variables);

        // then
        assertThat(result).isEqualTo("안녕하세요 홍길동님, 테스트상품 주문이 완료되었습니다.");
    }

    @Test
    @DisplayName("미해결 변수가 있으면 예외가 발생한다")
    void shouldThrowExceptionForUnresolvedVariable() {
        // given
        String template = "#{이름}님 #{주문번호} 안내";
        Map<String, String> variables = Map.of("이름", "홍길동");

        // when & then
        assertThatThrownBy(() -> AligoTemplateRenderer.render(template, variables))
                .isInstanceOf(InvalidAligoTemplateException.class)
                .hasMessageContaining("주문번호");
    }

    @Test
    @DisplayName("치환자가 없는 템플릿은 그대로 반환한다")
    void shouldReturnTemplateAsIsWhenNoVariables() {
        // given
        String template = "변수 없는 고정 메시지입니다.";

        // when
        String result = AligoTemplateRenderer.render(template, Map.of());

        // then
        assertThat(result).isEqualTo("변수 없는 고정 메시지입니다.");
    }

    @Test
    @DisplayName("null 템플릿은 null을 반환한다")
    void shouldReturnNullForNullTemplate() {
        // when
        String result = AligoTemplateRenderer.render(null, Map.of());

        // then
        assertThat(result).isNull();
    }
}
