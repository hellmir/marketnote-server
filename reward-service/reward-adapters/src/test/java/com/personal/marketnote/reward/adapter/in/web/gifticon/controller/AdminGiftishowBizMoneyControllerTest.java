package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGiftishowBizMoneyBalanceResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonVendorBalanceResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonVendorBalanceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminGiftishowBizMoneyController 테스트")
class AdminGiftishowBizMoneyControllerTest {

    @Mock
    private GetGifticonVendorBalanceUseCase getGifticonVendorBalanceUseCase;

    @InjectMocks
    private AdminGiftishowBizMoneyController controller;

    @Test
    @DisplayName("OK 상태로 비즈머니 잔액을 반환한다")
    void returnsOkWithBalance() {
        given(getGifticonVendorBalanceUseCase.getBalance())
                .willReturn(GetGifticonVendorBalanceResult.of(1_234_567L));

        ResponseEntity<BaseResponse<GetGiftishowBizMoneyBalanceResponse>> response = controller.getBalance();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isNotNull();
        verify(getGifticonVendorBalanceUseCase).getBalance();
    }
}
