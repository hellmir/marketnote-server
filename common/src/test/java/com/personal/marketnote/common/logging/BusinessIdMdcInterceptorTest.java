package com.personal.marketnote.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessIdMdcInterceptorTest {

    private BusinessIdMdcInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor = new BusinessIdMdcInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        Method method = SampleHandler.class.getMethod("handle");
        handlerMethod = new HandlerMethod(new SampleHandler(), method);
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증된 principal 이 존재하면 preHandle 에서 userId 를 MDC 에 enrich 한다")
    void shouldEnrichUserIdWhenAuthenticated() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-1234", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isEqualTo("user-1234");
    }

    @Test
    @DisplayName("AnonymousAuthenticationToken 인 경우 userId 를 MDC 에 enrich 하지 않는다")
    void shouldNotEnrichUserIdWhenAnonymous() {
        // given
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("Authentication 이 null 이면 userId 를 MDC 에 enrich 하지 않는다")
    void shouldNotEnrichUserIdWhenAuthenticationNull() {
        // given (SecurityContext 비워둠)

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("Authentication.isAuthenticated() 가 false 이면 userId 를 enrich 하지 않는다")
    void shouldNotEnrichUserIdWhenNotAuthenticated() {
        // given
        AbstractAuthenticationToken token = new UsernamePasswordAuthenticationToken("user-x", null);
        token.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(token);

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("Authentication.getName() 이 blank 이면 userId 를 enrich 하지 않는다")
    void shouldNotEnrichUserIdWhenNameBlank() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "   ", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("URI template variables 의 화이트리스트 키(orderId, paymentKey)는 MDC 에 enrich 한다")
    void shouldEnrichWhitelistedPathVariables() {
        // given
        Map<String, String> vars = new HashMap<>();
        vars.put(BusinessIdMdcKeys.ORDER_ID, "123");
        vars.put(BusinessIdMdcKeys.PAYMENT_KEY, "PAY001");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(MDC.get(BusinessIdMdcKeys.ORDER_ID)).isEqualTo("123");
        assertThat(MDC.get(BusinessIdMdcKeys.PAYMENT_KEY)).isEqualTo("PAY001");
    }

    @Test
    @DisplayName("화이트리스트에 없는 path variable(id, categoryId, pricePolicyId)은 MDC 에 enrich 하지 않는다")
    void shouldIgnoreNonWhitelistedPathVariables() {
        // given
        Map<String, String> vars = new HashMap<>();
        vars.put("id", "999");
        vars.put("categoryId", "10");
        vars.put("pricePolicyId", "55");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(MDC.get("id")).isNull();
        assertThat(MDC.get("categoryId")).isNull();
        assertThat(MDC.get("pricePolicyId")).isNull();
    }

    @Test
    @DisplayName("path variable 값이 빈 문자열이면 enrich 를 생략한다")
    void shouldSkipEnrichWhenPathVariableBlank() {
        // given
        Map<String, String> vars = new HashMap<>();
        vars.put(BusinessIdMdcKeys.ORDER_ID, "");
        vars.put(BusinessIdMdcKeys.PRODUCT_ID, "   ");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(MDC.get(BusinessIdMdcKeys.ORDER_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.PRODUCT_ID)).isNull();
    }

    @Test
    @DisplayName("path variable 값이 null 이면 enrich 를 생략한다")
    void shouldSkipEnrichWhenPathVariableNull() {
        // given
        Map<String, Object> vars = new HashMap<>();
        vars.put(BusinessIdMdcKeys.SELLER_ID, null);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(MDC.get(BusinessIdMdcKeys.SELLER_ID)).isNull();
    }

    @Test
    @DisplayName("URI template variables attribute 가 없어도 정상 동작한다")
    void shouldHandleMissingUriTemplateVariablesAttribute() {
        // given (request 에 attribute 미설정)

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.ORDER_ID)).isNull();
    }

    @Test
    @DisplayName("URI template variables attribute 가 Map 이 아닌 타입이면 path variable enrich 을 시도하지 않는다")
    void shouldSkipEnrichWhenAttributeNotMap() {
        // given
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, "not-a-map");

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.ORDER_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.PAYMENT_KEY)).isNull();
    }

    @Test
    @DisplayName("handler 가 HandlerMethod 가 아니면(정적 리소스 등) enrich 를 시도하지 않는다")
    void shouldSkipEnrichWhenHandlerIsNotHandlerMethod() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-9999", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        Map<String, String> vars = new HashMap<>();
        vars.put(BusinessIdMdcKeys.ORDER_ID, "456");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.ORDER_ID)).isNull();
    }

    @Test
    @DisplayName("path variable 에 CRLF/제어 문자가 포함되면 _ 로 치환되어 MDC 에 저장된다")
    void shouldSanitizeCrlfInPathVariable() {
        // given
        String malicious = "order-1\r\nFAKE_LOG=injected";
        Map<String, String> vars = new HashMap<>();
        vars.put(BusinessIdMdcKeys.ORDER_ID, malicious);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        interceptor.preHandle(request, response, handlerMethod);

        // then
        String stored = MDC.get(BusinessIdMdcKeys.ORDER_ID);
        assertThat(stored).doesNotContain("\r").doesNotContain("\n").doesNotContain("");
        assertThat(stored).startsWith("order-1");
        assertThat(stored).contains("_");
    }

    @Test
    @DisplayName("Authentication.getName() 에 CRLF 가 포함되면 _ 로 치환되어 MDC 에 저장된다")
    void shouldSanitizeCrlfInAuthenticationName() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user\r\ninjected", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        interceptor.preHandle(request, response, handlerMethod);

        // then
        String stored = MDC.get(BusinessIdMdcKeys.USER_ID);
        assertThat(stored).doesNotContain("\r").doesNotContain("\n");
        assertThat(stored).startsWith("user");
    }

    @Test
    @DisplayName("afterCompletion 은 화이트리스트 6 개 키를 모두 MDC 에서 제거하고 traceId 는 건드리지 않는다")
    void shouldRemoveAllWhitelistKeysOnAfterCompletion() {
        // given
        MDC.put(BusinessIdMdcKeys.USER_ID, "user-1");
        MDC.put(BusinessIdMdcKeys.ORDER_ID, "order-1");
        MDC.put(BusinessIdMdcKeys.PAYMENT_KEY, "pay-1");
        MDC.put(BusinessIdMdcKeys.SELLER_ID, "seller-1");
        MDC.put(BusinessIdMdcKeys.PRODUCT_ID, "product-1");
        MDC.put(BusinessIdMdcKeys.EVENT_ID, "event-1");
        MDC.put("traceId", "trace-keep");

        // when
        interceptor.afterCompletion(request, response, handlerMethod, null);

        // then
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.ORDER_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.PAYMENT_KEY)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.SELLER_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.PRODUCT_ID)).isNull();
        assertThat(MDC.get(BusinessIdMdcKeys.EVENT_ID)).isNull();
        assertThat(MDC.get("traceId")).isEqualTo("trace-keep");
    }

    @Test
    @DisplayName("인증과 path variable 을 동시에 enrich 한다")
    void shouldEnrichBothAuthenticationAndPathVariables() {
        // given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-555", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        Map<String, String> vars = new HashMap<>();
        vars.put(BusinessIdMdcKeys.PRODUCT_ID, "prod-9");
        vars.put(BusinessIdMdcKeys.EVENT_ID, "evt-9");
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, vars);

        // when
        boolean result = interceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isTrue();
        assertThat(MDC.get(BusinessIdMdcKeys.USER_ID)).isEqualTo("user-555");
        assertThat(MDC.get(BusinessIdMdcKeys.PRODUCT_ID)).isEqualTo("prod-9");
        assertThat(MDC.get(BusinessIdMdcKeys.EVENT_ID)).isEqualTo("evt-9");
    }

    static class SampleHandler {
        public void handle() {
        }
    }
}
