package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryExposureCommand.ExposureItem;
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
class ManageGifticonCategoryExposureUseCaseTest {

    @InjectMocks
    private ManageGifticonCategoryExposureService manageGifticonCategoryExposureService;

    @Mock
    private FindGifticonCategoryPort findGifticonCategoryPort;

    @Mock
    private UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Test
    @DisplayName("카테고리를 노출로 변경한다")
    void shouldExposeCategory() {
        // given
        GifticonCategory category = createCategory(1L, false);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        ManageGifticonCategoryExposureCommand command = new ManageGifticonCategoryExposureCommand(
                List.of(new ExposureItem(1L, true))
        );

        // when
        manageGifticonCategoryExposureService.manageExposure(command);

        // then
        assertThat(category.isExposed()).isTrue();
        verify(updateGifticonCategoryPort).update(category);
    }

    @Test
    @DisplayName("카테고리를 비노출로 변경한다")
    void shouldUnexposeCategory() {
        // given
        GifticonCategory category = createCategory(1L, true);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        ManageGifticonCategoryExposureCommand command = new ManageGifticonCategoryExposureCommand(
                List.of(new ExposureItem(1L, false))
        );

        // when
        manageGifticonCategoryExposureService.manageExposure(command);

        // then
        assertThat(category.isExposed()).isFalse();
        verify(updateGifticonCategoryPort).update(category);
    }

    @Test
    @DisplayName("여러 카테고리의 노출 여부를 한번에 변경한다")
    void shouldManageMultipleCategoriesExposure() {
        // given
        GifticonCategory category1 = createCategory(1L, false);
        GifticonCategory category2 = createCategory(2L, true);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category1));
        when(findGifticonCategoryPort.findById(2L)).thenReturn(Optional.of(category2));
        ManageGifticonCategoryExposureCommand command = new ManageGifticonCategoryExposureCommand(
                List.of(new ExposureItem(1L, true), new ExposureItem(2L, false))
        );

        // when
        manageGifticonCategoryExposureService.manageExposure(command);

        // then
        assertThat(category1.isExposed()).isTrue();
        assertThat(category2.isExposed()).isFalse();
        verify(updateGifticonCategoryPort).update(category1);
        verify(updateGifticonCategoryPort).update(category2);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리의 노출을 변경하면 GifticonCategoryNotFoundException이 발생한다")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // given
        when(findGifticonCategoryPort.findById(999L)).thenReturn(Optional.empty());
        ManageGifticonCategoryExposureCommand command = new ManageGifticonCategoryExposureCommand(
                List.of(new ExposureItem(999L, true))
        );

        // when & then
        assertThatThrownBy(() -> manageGifticonCategoryExposureService.manageExposure(command))
                .isInstanceOf(GifticonCategoryNotFoundException.class);
        verify(updateGifticonCategoryPort, never()).update(any());
    }

    private GifticonCategory createCategory(Long id, boolean exposed) {
        return GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode(String.valueOf(id))
                .categoryName("카테고리" + id)
                .exposed(exposed)
                .createdAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .build());
    }
}
