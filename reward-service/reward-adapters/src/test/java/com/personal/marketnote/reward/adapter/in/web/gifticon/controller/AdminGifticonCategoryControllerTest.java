package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonCategoryExposureRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonCategoryOrderRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.UpdateGifticonCategoryRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonCategoriesResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.UpdateGifticonCategoryCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonCategoriesUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonCategoryExposureUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonCategoryOrderUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.UpdateGifticonCategoryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminGifticonCategoryController 테스트")
class AdminGifticonCategoryControllerTest {

    @Mock
    private GetAdminGifticonCategoriesUseCase getAdminGifticonCategoriesUseCase;
    @Mock
    private UpdateGifticonCategoryUseCase updateGifticonCategoryUseCase;
    @Mock
    private ManageGifticonCategoryExposureUseCase manageGifticonCategoryExposureUseCase;
    @Mock
    private ManageGifticonCategoryOrderUseCase manageGifticonCategoryOrderUseCase;

    @InjectMocks
    private AdminGifticonCategoryController controller;

    @Nested
    @DisplayName("GET /api/v1/admin/gifticon/categories")
    class GetAdminGifticonCategories {

        @Test
        @DisplayName("OK 상태로 카테고리 목록을 반환한다")
        void returnsOkWithCategories() {
            given(getAdminGifticonCategoriesUseCase.getAdminGifticonCategories())
                    .willReturn(new GetAdminGifticonCategoriesResult(List.of()));

            ResponseEntity<BaseResponse<GetAdminGifticonCategoriesResponse>> response =
                    controller.getAdminGifticonCategories();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getAdminGifticonCategoriesUseCase).getAdminGifticonCategories();
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/gifticon/categories/{categoryId}")
    class UpdateGifticonCategory {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            UpdateGifticonCategoryRequest request = new UpdateGifticonCategoryRequest();
            request.setDisplayName("새 표시명");
            request.setIconUrl("icon.png");

            ResponseEntity<BaseResponse<Void>> response = controller.updateGifticonCategory(99L, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdateGifticonCategoryCommand> captor =
                    ArgumentCaptor.forClass(UpdateGifticonCategoryCommand.class);
            verify(updateGifticonCategoryUseCase).updateGifticonCategory(captor.capture());
            assertThat(captor.getValue().categoryId()).isEqualTo(99L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/gifticon/categories/exposure")
    class ManageGifticonCategoryExposure {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            ManageGifticonCategoryExposureRequest request = new ManageGifticonCategoryExposureRequest();
            ManageGifticonCategoryExposureRequest.ExposureItem item =
                    new ManageGifticonCategoryExposureRequest.ExposureItem();
            ReflectionTestUtils.setField(item, "categoryId", 1L);
            ReflectionTestUtils.setField(item, "exposed", true);
            ReflectionTestUtils.setField(request, "items", List.of(item));

            ResponseEntity<BaseResponse<Void>> response = controller.manageGifticonCategoryExposure(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(manageGifticonCategoryExposureUseCase).manageExposure(any(ManageGifticonCategoryExposureCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/gifticon/categories/order")
    class ManageGifticonCategoryOrder {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            ManageGifticonCategoryOrderRequest request = new ManageGifticonCategoryOrderRequest();
            ManageGifticonCategoryOrderRequest.OrderItem item = new ManageGifticonCategoryOrderRequest.OrderItem();
            ReflectionTestUtils.setField(item, "categoryId", 1L);
            ReflectionTestUtils.setField(item, "orderNum", 5);
            ReflectionTestUtils.setField(request, "items", List.of(item));

            ResponseEntity<BaseResponse<Void>> response = controller.manageGifticonCategoryOrder(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(manageGifticonCategoryOrderUseCase).manageOrder(any(ManageGifticonCategoryOrderCommand.class));
        }
    }
}
