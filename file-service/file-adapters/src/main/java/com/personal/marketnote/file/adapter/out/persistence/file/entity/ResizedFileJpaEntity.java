package com.personal.marketnote.file.adapter.out.persistence.file.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.file.domain.file.ResizedFile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static lombok.AccessLevel.PROTECTED;

@Entity
@EntityListeners(value = AuditingEntityListener.class)
@Table(name = "resized_file")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ResizedFileJpaEntity extends BaseGeneralEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileJpaEntity file;

    @Column(name = "size", nullable = false, length = 255)
    private String size;

    @Column(name = "s3_url", nullable = false, length = 1024)
    private String storageUrl;

    public static ResizedFileJpaEntity of(FileJpaEntity file, String size) {
        return ResizedFileJpaEntity.builder()
                .file(file)
                .size(size)
                .build();
    }

    public static ResizedFileJpaEntity of(FileJpaEntity file, String size, String storageUrl) {
        return ResizedFileJpaEntity.builder()
                .file(file)
                .size(size)
                .storageUrl(storageUrl)
                .build();
    }

    public void updateFrom(ResizedFile resizedFile) {
        updateActivation(resizedFile);
    }

    private void updateActivation(ResizedFile resizedFile) {
        if (resizedFile.isActive()) {
            activate();
            return;
        }

        if (resizedFile.isInactive()) {
            deactivate();
            return;
        }

        hide();
    }
}


