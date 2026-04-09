package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotExposedException;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand.OrderItem;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageGifticonCategoryOrderServiceTest {

    @InjectMocks
    private ManageGifticonCategoryOrderService manageGifticonCategoryOrderService;

    @Mock
    private FindGifticonCategoryPort findGifticonCategoryPort;

    @Mock
    private UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Test
    @DisplayName("노출된 카테고리의 노출 순서를 설정한다")
    void shouldSetOrderNumForExposedCategory() {
        // given
        GifticonCategory category = createCategory(1L, true, null);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        ManageGifticonCategoryOrderCommand command = new ManageGifticonCategoryOrderCommand(
                List.of(new OrderItem(1L, 1))
        );

        // when
        manageGifticonCategoryOrderService.manageOrder(command);

        // then
        assertThat(category.getOrderNum()).isEqualTo(1);
        verify(updateGifticonCategoryPort).update(category);
    }

    @Test
    @DisplayName("여러 카테고리의 노출 순서를 한번에 설정한다")
    void shouldSetOrderNumForMultipleCategories() {
        // given
        GifticonCategory category1 = createCategory(1L, true, null);
        GifticonCategory category2 = createCategory(2L, true, null);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category1));
        when(findGifticonCategoryPort.findById(2L)).thenReturn(Optional.of(category2));
        ManageGifticonCategoryOrderCommand command = new ManageGifticonCategoryOrderCommand(
                List.of(new OrderItem(1L, 1), new OrderItem(2L, 2))
        );

        // when
        manageGifticonCategoryOrderService.manageOrder(command);

        // then
        assertThat(category1.getOrderNum()).isEqualTo(1);
        assertThat(category2.getOrderNum()).isEqualTo(2);
        verify(updateGifticonCategoryPort).update(category1);
        verify(updateGifticonCategoryPort).update(category2);
    }

    @Test
    @DisplayName("비노출 카테고리에 노출 순서를 설정하면 GifticonCategoryNotExposedException이 발생한다")
    void shouldThrowExceptionWhenCategoryNotExposed() {
        // given
        GifticonCategory category = createCategory(1L, false, null);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        ManageGifticonCategoryOrderCommand command = new ManageGifticonCategoryOrderCommand(
                List.of(new OrderItem(1L, 1))
        );

        // when & then
        assertThatThrownBy(() -> manageGifticonCategoryOrderService.manageOrder(command))
                .isInstanceOf(GifticonCategoryNotExposedException.class);
        verify(updateGifticonCategoryPort, never()).update(any());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리의 순서를 설정하면 GifticonCategoryNotFoundException이 발생한다")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // given
        when(findGifticonCategoryPort.findById(999L)).thenReturn(Optional.empty());
        ManageGifticonCategoryOrderCommand command = new ManageGifticonCategoryOrderCommand(
                List.of(new OrderItem(999L, 1))
        );

        // when & then
        assertThatThrownBy(() -> manageGifticonCategoryOrderService.manageOrder(command))
                .isInstanceOf(GifticonCategoryNotFoundException.class);
        verify(updateGifticonCategoryPort, never()).update(any());
    }

    @Test
    @DisplayName("기존 노출 순서를 새 값으로 변경한다")
    void shouldChangeExistingOrderNum() {
        // given
        GifticonCategory category = createCategory(1L, true, 3);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        ManageGifticonCategoryOrderCommand command = new ManageGifticonCategoryOrderCommand(
                List.of(new OrderItem(1L, 5))
        );

        // when
        manageGifticonCategoryOrderService.manageOrder(command);

        // then
        assertThat(category.getOrderNum()).isEqualTo(5);
        verify(updateGifticonCategoryPort).update(category);
    }

    private GifticonCategory createCategory(Long id, boolean exposed, Integer orderNum) {
        return GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode(String.valueOf(id))
                .categoryName("카테고리" + id)
                .exposed(exposed)
                .orderNum(orderNum)
                .createdAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .build());
    }
}
