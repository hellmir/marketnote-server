package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateEmType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aligo_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoTemplateJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false)
    private String senderKey;

    @Column(nullable = false, unique = true)
    private String templateCode;

    @Column(nullable = false)
    private String templateName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateContent;

    private String templateTitle;

    private String templateSubtitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AligoTemplateType templateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AligoTemplateEmType templateEmType;

    @Column(nullable = false)
    private boolean templateSecure;

    @Column(nullable = false)
    private boolean templateAdvert;

    @Column(length = 2048)
    private String imageUrl;

    private String imageName;

    @Column(nullable = false)
    private String inspStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AligoTemplateStatus templateStatus;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @OneToMany(mappedBy = "aligoTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordering ASC")
    private List<AligoTemplateButtonJpaEntity> buttons = new ArrayList<>();

    public void updateFrom(
            String templateName, String templateContent,
            String templateTitle, String templateSubtitle,
            String inspStatus, AligoTemplateStatus templateStatus,
            String comments, List<AligoTemplateButtonJpaEntity> newButtons
    ) {
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.templateTitle = templateTitle;
        this.templateSubtitle = templateSubtitle;
        this.inspStatus = inspStatus;
        this.templateStatus = templateStatus;
        this.comments = comments;
        this.buttons.clear();
        if (FormatValidator.hasValue(newButtons)) {
            for (AligoTemplateButtonJpaEntity button : newButtons) {
                button.assignTemplate(this);
                this.buttons.add(button);
            }
        }
    }

    public static AligoTemplateJpaEntity create(
            String senderKey, String templateCode, String templateName,
            String templateContent, String templateTitle, String templateSubtitle,
            AligoTemplateType templateType, AligoTemplateEmType templateEmType,
            boolean templateSecure, boolean templateAdvert,
            String imageUrl, String imageName,
            String inspStatus, AligoTemplateStatus templateStatus,
            List<AligoTemplateButtonJpaEntity> buttons
    ) {
        AligoTemplateJpaEntity entity = new AligoTemplateJpaEntity();
        entity.senderKey = senderKey;
        entity.templateCode = templateCode;
        entity.templateName = templateName;
        entity.templateContent = templateContent;
        entity.templateTitle = templateTitle;
        entity.templateSubtitle = templateSubtitle;
        entity.templateType = templateType;
        entity.templateEmType = templateEmType;
        entity.templateSecure = templateSecure;
        entity.templateAdvert = templateAdvert;
        entity.imageUrl = imageUrl;
        entity.imageName = imageName;
        entity.inspStatus = inspStatus;
        entity.templateStatus = templateStatus;
        if (FormatValidator.hasValue(buttons)) {
            for (AligoTemplateButtonJpaEntity button : buttons) {
                button.assignTemplate(entity);
                entity.buttons.add(button);
            }
        }
        return entity;
    }
}
