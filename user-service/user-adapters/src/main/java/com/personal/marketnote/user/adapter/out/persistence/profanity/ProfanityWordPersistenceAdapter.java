package com.personal.marketnote.user.adapter.out.persistence.profanity;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.profanity.entity.ProfanityWordJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.profanity.repository.ProfanityWordJpaRepository;
import com.personal.marketnote.user.port.out.profanity.FindProfanityWordPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProfanityWordPersistenceAdapter implements FindProfanityWordPort {
    private final ProfanityWordJpaRepository profanityWordJpaRepository;
    private Set<String> profanityWords = new HashSet<>();

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        List<ProfanityWordJpaEntity> entities = profanityWordJpaRepository.findAllByStatus(EntityStatus.ACTIVE);
        Set<String> words = new HashSet<>();
        for (ProfanityWordJpaEntity entity : entities) {
            words.add(entity.getWord().toLowerCase());
        }
        this.profanityWords = words;
    }

    @Override
    public boolean containsProfanity(String text) {
        String lowerText = text.toLowerCase();
        for (String word : profanityWords) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
