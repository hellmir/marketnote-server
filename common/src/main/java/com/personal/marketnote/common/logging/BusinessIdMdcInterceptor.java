package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * 컨트롤러 진입 직전에 비즈니스 ID(userId, orderId, paymentKey, sellerId, productId, eventId)를
 * MDC 에 자동 enrich 하고, 요청 종료 시 정리하는 인터셉터.
 *
 * <p>userId 는 {@link SecurityContextHolder} 의 인증 principal 이름에서 추출하고,
 * 나머지는 Spring 의 URI template variables 에서 화이트리스트 키만 추출한다.
 * 정적 리소스 등 {@link HandlerMethod} 가 아닌 핸들러는 enrich 대상에서 제외된다.
 *
 * <p>모든 값은 {@link BusinessIdMdcKeys#sanitize(String)} 로 CRLF/제어 문자가 제거되어
 * 로그 인젝션을 방어한다.
 */
@Component
public class BusinessIdMdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        enrichUserIdFromAuthentication();
        enrichBusinessIdsFromPathVariables(request);
        return true;
    }

    /**
     * afterCompletion 은 화이트리스트 6 개 키를 모두 MDC 에서 제거한다.
     * 정책: 요청 종료 시 비즈니스 ID 누수 방지 (ThreadLocal 풀 재사용 시 잔재 차단).
     * preHandle 이후 컨트롤러 내부에서 추가 enrich 한 비즈니스 ID 도 함께 정리되므로,
     * 컨트롤러 내부에서 수동 MDC.put 은 인터셉터 정리 정책의 영향을 받음을 인지한다.
     */
    @Override
    public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex
    ) {
        for (String key : BusinessIdMdcKeys.WHITELIST) {
            MDC.remove(key);
        }
    }

    private void enrichUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (FormatValidator.isNull(authentication)) {
            return;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return;
        }
        if (!authentication.isAuthenticated()) {
            return;
        }
        String name = authentication.getName();
        if (FormatValidator.hasNoValue(name)) {
            return;
        }
        MDC.put(BusinessIdMdcKeys.USER_ID, BusinessIdMdcKeys.sanitize(name));
    }

    private void enrichBusinessIdsFromPathVariables(HttpServletRequest request) {
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof Map<?, ?> rawVars)) {
            return;
        }
        for (Map.Entry<?, ?> entry : rawVars.entrySet()) {
            Object rawKey = entry.getKey();
            Object rawValue = entry.getValue();
            if (!(rawKey instanceof String key)) {
                continue;
            }
            if (!BusinessIdMdcKeys.WHITELIST.contains(key)) {
                continue;
            }
            if (!(rawValue instanceof String stringValue)) {
                continue;
            }
            String value = stringValue.trim();
            if (value.isEmpty()) {
                continue;
            }
            MDC.put(key, BusinessIdMdcKeys.sanitize(value));
        }
    }
}
