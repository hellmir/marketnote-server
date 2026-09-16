package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.mapper.FulfillmentGoodsSyncedEventMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.out.event.PublishFulfillmentGoodsSyncedEventPort;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class UpdateFulfillmentGoodsService implements UpdateFulfillmentGoodsUseCase {
    private final UpdateFulfillmentGoodsPort updateFulfillmentGoodsPort;
    private final PublishFulfillmentGoodsSyncedEventPort publishFulfillmentGoodsSyncedEventPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public UpdateFulfillmentGoodsResult updateGoods(UpdateFulfillmentGoodsCommand command) {
        UpdateFulfillmentGoodsResult result = updateFulfillmentGoodsPort.updateGoods(command);

        for (UpdateFulfillmentGoodsItemCommand item : command.goods()) {
            publishFulfillmentGoodsSyncedEventPort.publish(
                    FulfillmentGoodsSyncedEventMapper.mapToEvent(item)
            );
        }

        return result;
    }
}
