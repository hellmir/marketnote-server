package com.personal.marketnote.common.configuration.cache;

import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.List;

/**
 * Redis 캐시 ObjectMapper 폴리모픽 화이트리스트 정책(#4856)을 개입화한 공용 팩토리.
 * <p>
 * 보안 정책:
 * <ul>
 *     <li>{@code com.personal.marketnote.} — 도메인 객체</li>
 *     <li>{@code java.util.} — 표준 컬렉션/유틸</li>
 *     <li>{@code java.time.} — 날짜/시간 타입</li>
 *     <li>{@code java.math.} — BigDecimal/BigInteger</li>
 *     <li>{@code java.lang.} — String/Number 등 기본 타입</li>
 * </ul>
 * 위 5개 prefix 외 클래스는 폴리모픽 역직렬화에서 거부되어
 * 임의 클래스 인스턴스화(RCE 류) 위험을 차단한다.
 * <p>
 * 회귀 이력:
 * <ul>
 *     <li>#4844 — DefaultTyping.EVERYTHING 정책으로 prod 회귀(Money/Rate 등 final VO 비대칭) 발생</li>
 *     <li>#4851 — NON_FINAL 유지 + record 캐시 value를 final class로 전환하여 정정</li>
 * </ul>
 * 본 헬퍼는 NON_FINAL 정책과 함께 사용해야 하며, prefix 외부화/추가는 보안 정책 개입화 원칙에 따라 금지한다.
 *
 * @see com.fasterxml.jackson.databind.ObjectMapper#activateDefaultTyping
 */
public final class CachePolymorphicTypeValidatorFactory {

    private static final List<String> ALLOWED_PREFIXES = List.of(
        "com.personal.marketnote.",
        "java.util.",
        "java.time.",
        "java.math.",
        "java.lang."
    );

    private CachePolymorphicTypeValidatorFactory() {
    }

    /**
     * 5개 prefix 화이트리스트가 적용된 {@link BasicPolymorphicTypeValidator}를 새로 생성하여 반환한다.
     * 매 호출마다 동등한 정책을 가진 새 인스턴스를 반환한다.
     */
    public static BasicPolymorphicTypeValidator createDefault() {
        BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();
        ALLOWED_PREFIXES.forEach(builder::allowIfSubType);
        return builder.build();
    }
}
