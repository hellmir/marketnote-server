package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonCategoriesResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonCategoriesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonCategoryController 테스트")
class GifticonCategoryControllerTest {

    @Mock
    private GetGifticonCategoriesUseCase getGifticonCategoriesUseCase;

    @InjectMocks
    private GifticonCategoryController controller;

    @Test
    @DisplayName("OK 상태로 카테고리 목록을 반환한다")
    void returnsOkWithCategories() {
        given(getGifticonCategoriesUseCase.getCategories())
                .willReturn(new GetGifticonCategoriesResult(List.of()));

        ResponseEntity<BaseResponse<GetGifticonCategoriesResponse>> response = controller.getCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isNotNull();
        verify(getGifticonCategoriesUseCase).getCategories();
    }
}
