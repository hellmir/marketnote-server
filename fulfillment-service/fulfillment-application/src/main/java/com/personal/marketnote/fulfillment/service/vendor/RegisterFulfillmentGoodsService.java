package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistration;
import com.personal.marketnote.fulfillment.mapper.FulfillmentCommandToStateMapper;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.out.goods.SaveFulfillmentGoodsRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class RegisterFulfillmentGoodsService implements RegisterFulfillmentGoodsUseCase {
    private final RegisterFulfillmentGoodsPort registerFulfillmentGoodsPort;
    private final SaveFulfillmentGoodsRegistrationPort saveFulfillmentGoodsRegistrationPort;

    @Override
    public RegisterFulfillmentGoodsResult registerGoods(RegisterFulfillmentGoodsCommand command) {
        return registerFulfillmentGoodsPort.registerGoods(command);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterFulfillmentGoodsResult registerGoodsIdempotent(RegisterFulfillmentGoodsCommand command) {
        FulfillmentGoodsRegistration registration = FulfillmentGoodsRegistration.from(
                FulfillmentCommandToStateMapper.mapToGoodsRegistrationCreateState(command)
        );
        saveFulfillmentGoodsRegistrationPort.save(registration);

        return registerFulfillmentGoodsPort.registerGoods(command);
    }
}
