package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonVendorBalanceResult;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonVendorBalancePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGifticonVendorBalanceUseCaseTest {

    @InjectMocks
    private GetGifticonVendorBalanceService getGifticonVendorBalanceService;

    @Mock
    private FetchGifticonVendorBalancePort fetchGifticonVendorBalancePort;

    @Test
    @DisplayName("기프티콘 벤더 잔액을 정상 조회한다")
    void shouldReturnBalance() {
        // given
        when(fetchGifticonVendorBalancePort.fetchBalance()).thenReturn(250000L);

        // when
        GetGifticonVendorBalanceResult result = getGifticonVendorBalanceService.getBalance();

        // then
        assertThat(result.balance()).isEqualTo(250000L);
        verify(fetchGifticonVendorBalancePort).fetchBalance();
    }

    @Test
    @DisplayName("잔액이 0인 경우에도 정상 반환한다")
    void shouldReturnZeroBalance() {
        // given
        when(fetchGifticonVendorBalancePort.fetchBalance()).thenReturn(0L);

        // when
        GetGifticonVendorBalanceResult result = getGifticonVendorBalanceService.getBalance();

        // then
        assertThat(result.balance()).isEqualTo(0L);
        verify(fetchGifticonVendorBalancePort).fetchBalance();
    }
}
