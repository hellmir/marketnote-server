package com.personal.marketnote.product.service.product;

import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsInfoResult;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsResult;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductDetailResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsElementPort;
import com.personal.marketnote.product.port.out.fulfillment.GetFulfillmentVendorGoodsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAdminProductDetailUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private GetFulfillmentVendorGoodsPort getFulfillmentVendorGoodsPort;
    @Mock
    private GetFulfillmentVendorGoodsElementPort getFulfillmentVendorGoodsElementPort;

    @InjectMocks
    private GetAdminProductDetailService getAdminProductDetailService;

    @Test
    @DisplayName("관리자 상품 상세 정보 조회 시 선택 옵션 목록을 그대로 전달하고 풀필먼트 정보를 매핑한다")
    void getAdminProductDetail_mapsFulfillmentInfo() {
        Long id = 100L;
        List<Long> selectedOptionIds = List.of(1L, 2L);
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);
        FulfillmentVendorGoodsInfoResult goodsInfo = goodsInfoWithCstGodCd(String.valueOf(id));
        FulfillmentVendorGoodsElementInfoResult elementInfo = mock(FulfillmentVendorGoodsElementInfoResult.class);

        when(getProductUseCase.getProductInfoIncludingInactive(id, selectedOptionIds)).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)))
                .thenReturn(GetFulfillmentVendorGoodsResult.of(1, List.of(goodsInfo)));
        when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenReturn(Optional.of(elementInfo));

        GetAdminProductDetailResult result = getAdminProductDetailService.getAdminProductDetail(
                id, selectedOptionIds
        );

        assertThat(result.product()).isSameAs(productInfo);
        assertThat(result.fulfillmentGoods()).isSameAs(goodsInfo);
        assertThat(result.fulfillmentGoodsElement()).isSameAs(elementInfo);
        verify(getProductUseCase).getProductInfoIncludingInactive(id, selectedOptionIds);
        verify(getFulfillmentVendorGoodsPort).getFulfillmentVendorGoods(String.valueOf(id));
        verify(getFulfillmentVendorGoodsElementPort).getFulfillmentVendorGoodsElement(String.valueOf(id));
    }

    @Test
    @DisplayName("상품 상세 정보 조회 시 선택 옵션이 없으면 빈 목록으로 조회한다")
    void getAdminProductDetail_emptySelectedOptions_usesEmptyList() {
        Long id = 200L;
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);

        when(getProductUseCase.getProductInfoIncludingInactive(eq(id), anyList())).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)))
                .thenReturn(GetFulfillmentVendorGoodsResult.of(0, List.of()));
        when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenReturn(Optional.empty());

        GetAdminProductDetailResult result = getAdminProductDetailService.getAdminProductDetail(id, null);

        ArgumentCaptor<List<Long>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(getProductUseCase).getProductInfoIncludingInactive(eq(id), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue()).isEmpty();
        assertThat(result.fulfillmentGoods()).isNull();
        assertThat(result.fulfillmentGoodsElement()).isNull();
    }

    @Test
    @DisplayName("풀필먼트 상품 정보 중 유효하지 않은 항목은 무시하고 매칭된 항목만 반환한다")
    void getAdminProductDetail_skipsInvalidFulfillmentGoodsEntries() {
        Long id = 300L;
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);
        FulfillmentVendorGoodsInfoResult invalidGoods = goodsInfoWithCstGodCd(" ");
        FulfillmentVendorGoodsInfoResult validGoods = goodsInfoWithCstGodCd(String.valueOf(id));
        FulfillmentVendorGoodsElementInfoResult elementInfo = mock(FulfillmentVendorGoodsElementInfoResult.class);

        when(getProductUseCase.getProductInfoIncludingInactive(id, List.of())).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)))
                .thenReturn(GetFulfillmentVendorGoodsResult.of(2, Arrays.asList(null, invalidGoods, validGoods)));
        when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenReturn(Optional.of(elementInfo));

        GetAdminProductDetailResult result = getAdminProductDetailService.getAdminProductDetail(id, List.of());

        assertThat(result.fulfillmentGoods()).isSameAs(validGoods);
        assertThat(result.fulfillmentGoodsElement()).isSameAs(elementInfo);
    }

    @Test
    @DisplayName("풀필먼트 상품 정보가 중복될 경우 최초 항목을 반환한다")
    void getAdminProductDetail_returnsFirstDuplicateFulfillmentGoods() {
        Long id = 400L;
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);
        FulfillmentVendorGoodsInfoResult firstGoods = goodsInfoWithCstGodCd(String.valueOf(id));
        FulfillmentVendorGoodsInfoResult secondGoods = goodsInfoWithCstGodCd(String.valueOf(id));
        FulfillmentVendorGoodsElementInfoResult elementInfo = mock(FulfillmentVendorGoodsElementInfoResult.class);

        when(getProductUseCase.getProductInfoIncludingInactive(id, List.of())).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)))
                .thenReturn(GetFulfillmentVendorGoodsResult.of(2, List.of(firstGoods, secondGoods)));
        when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenReturn(Optional.of(elementInfo));

        GetAdminProductDetailResult result = getAdminProductDetailService.getAdminProductDetail(id, List.of());

        assertThat(result.fulfillmentGoods()).isSameAs(firstGoods);
    }

    @Test
    @DisplayName("풀필먼트 구성 조회 결과가 비어 있으면 null을 반환한다")
    void getAdminProductDetail_returnsNullWhenElementEmpty() {
        Long id = 450L;
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);

        when(getProductUseCase.getProductInfoIncludingInactive(id, List.of())).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)))
                .thenReturn(GetFulfillmentVendorGoodsResult.of(0, List.of()));
        when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenReturn(Optional.empty());

        GetAdminProductDetailResult result = getAdminProductDetailService.getAdminProductDetail(id, List.of());

        assertThat(result.fulfillmentGoodsElement()).isNull();
    }

    @Test
    @DisplayName("관리자 상품 상세 정보 조회 중 상품 상세 조회가 실패하면 예외를 전파한다")
    void getAdminProductDetail_getProductInfoFails_propagates() {
        Long id = 500L;
        RuntimeException exception = new RuntimeException("product info fail");

        when(getProductUseCase.getProductInfoIncludingInactive(id, List.of())).thenThrow(exception);

        assertThatThrownBy(() -> getAdminProductDetailService.getAdminProductDetail(id, List.of()))
                .isSameAs(exception);

        verifyNoInteractions(getFulfillmentVendorGoodsPort, getFulfillmentVendorGoodsElementPort);
    }

    @Test
    @DisplayName("관리자 상품 상세 정보 조회 중 풀필먼트 상품 조회가 실패하면 예외를 전파한다")
    void getAdminProductDetail_goodsFails_propagates() {
        Long id = 600L;
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);
        RuntimeException exception = new RuntimeException("goods fail");

        when(getProductUseCase.getProductInfoIncludingInactive(id, List.of())).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id))).thenThrow(exception);
        lenient().when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAdminProductDetailService.getAdminProductDetail(id, List.of()))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("관리자 상품 상세 정보 조회 중 풀필먼트 구성 조회가 실패하면 예외를 전파한다")
    void getAdminProductDetail_elementsFails_propagates() {
        Long id = 700L;
        GetProductInfoWithOptionsResult productInfo = mock(GetProductInfoWithOptionsResult.class);
        RuntimeException exception = new RuntimeException("elements fail");

        when(getProductUseCase.getProductInfoIncludingInactive(id, List.of())).thenReturn(productInfo);
        when(getFulfillmentVendorGoodsPort.getFulfillmentVendorGoods(String.valueOf(id)))
                .thenReturn(GetFulfillmentVendorGoodsResult.of(0, List.of()));
        when(getFulfillmentVendorGoodsElementPort.getFulfillmentVendorGoodsElement(String.valueOf(id)))
                .thenThrow(exception);

        assertThatThrownBy(() -> getAdminProductDetailService.getAdminProductDetail(id, List.of()))
                .isSameAs(exception);
    }

    private FulfillmentVendorGoodsInfoResult goodsInfoWithCstGodCd(String cstGodCd) {
        FulfillmentVendorGoodsInfoResult info = mock(FulfillmentVendorGoodsInfoResult.class);
        when(info.customerGoodsCode()).thenReturn(cstGodCd);
        return info;
    }
}
