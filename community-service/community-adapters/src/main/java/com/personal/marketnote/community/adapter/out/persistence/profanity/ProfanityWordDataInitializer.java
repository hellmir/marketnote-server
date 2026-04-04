package com.personal.marketnote.community.adapter.out.persistence.profanity;

import com.personal.marketnote.community.adapter.out.persistence.profanity.entity.ProfanityWordJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.profanity.repository.ProfanityWordJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "profanity.init.enabled", havingValue = "true", matchIfMissing = false)
public class ProfanityWordDataInitializer implements ApplicationRunner {
    private static final int BATCH_SIZE = 500;

    private final ProfanityWordJpaRepository profanityWordJpaRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (profanityWordJpaRepository.count() > 0) {
            log.info("욕설 단어 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        ClassPathResource resource = new ClassPathResource("profanity-words.txt");
        List<ProfanityWordJpaEntity> batch = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                batch.add(ProfanityWordJpaEntity.of(trimmed.toLowerCase()));

                if (batch.size() >= BATCH_SIZE) {
                    profanityWordJpaRepository.saveAll(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            profanityWordJpaRepository.saveAll(batch);
        }

        log.info("욕설 단어 데이터 초기화가 완료되었습니다.");
    }
}
