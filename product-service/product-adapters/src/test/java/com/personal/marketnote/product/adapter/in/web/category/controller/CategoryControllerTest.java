package com.personal.marketnote.product.adapter.in.web.category.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterCategoryRequest;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterProductCategoriesRequest;
import com.personal.marketnote.product.adapter.in.web.category.response.GetCategoriesResponse;
import com.personal.marketnote.product.adapter.in.web.category.response.RegisterCategoryResponse;
import com.personal.marketnote.product.adapter.in.web.category.response.RegisterProductCategoriesResponse;
import com.personal.marketnote.product.port.in.command.DeleteCategoryCommand;
import com.personal.marketnote.product.port.in.command.RegisterCategoryCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCategoriesCommand;
import com.personal.marketnote.product.port.in.result.category.GetCategoriesResult;
import com.personal.marketnote.product.port.in.result.category.RegisterCategoryResult;
import com.personal.marketnote.product.port.in.result.category.RegisterProductCategoriesResult;
import com.personal.marketnote.product.port.in.usecase.category.DeleteCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.GetCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.RegisterCategoryUseCase;
import com.personal.marketnote.product.port.in.usecase.category.RegisterProductCategoriesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController 테스트")
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private RegisterCategoryUseCase registerCategoryUseCase;
    @Mock
    private RegisterProductCategoriesUseCase registerProductCategoriesUseCase;
    @Mock
    private GetCategoryUseCase getCategoryUseCase;
    @Mock
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @Nested
    @DisplayName("POST /api/v1/categories")
    class RegisterCategory {

        @Test
        @DisplayName("카테고리 등록 시 201 CREATED를 반환한다")
        void returnsCreatedOnRegister() {
            RegisterCategoryRequest request = new RegisterCategoryRequest();
            ReflectionTestUtils.setField(request, "parentCategoryId", null);
            ReflectionTestUtils.setField(request, "name", "루테인");
            RegisterCategoryResult result = new RegisterCategoryResult(1L, null, "루테인");
            when(registerCategoryUseCase.registerCategory(any(RegisterCategoryCommand.class)))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<RegisterCategoryResponse>> response =
                    categoryController.registerCategory(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(registerCategoryUseCase).registerCategory(any(RegisterCategoryCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/categories")
    class GetCategories {

        @Test
        @DisplayName("parentId 없이 조회하면 최상위 카테고리 목록을 반환한다")
        void returnsRootCategories() {
            GetCategoriesResult result = new GetCategoriesResult(List.of());
            when(getCategoryUseCase.getCategoriesByParentId(null)).thenReturn(result);

            ResponseEntity<BaseResponse<GetCategoriesResponse>> response =
                    categoryController.getCategories(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("parentId로 조회하면 해당 부모의 하위 카테고리 목록을 반환한다")
        void returnsCategoriesByParent() {
            GetCategoriesResult result = new GetCategoriesResult(List.of());
            when(getCategoryUseCase.getCategoriesByParentId(10L)).thenReturn(result);

            ResponseEntity<BaseResponse<GetCategoriesResponse>> response =
                    categoryController.getCategories(10L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getCategoryUseCase).getCategoriesByParentId(10L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/categories/{categoryId}")
    class DeleteCategory {

        @Test
        @DisplayName("카테고리 삭제 시 200 OK를 반환한다")
        void deletesCategory() {
            ResponseEntity<BaseResponse<Void>> response = categoryController.deleteCategory(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteCategoryUseCase).deleteCategory(any(DeleteCategoryCommand.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/categories/{productId}/categories")
    class RegisterProductCategories {

        @Test
        @DisplayName("상품 카테고리 등록 시 200 OK를 반환한다")
        void registersProductCategories() {
            RegisterProductCategoriesRequest request = new RegisterProductCategoriesRequest();
            ReflectionTestUtils.setField(request, "categoryIds", List.of(10L, 20L));
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");
            RegisterProductCategoriesResult result = RegisterProductCategoriesResult.of(1L, List.of(10L, 20L));
            when(registerProductCategoriesUseCase.registerProductCategories(
                    eq(100L), anyBoolean(), any(RegisterProductCategoriesCommand.class)
            )).thenReturn(result);

            ResponseEntity<BaseResponse<RegisterProductCategoriesResponse>> response =
                    categoryController.registerProductCategories(1L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
        }
    }
}
