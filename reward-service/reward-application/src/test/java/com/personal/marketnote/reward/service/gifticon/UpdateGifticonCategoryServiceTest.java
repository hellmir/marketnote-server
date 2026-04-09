package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import com.personal.marketnote.reward.port.in.command.gifticon.UpdateGifticonCategoryCommand;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateGifticonCategoryServiceTest {

    @InjectMocks
    private UpdateGifticonCategoryService updateGifticonCategoryService;

    @Mock
    private FindGifticonCategoryPort findGifticonCategoryPort;

    @Mock
    private UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Test
    @DisplayName("displayName과 iconUrl을 모두 수정한다")
    void shouldUpdateBothDisplayNameAndIconUrl() {
        // given
        GifticonCategory category = createCategory(1L, "커피", null, null);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        UpdateGifticonCategoryCommand command = new UpdateGifticonCategoryCommand(
                1L, "편의점/마트", true, "https://cdn.example.com/icon.png", true
        );

        // when
        updateGifticonCategoryService.updateGifticonCategory(command);

        // then
        assertThat(category.getDisplayName()).isEqualTo("편의점/마트");
        assertThat(category.getIconUrl()).isEqualTo("https://cdn.example.com/icon.png");
        verify(updateGifticonCategoryPort).update(category);
    }

    @Test
    @DisplayName("displayName만 수정하고 iconUrl은 변경하지 않는다")
    void shouldUpdateOnlyDisplayName() {
        // given
        GifticonCategory category = createCategory(1L, "커피", null, "https://old.com/icon.png");
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        UpdateGifticonCategoryCommand command = new UpdateGifticonCategoryCommand(
                1L, "카페/음료", true, null, false
        );

        // when
        updateGifticonCategoryService.updateGifticonCategory(command);

        // then
        assertThat(category.getDisplayName()).isEqualTo("카페/음료");
        assertThat(category.getIconUrl()).isEqualTo("https://old.com/icon.png");
        verify(updateGifticonCategoryPort).update(category);
    }

    @Test
    @DisplayName("displayName을 빈 문자열로 전달하면 null로 초기화된다")
    void shouldResetDisplayNameToNullWhenEmptyString() {
        // given
        GifticonCategory category = createCategory(1L, "커피", "카페/음료", null);
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        UpdateGifticonCategoryCommand command = new UpdateGifticonCategoryCommand(
                1L, "", true, null, false
        );

        // when
        updateGifticonCategoryService.updateGifticonCategory(command);

        // then
        assertThat(category.getDisplayName()).isNull();
        verify(updateGifticonCategoryPort).update(category);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 수정하면 GifticonCategoryNotFoundException이 발생한다")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // given
        when(findGifticonCategoryPort.findById(999L)).thenReturn(Optional.empty());
        UpdateGifticonCategoryCommand command = new UpdateGifticonCategoryCommand(
                999L, "테스트", true, null, false
        );

        // when & then
        assertThatThrownBy(() -> updateGifticonCategoryService.updateGifticonCategory(command))
                .isInstanceOf(GifticonCategoryNotFoundException.class);
        verify(updateGifticonCategoryPort, never()).update(any());
    }

    @Test
    @DisplayName("displayName과 iconUrl 모두 미전달이면 기존 값을 유지한다")
    void shouldKeepExistingValuesWhenNothingProvided() {
        // given
        GifticonCategory category = createCategory(1L, "커피", "카페/음료", "https://old.com/icon.png");
        when(findGifticonCategoryPort.findById(1L)).thenReturn(Optional.of(category));
        UpdateGifticonCategoryCommand command = new UpdateGifticonCategoryCommand(
                1L, null, false, null, false
        );

        // when
        updateGifticonCategoryService.updateGifticonCategory(command);

        // then
        assertThat(category.getDisplayName()).isEqualTo("카페/음료");
        assertThat(category.getIconUrl()).isEqualTo("https://old.com/icon.png");
        verify(updateGifticonCategoryPort).update(category);
    }

    private GifticonCategory createCategory(Long id, String categoryName, String displayName, String iconUrl) {
        return GifticonCategory.from(GifticonCategorySnapshotState.builder()
                .id(id)
                .categoryCode("1")
                .categoryName(categoryName)
                .displayName(displayName)
                .iconUrl(iconUrl)
                .exposed(false)
                .createdAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 4, 3, 10, 0))
                .build());
    }
}
