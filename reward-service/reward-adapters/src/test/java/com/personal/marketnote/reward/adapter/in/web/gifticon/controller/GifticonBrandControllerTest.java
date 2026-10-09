package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonBrandsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonBrandsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonBrandsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonBrandController 테스트")
class GifticonBrandControllerTest {

    @Mock
    private GetGifticonBrandsUseCase getGifticonBrandsUseCase;

    @InjectMocks
    private GifticonBrandController controller;

    @Test
    @DisplayName("카테고리 코드를 UseCase Command에 전달하고 OK 상태로 브랜드 목록을 반환한다")
    void returnsOkWithBrands() {
        given(getGifticonBrandsUseCase.getBrands(any(GetGifticonBrandsCommand.class)))
                .willReturn(new GetGifticonBrandsResult(List.of()));

        ResponseEntity<BaseResponse<GetGifticonBrandsResponse>> response = controller.getBrands("CAT-01");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isNotNull();
        ArgumentCaptor<GetGifticonBrandsCommand> captor = ArgumentCaptor.forClass(GetGifticonBrandsCommand.class);
        verify(getGifticonBrandsUseCase).getBrands(captor.capture());
        assertThat(captor.getValue().categoryCode()).isEqualTo("CAT-01");
    }
}
