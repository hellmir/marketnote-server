package com.personal.marketnote.community.adapter.out.persistence.profanity.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "profanity_words")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProfanityWordJpaEntity extends BaseGeneralEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String word;

    public static ProfanityWordJpaEntity of(String word) {
        return ProfanityWordJpaEntity.builder()
                .word(word)
                .build();
    }
}
