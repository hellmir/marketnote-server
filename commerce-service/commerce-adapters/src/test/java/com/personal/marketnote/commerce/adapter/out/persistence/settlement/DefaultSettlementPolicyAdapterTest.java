package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.configuration.SettlementSchedulerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultSettlementPolicyAdapter 단위 테스트")
class DefaultSettlementPolicyAdapterTest {

    @InjectMocks
    private DefaultSettlementPolicyAdapter adapter;

    @Mock
    private SettlementSchedulerProperties properties;

    @BeforeEach
    void setUp() {
        // no-op
    }

    @Test
    @DisplayName("기본 PG 수수료율을 properties에서 조회한다")
    void shouldReturnDefaultPgFeeRate() {
        // given
        when(properties.getDefaultPgFeeRate()).thenReturn(300);

        // when
        Integer result = adapter.getDefaultPgFeeRate();

        // then
        assertThat(result).isEqualTo(300);
        verify(properties).getDefaultPgFeeRate();
        verifyNoMoreInteractions(properties);
    }

    @Test
    @DisplayName("기본 플랫폼 수수료율을 properties에서 조회한다")
    void shouldReturnDefaultPlatformFeeRate() {
        // given
        when(properties.getDefaultPlatformFeeRate()).thenReturn(500);

        // when
        Integer result = adapter.getDefaultPlatformFeeRate();

        // then
        assertThat(result).isEqualTo(500);
        verify(properties).getDefaultPlatformFeeRate();
        verifyNoMoreInteractions(properties);
    }
}
