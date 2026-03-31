package com.personal.marketnote.file.adapter.out.persistence.file.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static lombok.AccessLevel.PROTECTED;

@Entity
@EntityListeners(value = AuditingEntityListener.class)
@Table(name = "files")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FileJpaEntity extends BaseGeneralEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private OwnerType ownerType;

    @Column(nullable = false)
    private Long ownerId;

    @Column(name = "sort", nullable = false, length = 31)
    private String sort;

    @Column(name = "extension", nullable = false, length = 31)
    private String extension;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "s3_url", nullable = false, length = 511)
    private String storageUrl;

    private Long orderNum;

    public static FileJpaEntity from(FileDomain domain, String storageUrl) {
        return FileJpaEntity.builder()
                .ownerType(domain.getOwnerType())
                .ownerId(domain.getOwnerId())
                .sort(domain.getSort().name())
                .extension(domain.getExtension())
                .name(domain.getName())
                .storageUrl(storageUrl)
                .build();
    }

    public void setIdToOrderNum() {
        orderNum = getId();
    }

    public void updateFrom(FileDomain file) {
        updateActivation(file);
        storageUrl = file.getStorageUrl();
        name = file.getName();
        extension = file.getExtension();
        sort = file.getSort().name();
        ownerType = file.getOwnerType();
        ownerId = file.getOwnerId();
        orderNum = file.getOrderNum();
    }

    private void updateActivation(FileDomain fileDomain) {
        if (fileDomain.isActive()) {
            activate();
            return;
        }

        if (fileDomain.isInactive()) {
            deactivate();
            return;
        }

        hide();
    }
}
