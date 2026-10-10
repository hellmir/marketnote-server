package com.personal.marketnote.notification.domain.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateRendererTest {

    @Nested
    @DisplayName("render 메서드")
    class Render {

        @Test
        @DisplayName("템플릿 변수를 치환한다")
        void shouldReplaceTemplateVariables() {
            // given
            String template = "{productName} 외 {count}건이 결제되었습니다.";
            Map<String, String> variables = Map.of(
                    "productName", "비타민C",
                    "count", "2"
            );

            // when
            String result = TemplateRenderer.render(template, variables);

            // then
            assertThat(result).isEqualTo("비타민C 외 2건이 결제되었습니다.");
        }

        @Test
        @DisplayName("변수가 없는 템플릿은 그대로 반환한다")
        void shouldReturnTemplateAsIsWhenNoVariables() {
            // given
            String template = "주문이 완료되었습니다.";
            Map<String, String> variables = Map.of();

            // when
            String result = TemplateRenderer.render(template, variables);

            // then
            assertThat(result).isEqualTo("주문이 완료되었습니다.");
        }

        @Test
        @DisplayName("여러 변수를 동시에 치환한다")
        void shouldReplaceMultipleVariables() {
            // given
            String template = "{userName}님, {productName} 주문이 {status}되었습니다.";
            Map<String, String> variables = Map.of(
                    "userName", "홍길동",
                    "productName", "프로틴바",
                    "status", "확인"
            );

            // when
            String result = TemplateRenderer.render(template, variables);

            // then
            assertThat(result).isEqualTo("홍길동님, 프로틴바 주문이 확인되었습니다.");
        }

        @Test
        @DisplayName("미치환 변수가 남으면 예외가 발생한다")
        void shouldThrowExceptionWhenUnresolvedVariablesRemain() {
            // given
            String template = "{productName} 외 {count}건이 결제되었습니다.";
            Map<String, String> variables = Map.of("productName", "비타민C");

            // when & then
            assertThatThrownBy(() -> TemplateRenderer.render(template, variables))
                    .isInstanceOf(InvalidTemplateVariableException.class);
        }

        @Test
        @DisplayName("null 템플릿이면 null을 반환한다")
        void shouldReturnNullWhenTemplateIsNull() {
            // when
            String result = TemplateRenderer.render(null, Map.of());

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("치환 값에 중괄호 변수 패턴이 포함되어도 오탐하지 않고 그대로 반영한다")
        void shouldNotMisdetectBracePatternInReplacementValue() {
            // given
            String template = "{productName} 상품 문의가 답변되었습니다.";
            Map<String, String> variables = Map.of("productName", "유기농 사과 {post_id}");

            // when
            String result = TemplateRenderer.render(template, variables);

            // then
            assertThat(result).isEqualTo("유기농 사과 {post_id} 상품 문의가 답변되었습니다.");
        }

        @Test
        @DisplayName("치환 값이 빈 문자열이어도 정상 치환된다")
        void shouldReplaceWithBlankValue() {
            // given
            String template = "{prefix}{name}";
            Map<String, String> variables = Map.of("prefix", "", "name", "홍길동");

            // when
            String result = TemplateRenderer.render(template, variables);

            // then
            assertThat(result).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("치환 값에 중괄호 전용 문자가 섞여도 예외가 발생하지 않는다")
        void shouldAllowSpecialCharactersInReplacementValue() {
            // given
            String template = "메시지: {content}";
            Map<String, String> variables = Map.of("content", "테스트 {abc} {def} {ghi}");

            // when
            String result = TemplateRenderer.render(template, variables);

            // then
            assertThat(result).isEqualTo("메시지: 테스트 {abc} {def} {ghi}");
        }

        @Test
        @DisplayName("variables가 null이고 템플릿에 변수가 있으면 예외가 발생한다")
        void shouldThrowExceptionWhenVariablesNullAndTemplateHasVariables() {
            // given
            String template = "{productName} 상품";

            // when & then
            assertThatThrownBy(() -> TemplateRenderer.render(template, null))
                    .isInstanceOf(InvalidTemplateVariableException.class);
        }
    }

    @Nested
    @DisplayName("renderUrl 메서드")
    class RenderUrl {

        @Test
        @DisplayName("URL 템플릿 변수를 치환한다")
        void shouldReplaceUrlTemplateVariables() {
            // given
            String urlTemplate = "/order/{orderId}";
            Map<String, String> variables = Map.of("orderId", "12345");

            // when
            String result = TemplateRenderer.render(urlTemplate, variables);

            // then
            assertThat(result).isEqualTo("/order/12345");
        }

        @Test
        @DisplayName("쿼리 파라미터가 포함된 URL 템플릿 변수를 치환한다")
        void shouldReplaceUrlTemplateWithQueryParams() {
            // given
            String urlTemplate = "/review/create/{orderId}?pricePolicyId={pricePolicyId}";
            Map<String, String> variables = Map.of(
                    "orderId", "100",
                    "pricePolicyId", "200"
            );

            // when
            String result = TemplateRenderer.render(urlTemplate, variables);

            // then
            assertThat(result).isEqualTo("/review/create/100?pricePolicyId=200");
        }
    }
}
