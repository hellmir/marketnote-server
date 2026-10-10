package com.personal.marketnote.product.adapter.out.persistence.image;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.image.entity.ImageReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.image.entity.ImageReadModelResizedFileJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.image.repository.ImageReadModelJpaRepository;
import com.personal.marketnote.product.port.out.file.FindProductImagesPort;
import com.personal.marketnote.product.port.out.file.SaveImageReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@PersistenceAdapter
@RequiredArgsConstructor
public class ImageReadModelPersistenceAdapter implements FindProductImagesPort, SaveImageReadModelPort {
    private final ImageReadModelJpaRepository imageReadModelJpaRepository;

    @Override
    public Optional<GetFilesResult> findImagesByProductIdAndSort(Long productId, FileSort sort) {
        List<ImageReadModelJpaEntity> entities =
                imageReadModelJpaRepository.findByTargetIdAndFileSortAndStatusOrderBySortOrderAsc(
                        productId, sort.name(), EntityStatus.ACTIVE
                );

        if (FormatValidator.hasNoValue(entities)) {
            return Optional.empty();
        }

        List<GetFileResult> fileResults = entities.stream()
                .map(entity -> new GetFileResult(
                        entity.getImageId(),
                        entity.getFileSort(),
                        null,
                        null,
                        entity.getImageUrl(),
                        entity.getResizedFiles().stream()
                                .map(ImageReadModelResizedFileJpaEntity::getStorageUrl)
                                .toList(),
                        entity.getSortOrder().longValue()
                ))
                .toList();

        return Optional.of(new GetFilesResult(fileResults));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(
            Long imageId, Long targetId, String targetType, String fileSort,
            String imageUrl, Integer sortOrder, List<ResizedImageInput> resizedImages
    ) {
        List<ImageReadModelJpaEntity.ResizedFileInput> resizedInputs = toEntityInputs(resizedImages);
        Optional<ImageReadModelJpaEntity> existing = imageReadModelJpaRepository.findByImageId(imageId);

        if (existing.isPresent()) {
            existing.get().updateFrom(targetId, targetType, fileSort, imageUrl, sortOrder, resizedInputs);
            return;
        }

        try {
            ImageReadModelJpaEntity entity = ImageReadModelJpaEntity.of(
                    imageId, targetId, targetType, fileSort, imageUrl, sortOrder
            );
            entity.replaceResizedFiles(resizedInputs);
            imageReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("이미지 Read Model 중복 저장 (멱등 처리). imageId={}", imageId);
            imageReadModelJpaRepository.findByImageId(imageId)
                    .ifPresent(entity -> entity.updateFrom(
                            targetId, targetType, fileSort, imageUrl, sortOrder, resizedInputs
                    ));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deactivateByImageId(Long imageId) {
        imageReadModelJpaRepository.findByImageId(imageId)
                .ifPresent(ImageReadModelJpaEntity::markInactive);
    }

    private List<ImageReadModelJpaEntity.ResizedFileInput> toEntityInputs(List<ResizedImageInput> resizedImages) {
        if (FormatValidator.hasNoValue(resizedImages)) {
            return List.of();
        }
        return resizedImages.stream()
                .map(input -> new ImageReadModelJpaEntity.ResizedFileInput(input.size(), input.storageUrl()))
                .toList();
    }
}
