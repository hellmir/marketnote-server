package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoCategoryTreeResult;
import com.personal.marketnote.notification.port.out.aligo.GetAligoCategoryExternalPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAligoCategoryUseCaseTest {

    @InjectMocks
    private GetAligoCategoryService getAligoCategoryService;

    @Mock
    private GetAligoCategoryExternalPort getAligoCategoryExternalPort;

    @Test
    @DisplayName("카테고리 조회 시 외부 API에서 카테고리 트리를 반환한다")
    void shouldReturnCategoryTreeFromExternalPort() {
        // given
        GetAligoCategoryTreeResult.CategoryResult child = new GetAligoCategoryTreeResult.CategoryResult(
                "001001", "병원", List.of()
        );
        GetAligoCategoryTreeResult.CategoryResult parent = new GetAligoCategoryTreeResult.CategoryResult(
                "001", "건강", List.of(child)
        );
        GetAligoCategoryTreeResult expected = new GetAligoCategoryTreeResult(List.of(parent));

        when(getAligoCategoryExternalPort.getCategoryTree()).thenReturn(expected);

        // when
        GetAligoCategoryTreeResult result = getAligoCategoryService.getCategoryTree();

        // then
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().get(0).code()).isEqualTo("001");
        assertThat(result.categories().get(0).name()).isEqualTo("건강");
        assertThat(result.categories().get(0).children()).hasSize(1);
        assertThat(result.categories().get(0).children().get(0).code()).isEqualTo("001001");
        verify(getAligoCategoryExternalPort).getCategoryTree();
    }

    @Test
    @DisplayName("외부 API 호출 실패 시 예외가 그대로 전파된다")
    void shouldPropagateExceptionFromExternalPort() {
        // given
        when(getAligoCategoryExternalPort.getCategoryTree())
                .thenThrow(new AligoApiRequestFailedException("알리고 API 호출 실패"));

        // when & then
        assertThatThrownBy(() -> getAligoCategoryService.getCategoryTree())
                .isInstanceOf(AligoApiRequestFailedException.class);
        verify(getAligoCategoryExternalPort).getCategoryTree();
    }
}
