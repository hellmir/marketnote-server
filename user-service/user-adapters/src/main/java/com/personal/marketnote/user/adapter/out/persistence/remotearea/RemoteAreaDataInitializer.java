package com.personal.marketnote.user.adapter.out.persistence.remotearea;

import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaCreateState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "remote-area.init.enabled", havingValue = "true", matchIfMissing = false)
public class RemoteAreaDataInitializer implements ApplicationRunner {
    private static final int BATCH_SIZE = 500;

    private final RemoteAreaJpaRepository remoteAreaJpaRepository;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void run(ApplicationArguments args) throws Exception {
        if (remoteAreaJpaRepository.count() > 0) {
            log.info("도서산간 지역 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        ClassPathResource resource = new ClassPathResource("remote-areas.csv");
        List<RemoteAreaJpaEntity> batch = new ArrayList<>();
        boolean isFirstLine = true;
        int totalCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                RemoteAreaJpaEntity entity = parseLine(trimmed);
                batch.add(entity);

                if (batch.size() >= BATCH_SIZE) {
                    remoteAreaJpaRepository.saveAll(batch);
                    totalCount += batch.size();
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            remoteAreaJpaRepository.saveAll(batch);
            totalCount += batch.size();
        }

        log.info("도서산간 지역 데이터 초기화가 완료되었습니다. 총 {}건", totalCount);
    }

    private RemoteAreaJpaEntity parseLine(String line) {
        String[] columns = line.split(",", -1);

        String province = columns[0].trim();
        String district = columns.length > 1 ? columns[1].trim() : "";
        String village = columns.length > 2 ? columns[2].trim() : "";
        String subarea = columns.length > 3 ? normalizeSubarea(columns[3].trim()) : "";

        RemoteArea remoteArea = RemoteArea.from(
                RemoteAreaCreateState.builder()
                        .province(province)
                        .district(district)
                        .village(village)
                        .subarea(subarea)
                        .build()
        );

        return RemoteAreaJpaEntity.from(remoteArea);
    }

    private String normalizeSubarea(String subarea) {
        if (subarea.startsWith("(") && subarea.endsWith(")")) {
            return "";
        }
        return subarea;
    }
}
