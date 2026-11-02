package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.in.usecase.cart.DeleteCartProductUseCase;
import com.personal.marketnote.product.port.out.cart.DeleteCartProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteCartProductService implements DeleteCartProductUseCase {
    private final DeleteCartProductPort deleteCartProductPort;

    @Override
    public void deleteCartProducts(DeleteCartProductCommand command) {
        deleteCartProductPort.delete(command.userId(), command.pricePolicyIds());
    }

    @Override
    public void deleteAllCartProducts(Long userId) {
        deleteCartProductPort.deleteAll(userId);
    }
}
