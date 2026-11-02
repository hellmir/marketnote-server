package com.personal.marketnote.user.adapter.out.persistence.user.entity;

import com.personal.marketnote.user.domain.user.FingerprintSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_fingerprint",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_user_fingerprint_user_id_fingerprint",
                columnNames = {"user_id", "fingerprint"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserFingerprintJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fingerprint", nullable = false)
    private String fingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private FingerprintSource source;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    public static UserFingerprintJpaEntity of(Long userId, String fingerprint, FingerprintSource source, LocalDateTime now) {
        return UserFingerprintJpaEntity.builder()
                .userId(userId)
                .fingerprint(fingerprint)
                .source(source)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .build();
    }

    public void updateLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
