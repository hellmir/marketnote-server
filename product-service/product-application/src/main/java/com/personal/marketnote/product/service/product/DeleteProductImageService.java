package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.command.DeleteProductImageCommand;
import com.personal.marketnote.product.port.in.usecase.product.DeleteProductImageUseCase;
import com.personal.marketnote.product.port.out.file.DeleteProductImagesPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class DeleteProductImageService implements DeleteProductImageUseCase {
    private final FindProductPort findProductPort;
    private final DeleteProductImagesPort deleteProductImagesPort;

    @Override
    public void delete(Long userId, boolean isAdmin, DeleteProductImageCommand command) {
        Long id = command.id();
        if (!isAdmin && !findProductPort.existsByIdAndSellerId(id, userId)) {
            throw new NotProductOwnerException(FIRST_ERROR_CODE, id);
        }

        deleteProductImagesPort.delete(command.fileId());
    }
}
