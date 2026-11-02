package com.personal.marketnote.user.adapter.out.hibernate;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class HibernateCollectionIdResolverTest {

    private HibernateCollectionIdResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new HibernateCollectionIdResolver();
    }

    @Nested
    @DisplayName("idFromValue - 값에서 타입 ID 추출")
    class IdFromValueTest {

        @Test
        @DisplayName("ArrayList를 전달하면 정규화된 클래스 이름을 반환한다")
        void shouldReturnClassNameForArrayList() {
            String result = resolver.idFromValue(new ArrayList<>());
            assertThat(result).isEqualTo("java.util.ArrayList");
        }

        @Test
        @DisplayName("HashMap을 전달하면 정규화된 클래스 이름을 반환한다")
        void shouldReturnClassNameForHashMap() {
            String result = resolver.idFromValue(new HashMap<>());
            assertThat(result).isEqualTo("java.util.HashMap");
        }

        @Test
        @DisplayName("HashSet을 전달하면 정규화된 클래스 이름을 반환한다")
        void shouldReturnClassNameForHashSet() {
            String result = resolver.idFromValue(new HashSet<>());
            assertThat(result).isEqualTo("java.util.HashSet");
        }

        @Test
        @DisplayName("TreeMap을 전달하면 정규화된 클래스 이름을 반환한다")
        void shouldReturnClassNameForTreeMap() {
            String result = resolver.idFromValue(new TreeMap<>());
            assertThat(result).isEqualTo("java.util.TreeMap");
        }

        @Test
        @DisplayName("일반 String 객체를 전달하면 String 클래스 이름을 반환한다")
        void shouldReturnClassNameForString() {
            String result = resolver.idFromValue("hello");
            assertThat(result).isEqualTo("java.lang.String");
        }
    }

    @Nested
    @DisplayName("idFromValueAndType - 값과 타입에서 ID 추출")
    class IdFromValueAndTypeTest {

        @Test
        @DisplayName("suggestedType과 무관하게 실제 값의 타입 ID를 반환한다")
        void shouldReturnIdFromValueRegardlessOfSuggestedType() {
            String result = resolver.idFromValueAndType(new LinkedList<>(), List.class);
            assertThat(result).isEqualTo("java.util.LinkedList");
        }
    }

    @Nested
    @DisplayName("getMechanism - 타입 해석 메커니즘")
    class GetMechanismTest {

        @Test
        @DisplayName("CLASS 메커니즘을 반환한다")
        void shouldReturnClassMechanism() {
            assertThat(resolver.getMechanism()).isEqualTo(JsonTypeInfo.Id.CLASS);
        }
    }
}
