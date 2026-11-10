package com.personal.marketnote.notification.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateDetailResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CacheConfig ObjectMapper 직렬화/역직렬화 왕복 테스트.
 * <p>
 * GenericJackson2JsonRedisSerializer는 역직렬화 시 Object.class로 읽으므로,
 * mapper.readValue(json, Object.class)로 테스트해야 실제 Redis 캐시 동작을 재현한다.
 * <p>
 * notification-service `aligo:template:active` 캐시는 GetAligoTemplateDetailResult(record)를
 * 캐시 value 로 저장하며, 내부에 List&lt;ButtonResult&gt;(record) 중첩 필드를 가진다.
 * record 캐시 value 금지 컨벤션(#4851) 정합 검증.
 */
class CacheConfigObjectMapperTest {

    private ObjectMapper createCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
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

    private GetAligoTemplateDetailResult.ButtonResult createTestButtonResult() {
        return new GetAligoTemplateDetailResult.ButtonResult(
                1, "버튼1", "WL",
                "https://m.example.com", "https://example.com",
                "iosapp://example", "andapp://example"
        );
    }

    private GetAligoTemplateDetailResult createTestDetailResult(List<GetAligoTemplateDetailResult.ButtonResult> buttons) {
        return new GetAligoTemplateDetailResult(
                1L, "senderKey-1", "TPL_001", "테스트 템플릿",
                "템플릿 본문", "제목", "부제목", "BA",
                "EM", false, false,
                "https://example.com/img.jpg", "img.jpg",
                "APR", "ACTIVE", "코멘트",
                buttons
        );
    }

    @Test
    @DisplayName("GetAligoTemplateDetailResult 단일 객체 (buttons=ArrayList) 라운드트립이 성공한다")
    void singleDetailResult_withArrayListButtons_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<GetAligoTemplateDetailResult.ButtonResult> buttons = new ArrayList<>(List.of(createTestButtonResult()));
        GetAligoTemplateDetailResult original = createTestDetailResult(buttons);

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(GetAligoTemplateDetailResult.class);
        GetAligoTemplateDetailResult result = (GetAligoTemplateDetailResult) deserialized;
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.templateCode()).isEqualTo("TPL_001");
        assertThat(result.buttons()).hasSize(1);
        assertThat(result.buttons().get(0)).isInstanceOf(GetAligoTemplateDetailResult.ButtonResult.class);
        assertThat(result.buttons().get(0).name()).isEqualTo("버튼1");
    }

    @Test
    @DisplayName("GetAligoTemplateDetailResult 중첩 ButtonResult ArrayList 라운드트립이 성공한다")
    void nestedButtonResultArrayList_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        List<GetAligoTemplateDetailResult.ButtonResult> buttons = Stream.of(createTestButtonResult())
                .collect(Collectors.toCollection(ArrayList::new));
        GetAligoTemplateDetailResult original = createTestDetailResult(buttons);

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(GetAligoTemplateDetailResult.class);
        GetAligoTemplateDetailResult result = (GetAligoTemplateDetailResult) deserialized;
        assertThat(result.buttons()).hasSize(1);
        assertThat(result.buttons().get(0)).isInstanceOf(GetAligoTemplateDetailResult.ButtonResult.class);
        assertThat(result.buttons().get(0).ordering()).isEqualTo(1);
        assertThat(result.buttons().get(0).linkType()).isEqualTo("WL");
    }

    @Test
    @DisplayName("GetAligoTemplateDetailResult 중첩 ButtonResult ImmutableList(stream().toList()) 라운드트립이 성공한다")
    void nestedButtonResultImmutableList_shouldRoundTrip() throws JsonProcessingException {
        // given - 실배포 코드와 동일 패턴: GetAligoTemplateByCodeService L39-41 stream().toList()
        ObjectMapper mapper = createCacheObjectMapper();
        List<GetAligoTemplateDetailResult.ButtonResult> buttons = Stream.of(createTestButtonResult()).toList();
        GetAligoTemplateDetailResult original = createTestDetailResult(buttons);

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(GetAligoTemplateDetailResult.class);
        GetAligoTemplateDetailResult result = (GetAligoTemplateDetailResult) deserialized;
        assertThat(result.buttons()).hasSize(1);
        assertThat(result.buttons().get(0)).isInstanceOf(GetAligoTemplateDetailResult.ButtonResult.class);
        assertThat(result.buttons().get(0).name()).isEqualTo("버튼1");
    }

    @Test
    @DisplayName("GetAligoTemplateDetailResult 빈 ButtonResult 리스트 라운드트립이 성공한다")
    void emptyButtonList_shouldRoundTrip() throws JsonProcessingException {
        // given
        ObjectMapper mapper = createCacheObjectMapper();
        GetAligoTemplateDetailResult original = createTestDetailResult(new ArrayList<>());

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(GetAligoTemplateDetailResult.class);
        GetAligoTemplateDetailResult result = (GetAligoTemplateDetailResult) deserialized;
        assertThat(result.buttons()).isEmpty();
    }
}
