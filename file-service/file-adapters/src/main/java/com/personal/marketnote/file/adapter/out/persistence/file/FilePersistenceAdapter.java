package com.personal.marketnote.file.adapter.out.persistence.file;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.adapter.out.mapper.FileJpaEntityToDomainMapper;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.ResizedFileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.FileJpaRepository;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.ResizedFileJpaRepository;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.exception.FileNotFoundException;
import com.personal.marketnote.file.exception.InvalidFileStorageUrlsSizeException;
import com.personal.marketnote.file.port.out.file.FindFilePort;
import com.personal.marketnote.file.port.out.file.SaveFilesPort;
import com.personal.marketnote.file.port.out.file.UpdateFilePort;
import com.personal.marketnote.file.port.out.file.UpdateFilesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class FilePersistenceAdapter implements SaveFilesPort, FindFilePort, UpdateFilesPort, UpdateFilePort {
    private final FileJpaRepository fileJpaRepository;
    private final ResizedFileJpaRepository resizedFileJpaRepository;

    @Override
    public List<FileDomain> saveAll(List<FileDomain> files, List<String> storageUrls) {
        if (FormatValidator.hasNoValue(files)) {
            return List.of();
        }
        if (FormatValidator.hasNoValue(storageUrls) || storageUrls.size() != files.size()) {
            throw new InvalidFileStorageUrlsSizeException();
        }

        List<FileJpaEntity> toSave = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            toSave.add(FileJpaEntity.from(files.get(i), storageUrls.get(i)));
        }

        List<FileJpaEntity> savedList = fileJpaRepository.saveAll(toSave);
        savedList.forEach(FileJpaEntity::setIdToOrderNum);

        return savedList.stream()
                .map(FileJpaEntityToDomainMapper::mapToDomain)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public Optional<FileDomain> findById(Long id) {
        return FileJpaEntityToDomainMapper.mapToDomain(fileJpaRepository.findById(id).orElse(null));
    }

    @Override
    public List<FileDomain> findByOwner(OwnerType ownerType, Long ownerId) {
        return fileJpaRepository.findAllByOwnerTypeAndOwnerIdOrderByIdAsc(ownerType, ownerId).stream()
                .map(FileJpaEntityToDomainMapper::mapToDomain)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public List<FileDomain> findByOwnerAndSort(OwnerType ownerType, Long ownerId, FileSort sort) {
        List<FileJpaEntity> entities = getEntities(ownerType, ownerId, sort);

        return entities.stream()
                .map(FileJpaEntityToDomainMapper::mapToDomain)
                .flatMap(Optional::stream)
                .toList();
    }

    private List<FileJpaEntity> getEntities(OwnerType ownerType, Long ownerId, FileSort sort) {
        return fileJpaRepository.findByOwnerTypeAndOwnerIdAndSort(
                ownerType, ownerId, sort.name(),
                PageRequest.of(0, sort.getMaxCount())
        );
    }

    @Override
    public void update(FileDomain file) {
        Long id = file.getId();
        FileJpaEntity entity = fileJpaRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));
        entity.updateFrom(file);
    }

    @Override
    public void update(List<FileDomain> files, List<ResizedFile> resizedFiles) {
        List<Long> fileIds = files.stream()
                .map(FileDomain::getId)
                .toList();

        updateFiles(files, fileIds);
        updateResizedFiles(resizedFiles, fileIds);
    }

    private void updateFiles(List<FileDomain> files, List<Long> fileIds) {
        List<FileJpaEntity> entities = fileJpaRepository.findByIds(fileIds);

        for (int i = 0; i < files.size(); i++) {
            FileJpaEntity entity = entities.get(i);
            entity.updateFrom(files.get(i));
        }
    }

    private void updateResizedFiles(List<ResizedFile> resizedFiles, List<Long> fileIds) {
        List<ResizedFileJpaEntity> entities = resizedFileJpaRepository.findAllByFile_IdIn(fileIds);

        for (int i = 0; i < resizedFiles.size(); i++) {
            ResizedFileJpaEntity entity = entities.get(i);
            entity.updateFrom(resizedFiles.get(i));
        }
    }
}
