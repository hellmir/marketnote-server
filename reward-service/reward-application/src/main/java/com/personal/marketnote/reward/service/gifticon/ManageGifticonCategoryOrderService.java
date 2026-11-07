package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotExposedException;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand.OrderItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonCategoryOrderUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonCategoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ManageGifticonCategoryOrderService implements ManageGifticonCategoryOrderUseCase {
    private final FindGifticonCategoryPort findGifticonCategoryPort;
    private final UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Override
    public void manageOrder(ManageGifticonCategoryOrderCommand command) {
        for (OrderItem item : command.items()) {
            GifticonCategory category = findGifticonCategoryPort.findById(item.categoryId())
                    .orElseThrow(() -> {
                        log.warn("기프티콘 카테고리를 찾을 수 없습니다. categoryId={}", item.categoryId());
                        return new GifticonCategoryNotFoundException();
                    });

            if (!category.isExposed()) {
                log.warn("노출되지 않은 카테고리에는 노출 순서를 설정할 수 없습니다. categoryId={}", item.categoryId());
                throw new GifticonCategoryNotExposedException();
            }

            category.changeOrderNum(item.orderNum());
            updateGifticonCategoryPort.update(category);
        }
    }
}
