package com.personal.marketnote.community.adapter.out.persistence.report;

import com.personal.marketnote.community.adapter.out.persistence.report.entity.ReportJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.report.repository.ReportJpaRepository;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportPersistenceAdapter")
class ReportPersistenceAdapterTest {

    @Mock
    private ReportJpaRepository reportJpaRepository;

    @InjectMocks
    private ReportPersistenceAdapter adapter;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("도메인을 엔티티로 변환해 저장한다")
        void savesDomainAsEntity() {
            Report report = Report.of(ReportTargetType.REVIEW, 100L, 10L, "광고성 리뷰");
            ArgumentCaptor<ReportJpaEntity> captor = ArgumentCaptor.forClass(ReportJpaEntity.class);

            adapter.save(report);

            verify(reportJpaRepository).save(captor.capture());
            ReportJpaEntity saved = captor.getValue();
            assertThat(saved.getTargetType()).isEqualTo(ReportTargetType.REVIEW);
            assertThat(saved.getTargetId()).isEqualTo(100L);
            assertThat(saved.getReporterId()).isEqualTo(10L);
            assertThat(saved.getReason()).isEqualTo("광고성 리뷰");
        }
    }

    @Nested
    @DisplayName("existsByTargetTypeAndTargetIdAndReporterId")
    class ExistsByTargetAndReporter {

        @Test
        @DisplayName("리포지토리 결과가 true이면 true를 반환한다")
        void delegatesExistsReturningTrue() {
            when(reportJpaRepository.existsByTargetTypeAndTargetIdAndReporterId(
                    ReportTargetType.REVIEW, 100L, 10L
            )).thenReturn(true);

            boolean result = adapter.existsByTargetTypeAndTargetIdAndReporterId(
                    ReportTargetType.REVIEW, 100L, 10L
            );

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("리포지토리 결과가 false이면 false를 반환한다")
        void delegatesExistsReturningFalse() {
            when(reportJpaRepository.existsByTargetTypeAndTargetIdAndReporterId(
                    ReportTargetType.POST, 200L, 20L
            )).thenReturn(false);

            boolean result = adapter.existsByTargetTypeAndTargetIdAndReporterId(
                    ReportTargetType.POST, 200L, 20L
            );

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByTargetTypeAndTargetId")
    class FindByTargetTypeAndTargetId {

        @Test
        @DisplayName("여러 엔티티를 도메인 리스트로 변환해 반환한다")
        void returnsMappedDomainList() {
            ReportJpaEntity first = newPersistedEntity(ReportTargetType.REVIEW, 100L, 10L, "광고성");
            ReportJpaEntity second = newPersistedEntity(ReportTargetType.REVIEW, 100L, 20L, "욕설");
            when(reportJpaRepository.findByTargetTypeAndTargetId(ReportTargetType.REVIEW, 100L))
                    .thenReturn(List.of(first, second));

            List<Report> result = adapter.findByTargetTypeAndTargetId(ReportTargetType.REVIEW, 100L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReporterId()).isEqualTo(10L);
            assertThat(result.get(0).getReason()).isEqualTo("광고성");
            assertThat(result.get(1).getReporterId()).isEqualTo(20L);
            assertThat(result.get(1).getReason()).isEqualTo("욕설");
        }

        @Test
        @DisplayName("엔티티가 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNoEntities() {
            when(reportJpaRepository.findByTargetTypeAndTargetId(ReportTargetType.POST, 999L))
                    .thenReturn(List.of());

            List<Report> result = adapter.findByTargetTypeAndTargetId(ReportTargetType.POST, 999L);

            assertThat(result).isEmpty();
        }
    }

    private ReportJpaEntity newPersistedEntity(
            ReportTargetType targetType, Long targetId, Long reporterId, String reason
    ) {
        Report report = Report.of(targetType, targetId, reporterId, reason);
        ReportJpaEntity entity = ReportJpaEntity.from(report);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        return entity;
    }
}
