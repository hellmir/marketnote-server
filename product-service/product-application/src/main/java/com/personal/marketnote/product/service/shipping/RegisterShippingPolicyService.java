package com.personal.marketnote.product.service.shipping;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicyCreateState;
import com.personal.marketnote.product.exception.ShippingPolicyAlreadyExistsException;
import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.shipping.RegisterShippingPolicyUseCase;
import com.personal.marketnote.product.port.out.event.PublishShippingPolicyEventPort;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.SaveShippingPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class RegisterShippingPolicyService implements RegisterShippingPolicyUseCase {

    private final FindShippingPolicyPort findShippingPolicyPort;
    private final SaveShippingPolicyPort saveShippingPolicyPort;
    private final PublishShippingPolicyEventPort publishShippingPolicyEventPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public RegisterShippingPolicyResult registerShippingPolicy(Long sellerId, RegisterShippingPolicyCommand command) {
        validateNoDuplicatePolicy(sellerId);

        ShippingPolicy shippingPolicy = ShippingPolicy.from(ShippingPolicyCreateState.builder()
                .sellerId(sellerId)
                .deliveryCompany(command.deliveryCompany())
                .shippingFee(command.shippingFee())
                .freeShippingThreshold(command.freeShippingThreshold())
                .build());

        Long savedId = saveShippingPolicyPort.save(shippingPolicy);

        publishShippingPolicyEventPort.publishShippingPolicyChangedEvent(
                sellerId, command.shippingFee(), command.freeShippingThreshold(), ShippingPolicyChangeAction.CREATED
        );

        return RegisterShippingPolicyResult.of(savedId);
    }

    private void validateNoDuplicatePolicy(Long sellerId) {
        findShippingPolicyPort.findActiveBySellerId(sellerId)
                .ifPresent(existing -> {
                    throw new ShippingPolicyAlreadyExistsException(sellerId);
                });
    }
}
