package com.personal.marketnote.user.adapter.out.persistence.user.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.user.domain.user.Terms;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class TermsJpaEntity extends BaseGeneralEntity {
    @Column(nullable = false, length = 511)
    private String content;

    private Boolean requiredYn;

    public static TermsJpaEntity from(Terms terms) {
        return TermsJpaEntity.builder()
                .content(terms.getContent())
                .requiredYn(terms.getRequiredYn())
                .build();
    }
}
