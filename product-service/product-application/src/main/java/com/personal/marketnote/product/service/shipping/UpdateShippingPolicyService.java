package com.personal.marketnote.product.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.exception.ShippingPolicyNotFoundException;
import com.personal.marketnote.product.port.in.command.UpdateShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.UpdateShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.shipping.UpdateShippingPolicyUseCase;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;
import com.personal.marketnote.product.port.out.event.PublishShippingPolicyEventPort;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.UpdateShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class UpdateShippingPolicyService implements UpdateShippingPolicyUseCase {

    private final FindShippingPolicyPort findShippingPolicyPort;
    private final UpdateShippingPolicyPort updateShippingPolicyPort;
    private final PublishShippingPolicyEventPort publishShippingPolicyEventPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public UpdateShippingPolicyResult updateShippingPolicy(Long sellerId, UpdateShippingPolicyCommand command) {
        ShippingPolicy shippingPolicy = findShippingPolicy(sellerId);

        shippingPolicy.update(
                command.deliveryCompany(),
                command.shippingFee(),
                command.freeShippingThreshold()
        );

        updateShippingPolicyPort.update(shippingPolicy);

        publishShippingPolicyEventPort.publishShippingPolicyChangedEvent(
                sellerId, shippingPolicy.getShippingFee(), shippingPolicy.getFreeShippingThreshold(), ShippingPolicyChangeAction.UPDATED
        );

        return UpdateShippingPolicyResult.from(shippingPolicy);
    }

    private ShippingPolicy findShippingPolicy(Long sellerId) {
        return findShippingPolicyPort.findActiveBySellerId(sellerId)
                .orElseThrow(() -> new ShippingPolicyNotFoundException(sellerId));
    }
}
