package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoAvailableCountResult;
import com.personal.marketnote.notification.port.out.aligo.GetAligoHeartInfoExternalPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoHeartInfoExternalPort.AligoHeartInfoResult;
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
class GetAligoAvailableCountUseCaseTest {

    @InjectMocks
    private GetAligoAvailableCountService getAligoAvailableCountService;

    @Mock
    private GetAligoHeartInfoExternalPort getAligoHeartInfoExternalPort;

    @Test
    @DisplayName("알리고 발송가능건수를 정상적으로 반환한다")
    void shouldReturnAvailableCountSuccessfully() {
        // given
        when(getAligoHeartInfoExternalPort.getHeartInfo())
                .thenReturn(new AligoHeartInfoResult(100, 50, 25, 200));

        // when
        GetAligoAvailableCountResult result = getAligoAvailableCountService.getAvailableCount();

        // then
        assertThat(result.smsCnt()).isEqualTo(100);
        assertThat(result.lmsCnt()).isEqualTo(50);
        assertThat(result.mmsCnt()).isEqualTo(25);
        assertThat(result.altCnt()).isEqualTo(200);

        verify(getAligoHeartInfoExternalPort).getHeartInfo();
    }

    @Test
    @DisplayName("잔여 포인트가 0이면 모든 건수가 0으로 반환된다")
    void shouldReturnZeroCountsWhenNoRemainingPoints() {
        // given
        when(getAligoHeartInfoExternalPort.getHeartInfo())
                .thenReturn(new AligoHeartInfoResult(0, 0, 0, 0));

        // when
        GetAligoAvailableCountResult result = getAligoAvailableCountService.getAvailableCount();

        // then
        assertThat(result.smsCnt()).isZero();
        assertThat(result.lmsCnt()).isZero();
        assertThat(result.mmsCnt()).isZero();
        assertThat(result.altCnt()).isZero();
    }
}
