package com.personal.marketnote.product.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {
    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = objectMapper.copy();

        // NON_FINAL: 도메인 클래스의 의도된 비대칭 직렬화 보존 (Money 등 final VO 필드 + @JsonCreator(@JsonProperty Long) 패턴).
        // EVERYTHING 으로 변경 시 final VO 에 type id 가 강제 부여되어 Long 매핑 실패(InvalidTypeIdException) 발생 (#4851).
        // 캐시 value 타입에 record 사용 금지 — record(final) element 에 type info 미부여로 두 번째 조회 시 LinkedHashMap 으로 복원 (#4844).
        // 캐시 value 는 final class + @JsonCreator/@JsonProperty 생성자로 불변성 유지.
        // 화이트리스트(#4856): allowIfBaseType(Object.class) → 패키지 prefix 화이트리스트로 좁힘.
        // 폴리모픽 역직렬화로 인한 임의 클래스 인스턴스화(RCE 류) 위험을 차단한다.
        // 도메인(com.personal.marketnote.) + 표준 라이브러리(java.util/time/math/lang)만 허용.
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.personal.marketnote.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType("java.math.")
                        .allowIfSubType("java.lang.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new Hibernate5JakartaModule());
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper())))
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> perCacheTtl = new HashMap<>();
        // 상품 목록 첫 페이지 전용 캐시: 2분 TTL
        perCacheTtl.put("pricePolicy:list:first", redisCacheConfiguration.entryTtl(Duration.ofMinutes(2)));
        perCacheTtl.put("product:list:first", redisCacheConfiguration.entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(perCacheTtl)
                .build();
    }

    @Bean
    public RedisConnectionFactory redisCacheManagerFactory() {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(host);
        standalone.setPort(port);
        standalone.setPassword(password);

        return new LettuceConnectionFactory(standalone);
    }
}


