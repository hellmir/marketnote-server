package com.personal.marketnote.product.adapter.out.web.fulfillment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsElementsResult;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsResult;
import com.personal.marketnote.product.port.out.fulfillment.RegisterFulfillmentVendorGoodsCommand;
import com.personal.marketnote.product.port.out.fulfillment.UpdateFulfillmentVendorGoodsCommand;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceClientTest {

    private static final String BASE_URL = "http://localhost:8084";
    private static final String CUSTOMER_CODE = "TEST_CUSTOMER";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer mockServer;
    private FulfillmentServiceClient fulfillmentServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @Mock
    private ServiceCommunicationRecorder serviceCommunicationRecorder;

    @Mock
    private ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        fulfillmentServiceClient = new FulfillmentServiceClient(
                builder,
                BASE_URL,
                CUSTOMER_CODE,
                hmacServiceAuthHeaderBuilder,
                serviceCommunicationRecorder,
                serviceCommunicationPayloadGenerator
        );

        lenient().when(serviceCommunicationPayloadGenerator.buildRequestPayloadJson(any(), any(), any(), anyInt()))
                .thenReturn(objectMapper.createObjectNode());
        lenient().when(serviceCommunicationPayloadGenerator.buildErrorPayloadJson(anyString(), any(), anyInt()))
                .thenReturn(objectMapper.createObjectNode());
    }

    private void expectAuthTokenRequest() {
        mockServer.expect(requestTo(BASE_URL + "/api/v1/vendors/fassto/auth"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                            "content": {
                                "tokenInfo": {
                                    "accessToken": "test-access-token",
                                    "expiresAt": "2026-12-31T23:59:59"
                                }
                            }
                        }
                        """, MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("registerFulfillmentVendorGoods")
    class RegisterFulfillmentVendorGoods {

        @Test
        @DisplayName("풀필먼트 벤더 상품 등록 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            expectAuthTokenRequest();

            mockServer.expect(requestTo(BASE_URL + "/api/v1/vendors/fassto/goods/" + CUSTOMER_CODE))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "dataCount": 1,
                                    "goods": [{"code": "200", "msg": "SUCCESS", "cstGodCd": "CST001"}]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            RegisterFulfillmentVendorGoodsCommand command = RegisterFulfillmentVendorGoodsCommand.builder()
                    .customerGoodsCode("CST001")
                    .goodsName("테스트 상품")
                    .goodsType("01")
                    .giftDivision("N")
                    .build();

            fulfillmentServiceClient.registerFulfillmentVendorGoods(command);

            mockServer.verify();
        }

        @Test
        @DisplayName("인증 토큰 요청이 모든 재시도 실패하면 FulfillmentServiceRequestFailedException이 발생한다")
        void shouldThrowExceptionWhenAuthTokenRequestFails() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/vendors/fassto/auth"))
                        .andExpect(method(HttpMethod.POST))
                        .andRespond(withServerError());
            }

            RegisterFulfillmentVendorGoodsCommand command = RegisterFulfillmentVendorGoodsCommand.builder()
                    .customerGoodsCode("CST001")
                    .goodsName("테스트 상품")
                    .goodsType("01")
                    .giftDivision("N")
                    .build();

            assertThatThrownBy(() -> fulfillmentServiceClient.registerFulfillmentVendorGoods(command))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getFulfillmentVendorGoods")
    class GetFulfillmentVendorGoods {

        @Test
        @DisplayName("풀필먼트 벤더 상품 목록 조회에 성공하면 결과를 반환한다")
        void shouldReturnGoodsResultWhenRequestSucceeds() {
            expectAuthTokenRequest();

            mockServer.expect(requestTo(BASE_URL + "/api/v1/vendors/fassto/goods/detail/" + CUSTOMER_CODE + "?godNm=test"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "dataCount": 1,
                                    "goods": [{
                                        "godCd": "GOD001", "godType": "01", "godNm": "test",
                                        "godTypeNm": "일반", "invGodNmUseYn": "N", "cstGodCd": "CST001",
                                        "godOptCd1": "", "godOptCd2": "", "cstCd": "C01", "cstNm": "고객",
                                        "supCd": "", "supNm": "", "cateCd": "", "cateNm": "",
                                        "seasonCd": "", "genderCd": "", "godPr": "10000", "inPr": "8000",
                                        "salPr": "12000", "dealTemp": "상온", "pickFac": "",
                                        "giftDiv": "N", "giftDivNm": "비선물",
                                        "godWidth": "", "godLength": "", "godHeight": "",
                                        "makeYr": "", "godBulk": "", "godWeight": "", "godSideSum": "",
                                        "godVolume": "", "godBarcd": "", "boxWidth": "", "boxLength": "",
                                        "boxHeight": "", "boxBulk": "", "boxWeight": "",
                                        "inBoxBarcd": "", "inBoxLength": "", "inBoxHeight": "",
                                        "inBoxBulk": "", "inBoxWidth": "", "inBoxWeight": "",
                                        "inBoxSideSum": "", "boxInCnt": "", "inBoxInCnt": "",
                                        "pltInCnt": "", "origin": "", "distTermMgtYn": "",
                                        "useTermDay": "", "outCanDay": "", "inCanDay": "",
                                        "boxDiv": "", "bufGodYn": "", "loadingDirection": "",
                                        "firstInDt": "", "useYn": "Y", "feeYn": "",
                                        "saleUnitQty": "", "cstOneDayDeliveryYn": "", "safetyStock": ""
                                    }]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            GetFulfillmentVendorGoodsResult result =
                    fulfillmentServiceClient.getFulfillmentVendorGoods("test");

            assertThat(result).isNotNull();
            assertThat(result.dataCount()).isEqualTo(1);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getFulfillmentVendorGoodsElements")
    class GetFulfillmentVendorGoodsElements {

        @Test
        @DisplayName("풀필먼트 벤더 상품 구성요소 조회에 성공하면 결과를 반환한다")
        void shouldReturnGoodsElementsResultWhenRequestSucceeds() {
            expectAuthTokenRequest();

            mockServer.expect(requestTo(BASE_URL + "/api/v1/vendors/fassto/goods/element/" + CUSTOMER_CODE))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "dataCount": 1,
                                    "elements": [{
                                        "godCd": "GOD001",
                                        "cstGodCd": "CST001",
                                        "godNm": "test",
                                        "useYn": "Y",
                                        "elementList": []
                                    }]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            GetFulfillmentVendorGoodsElementsResult result =
                    fulfillmentServiceClient.getFulfillmentVendorGoodsElements();

            assertThat(result).isNotNull();
            assertThat(result.dataCount()).isEqualTo(1);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("updateFulfillmentVendorGoods")
    class UpdateFulfillmentVendorGoods {

        @Test
        @DisplayName("풀필먼트 벤더 상품 수정 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenUpdateSucceeds() {
            expectAuthTokenRequest();

            mockServer.expect(requestTo(BASE_URL + "/api/v1/vendors/fassto/goods/" + CUSTOMER_CODE))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "dataCount": 1,
                                    "goods": [{"code": "200", "msg": "SUCCESS"}]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            UpdateFulfillmentVendorGoodsCommand command = UpdateFulfillmentVendorGoodsCommand.builder()
                    .customerGoodsCode("CST001")
                    .goodsName("수정된 상품")
                    .goodsType("01")
                    .giftDivision("N")
                    .build();

            fulfillmentServiceClient.updateFulfillmentVendorGoods(command);

            mockServer.verify();
        }
    }
}
