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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

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
                    .orElseThrow(() -> new GifticonCategoryNotFoundException(item.categoryId()));

            if (!category.isExposed()) {
                throw new GifticonCategoryNotExposedException(item.categoryId());
            }

            category.changeOrderNum(item.orderNum());
            updateGifticonCategoryPort.update(category);
        }
    }
}
