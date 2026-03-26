package com.personal.marketnote.common.configuration.kafka;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "dlt_message_resolution",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dlt_resolution_topic_partition_offset",
                columnNames = {"dlt_topic", "partition_number", "offset_number"}
        ),
        indexes = @Index(
                name = "idx_dlt_resolution_original_topic",
                columnList = "original_topic"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DltMessageResolutionJpaEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(name = "dlt_topic", nullable = false, length = 100)
    private String dltTopic;

    @Column(name = "partition_number", nullable = false)
    private int partitionNumber;

    @Column(name = "offset_number", nullable = false)
    private long offsetNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution", nullable = false, length = 20)
    private DltResolutionStatus resolution;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "reason", length = 500)
    private String reason;

    private DltMessageResolutionJpaEntity(String originalTopic, String dltTopic,
                                          int partitionNumber, long offsetNumber,
                                          DltResolutionStatus resolution,
                                          String resolvedBy, LocalDateTime resolvedAt,
                                          String reason) {
        this.originalTopic = originalTopic;
        this.dltTopic = dltTopic;
        this.partitionNumber = partitionNumber;
        this.offsetNumber = offsetNumber;
        this.resolution = resolution;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.reason = reason;
    }

    public static DltMessageResolutionJpaEntity of(String originalTopic, String dltTopic,
                                                   int partitionNumber, long offsetNumber,
                                                   DltResolutionStatus resolution,
                                                   String resolvedBy, LocalDateTime resolvedAt,
                                                   String reason) {
        return new DltMessageResolutionJpaEntity(
                originalTopic, dltTopic, partitionNumber, offsetNumber,
                resolution, resolvedBy, resolvedAt, reason
        );
    }

    public String toResolutionKey() {
        return partitionNumber + ":" + offsetNumber;
    }
}
