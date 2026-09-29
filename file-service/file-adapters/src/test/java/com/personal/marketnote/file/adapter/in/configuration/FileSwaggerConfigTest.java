package com.personal.marketnote.file.adapter.in.configuration;

import com.personal.marketnote.file.adapter.in.client.file.controller.FileController;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSwaggerConfigTest {
    private final FileSwaggerConfig fileSwaggerConfig = new FileSwaggerConfig();

    @Nested
    @DisplayName("tagOnlySorter")
    class TagOnlySorter {
        @Test
        @DisplayName("정의된 태그 순서대로 정렬한다")
        void sortsTagsInDefinedOrder() {
            // given
            OpenApiCustomizer sorter = fileSwaggerConfig.tagOnlySorter();
            OpenAPI openApi = new OpenAPI();
            List<Tag> tags = new ArrayList<>();
            tags.add(new Tag().name("서버 정보 API"));
            tags.add(new Tag().name("파일 API"));
            openApi.setTags(tags);

            // when
            sorter.customise(openApi);

            // then
            assertThat(openApi.getTags().get(0).getName()).isEqualTo("파일 API");
            assertThat(openApi.getTags().get(1).getName()).isEqualTo("서버 정보 API");
        }

        @Test
        @DisplayName("정의되지 않은 태그는 정의된 태그 뒤에 원래 순서대로 배치한다")
        void placesUnknownTagsAfterDefinedTags() {
            // given
            OpenApiCustomizer sorter = fileSwaggerConfig.tagOnlySorter();
            OpenAPI openApi = new OpenAPI();
            List<Tag> tags = new ArrayList<>();
            tags.add(new Tag().name("커스텀 API"));
            tags.add(new Tag().name("파일 API"));
            tags.add(new Tag().name("서버 정보 API"));
            openApi.setTags(tags);

            // when
            sorter.customise(openApi);

            // then
            assertThat(openApi.getTags().get(0).getName()).isEqualTo("파일 API");
            assertThat(openApi.getTags().get(1).getName()).isEqualTo("서버 정보 API");
            assertThat(openApi.getTags().get(2).getName()).isEqualTo("커스텀 API");
        }

        @Test
        @DisplayName("태그가 null이면 아무 작업도 하지 않는다")
        void doesNothingWhenTagsIsNull() {
            // given
            OpenApiCustomizer sorter = fileSwaggerConfig.tagOnlySorter();
            OpenAPI openApi = new OpenAPI();

            // when
            sorter.customise(openApi);

            // then
            assertThat(openApi.getTags()).isNull();
        }
    }

    @Nested
    @DisplayName("removeRequestBodyForAddFiles")
    class RemoveRequestBodyForAddFiles {
        @Test
        @DisplayName("addFiles 엔드포인트의 RequestBody를 제거한다")
        void removesRequestBodyForAddFilesEndpoint() throws NoSuchMethodException {
            // given
            ReflectionTestUtils.setField(fileSwaggerConfig, "serverOrigin", "https://files.test.com");
            OperationCustomizer customizer = fileSwaggerConfig.removeRequestBodyForAddFiles();
            Operation operation = new Operation();
            operation.setRequestBody(new io.swagger.v3.oas.models.parameters.RequestBody());

            HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBeanType()).thenReturn((Class) FileController.class);
            java.lang.reflect.Method method = mock(java.lang.reflect.Method.class);
            when(method.getName()).thenReturn("addFiles");
            when(handlerMethod.getMethod()).thenReturn(method);

            // when
            Operation result = customizer.customize(operation, handlerMethod);

            // then
            assertThat(result.getRequestBody()).isNull();
        }

        @Test
        @DisplayName("addFiles가 아닌 엔드포인트의 RequestBody는 유지한다")
        void keepsRequestBodyForNonAddFilesEndpoint() {
            // given
            ReflectionTestUtils.setField(fileSwaggerConfig, "serverOrigin", "https://files.test.com");
            OperationCustomizer customizer = fileSwaggerConfig.removeRequestBodyForAddFiles();
            Operation operation = new Operation();
            io.swagger.v3.oas.models.parameters.RequestBody requestBody = new io.swagger.v3.oas.models.parameters.RequestBody();
            operation.setRequestBody(requestBody);

            HandlerMethod handlerMethod = mock(HandlerMethod.class);
            when(handlerMethod.getBeanType()).thenReturn((Class) FileController.class);
            java.lang.reflect.Method method = mock(java.lang.reflect.Method.class);
            when(method.getName()).thenReturn("getFiles");
            when(handlerMethod.getMethod()).thenReturn(method);

            // when
            Operation result = customizer.customize(operation, handlerMethod);

            // then
            assertThat(result.getRequestBody()).isNotNull();
        }
    }
}
