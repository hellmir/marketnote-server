package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aligo_template_button")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoTemplateButtonJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aligo_template_id", nullable = false)
    private AligoTemplateJpaEntity aligoTemplate;

    @Column(nullable = false)
    private int ordering;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AligoTemplateButtonLinkType linkType;

    @Column(length = 2048)
    private String linkMo;

    @Column(length = 2048)
    private String linkPc;

    @Column(length = 2048)
    private String linkIos;

    @Column(length = 2048)
    private String linkAnd;

    public void assignTemplate(AligoTemplateJpaEntity template) {
        this.aligoTemplate = template;
    }

    public static AligoTemplateButtonJpaEntity create(
            int ordering, String name, AligoTemplateButtonLinkType linkType,
            String linkMo, String linkPc, String linkIos, String linkAnd
    ) {
        AligoTemplateButtonJpaEntity entity = new AligoTemplateButtonJpaEntity();
        entity.ordering = ordering;
        entity.name = name;
        entity.linkType = linkType;
        entity.linkMo = linkMo;
        entity.linkPc = linkPc;
        entity.linkIos = linkIos;
        entity.linkAnd = linkAnd;
        return entity;
    }
}
