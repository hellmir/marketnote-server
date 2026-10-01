package com.personal.marketnote.community.adapter.in.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommunitySwaggerConfig")
class CommunitySwaggerConfigTest {

    private CommunitySwaggerConfig config;
    private OpenApiCustomizer sorter;

    @BeforeEach
    void setUp() {
        config = new CommunitySwaggerConfig();
        sorter = config.tagOnlySorter();
    }

    @Nested
    @DisplayName("tagOnlySorter")
    class TagOnlySorter {

        @Test
        @DisplayName("tags가 null이면 아무 동작도 하지 않는다")
        void doesNothingWhenTagsNull() {
            OpenAPI openApi = new OpenAPI();

            sorter.customise(openApi);

            assertThat(openApi.getTags()).isNull();
        }

        @Test
        @DisplayName("tags가 비어 있으면 빈 그대로 유지한다")
        void doesNothingWhenTagsEmpty() {
            OpenAPI openApi = new OpenAPI();
            openApi.setTags(new ArrayList<>());

            sorter.customise(openApi);

            assertThat(openApi.getTags()).isEmpty();
        }

        @Test
        @DisplayName("정의된 순서에 따라 태그를 재정렬한다")
        void sortsKnownTagsByPredefinedOrder() {
            OpenAPI openApi = new OpenAPI();
            openApi.setTags(new ArrayList<>(List.of(
                    new Tag().name("리뷰 API"),
                    new Tag().name("게시판 API"),
                    new Tag().name("좋아요 API"),
                    new Tag().name("게시글 API"),
                    new Tag().name("서버 정보 API"),
                    new Tag().name("신고 API")
            )));

            sorter.customise(openApi);

            List<String> sortedNames = openApi.getTags().stream().map(Tag::getName).toList();
            assertThat(sortedNames).containsExactly(
                    "게시판 API",
                    "게시글 API",
                    "리뷰 API",
                    "좋아요 API",
                    "신고 API",
                    "서버 정보 API"
            );
        }

        @Test
        @DisplayName("정의되지 않은 태그는 정의된 태그 뒤에 원래 순서로 배치한다")
        void placesUnknownTagsAfterKnownPreservingOriginalOrder() {
            OpenAPI openApi = new OpenAPI();
            openApi.setTags(new ArrayList<>(List.of(
                    new Tag().name("커스텀 B"),
                    new Tag().name("리뷰 API"),
                    new Tag().name("커스텀 A"),
                    new Tag().name("게시판 API")
            )));

            sorter.customise(openApi);

            List<String> sortedNames = openApi.getTags().stream().map(Tag::getName).toList();
            assertThat(sortedNames).containsExactly(
                    "게시판 API",
                    "리뷰 API",
                    "커스텀 B",
                    "커스텀 A"
            );
        }

        @Test
        @DisplayName("정의되지 않은 태그만 있으면 원본 순서를 유지한다")
        void preservesOriginalOrderWhenAllUnknown() {
            OpenAPI openApi = new OpenAPI();
            openApi.setTags(new ArrayList<>(List.of(
                    new Tag().name("Z"),
                    new Tag().name("A"),
                    new Tag().name("M")
            )));

            sorter.customise(openApi);

            List<String> sortedNames = openApi.getTags().stream().map(Tag::getName).toList();
            assertThat(sortedNames).containsExactly("Z", "A", "M");
        }
    }
}
