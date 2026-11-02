package com.personal.marketnote.community.adapter.out.persistence.report.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReportJpaEntity {
    @EmbeddedId
    private ReportId id;

    @Column(name = "reason", nullable = false, length = 2047)
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    public static ReportJpaEntity from(Report report) {
        return ReportJpaEntity.builder()
                .id(new ReportId(report.getTargetType(), report.getTargetId(), report.getReporterId()))
                .reason(report.getReason())
                .build();
    }

    public ReportTargetType getTargetType() {
        return id.getTargetType();
    }

    public Long getTargetId() {
        return id.getTargetId();
    }

    public Long getReporterId() {
        return id.getReporterId();
    }
}

