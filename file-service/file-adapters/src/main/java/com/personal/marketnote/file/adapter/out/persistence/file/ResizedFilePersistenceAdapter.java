package com.personal.marketnote.file.adapter.out.persistence.file;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.FileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.entity.ResizedFileJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.FileJpaRepository;
import com.personal.marketnote.file.adapter.out.persistence.file.repository.ResizedFileJpaRepository;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.domain.file.ResizedFileSnapshotState;
import com.personal.marketnote.file.port.out.resized.FindResizedFilesPort;
import com.personal.marketnote.file.port.out.resized.SaveResizedFilesPort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class ResizedFilePersistenceAdapter implements SaveResizedFilesPort, FindResizedFilesPort {
    private final FileJpaRepository fileJpaRepository;
    private final ResizedFileJpaRepository resizedFileJpaRepository;

    @Override
    public void saveAll(List<ResizedFile> resizedFiles) {
        if (FormatValidator.hasNoValue(resizedFiles)) {
            return;
        }

        List<ResizedFileJpaEntity> toSave = new ArrayList<>(resizedFiles.size());
        for (ResizedFile f : resizedFiles) {
            FileJpaEntity fileRef = fileJpaRepository.getReferenceById(f.getFileId());
            toSave.add(ResizedFileJpaEntity.of(fileRef, f.getSize(), f.getStorageUrl()));
        }

        resizedFileJpaRepository.saveAll(toSave);
    }

    @Override
    public List<ResizedFile> findByFileIds(List<Long> fileIds) {
        if (FormatValidator.hasValue(fileIds)) {
            return resizedFileJpaRepository.findAllByFile_IdIn(fileIds).stream()
                    .map(e -> ResizedFile.from(ResizedFileSnapshotState.builder()
                            .id(e.getId())
                            .fileId(e.getFile().getId())
                            .size(e.getSize())
                            .storageUrl(e.getStorageUrl())
                            .createdAt(e.getCreatedAt())
                            .status(e.getStatus())
                            .build()
                    )).toList();
        }

        return List.of();
    }
}


