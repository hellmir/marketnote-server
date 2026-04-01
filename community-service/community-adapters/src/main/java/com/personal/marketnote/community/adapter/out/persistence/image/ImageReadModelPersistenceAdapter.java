package com.personal.marketnote.community.adapter.out.persistence.image;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.image.entity.ImageReadModelJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.image.repository.ImageReadModelJpaRepository;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.file.FindReviewImagesPort;
import com.personal.marketnote.community.port.out.file.SaveImageReadModelPort;
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
public class ImageReadModelPersistenceAdapter implements FindPostImagesPort, FindReviewImagesPort, SaveImageReadModelPort {
    private final ImageReadModelJpaRepository imageReadModelJpaRepository;

    @Override
    public Optional<GetFilesResult> findImagesByPostIdAndSort(Long postId, FileSort sort) {
        return findImagesByTargetIdAndSort(postId, sort);
    }

    @Override
    public Optional<GetFilesResult> findImagesByReviewIdAndSort(Long reviewId, FileSort sort) {
        return findImagesByTargetIdAndSort(reviewId, sort);
    }

    private Optional<GetFilesResult> findImagesByTargetIdAndSort(Long targetId, FileSort sort) {
        List<ImageReadModelJpaEntity> entities =
                imageReadModelJpaRepository.findByTargetIdAndFileSortAndStatusOrderBySortOrderAsc(
                        targetId, sort.name(), EntityStatus.ACTIVE
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
                        List.of(),
                        entity.getSortOrder().longValue()
                ))
                .toList();

        return Optional.of(new GetFilesResult(fileResults));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void upsert(Long imageId, Long targetId, String targetType,
                       String fileSort, String imageUrl, Integer sortOrder) {
        Optional<ImageReadModelJpaEntity> existing = imageReadModelJpaRepository.findByImageId(imageId);

        if (existing.isPresent()) {
            existing.get().updateFrom(targetId, targetType, fileSort, imageUrl, sortOrder);
            return;
        }

        try {
            ImageReadModelJpaEntity entity = ImageReadModelJpaEntity.of(
                    imageId, targetId, targetType, fileSort, imageUrl, sortOrder
            );
            imageReadModelJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            log.info("이미지 Read Model 중복 저장 (멱등 처리). imageId={}", imageId);
            imageReadModelJpaRepository.findByImageId(imageId)
                    .ifPresent(entity -> entity.updateFrom(targetId, targetType, fileSort, imageUrl, sortOrder));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void deactivateByImageId(Long imageId) {
        imageReadModelJpaRepository.findByImageId(imageId)
                .ifPresent(ImageReadModelJpaEntity::markInactive);
    }
}
