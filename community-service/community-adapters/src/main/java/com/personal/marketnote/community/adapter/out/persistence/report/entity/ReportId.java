package com.personal.marketnote.community.adapter.out.persistence.report.entity;

import com.personal.marketnote.community.domain.report.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportId implements Serializable {
    @Column(name = "target_type", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReportId that = (ReportId) o;
        return Objects.equals(targetType, that.targetType)
                && Objects.equals(targetId, that.targetId)
                && Objects.equals(reporterId, that.reporterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType, targetId, reporterId);
    }
}
