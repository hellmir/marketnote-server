package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("FulfillmentCustomerCodeAdapter 단위 테스트")
class FulfillmentCustomerCodeAdapterTest {

    @InjectMocks
    private FulfillmentCustomerCodeAdapter adapter;

    @Mock
    private FulfillmentAuthProperties fulfillmentAuthProperties;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("getCustomerCode")
    class GetCustomerCode {

        @Test
        @DisplayName("설정된 고객 코드를 그대로 반환한다")
        void returnsConfiguredCustomerCode() {
            // given
            when(fulfillmentAuthProperties.getCustomerCode()).thenReturn("CUST001");

            // when
            String result = adapter.getCustomerCode();

            // then
            assertThat(result).isEqualTo("CUST001");
            verify(fulfillmentAuthProperties).getCustomerCode();
            verifyNoMoreInteractions(fulfillmentAuthProperties);
        }

        @Test
        @DisplayName("설정 값이 null이면 null을 반환한다")
        void returnsNullWhenConfigurationNotSet() {
            // given
            when(fulfillmentAuthProperties.getCustomerCode()).thenReturn(null);

            // when
            String result = adapter.getCustomerCode();

            // then
            assertThat(result).isNull();
            verify(fulfillmentAuthProperties).getCustomerCode();
            verifyNoMoreInteractions(fulfillmentAuthProperties);
        }

        @Test
        @DisplayName("호출할 때마다 최신 설정 값을 조회한다")
        void readsPropertyEveryInvocation() {
            // given
            when(fulfillmentAuthProperties.getCustomerCode())
                    .thenReturn("CUST001")
                    .thenReturn("CUST002");

            // when
            String firstCall = adapter.getCustomerCode();
            String secondCall = adapter.getCustomerCode();

            // then
            assertThat(firstCall).isEqualTo("CUST001");
            assertThat(secondCall).isEqualTo("CUST002");
            verify(fulfillmentAuthProperties, org.mockito.Mockito.times(2)).getCustomerCode();
            verifyNoMoreInteractions(fulfillmentAuthProperties);
        }
    }
}
