package com.personal.marketnote.user.adapter.out.persistence.user.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "login_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class LoginHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity userJpaEntity;

    @Enumerated(EnumType.STRING)
    private AuthVendor authVendor;

    @Column(name = "ip_address")
    private String ipAddress;

    public static LoginHistoryJpaEntity of(UserJpaEntity userJpaEntity, AuthVendor authVendor, String ipAddress) {
        return LoginHistoryJpaEntity.builder()
                .userJpaEntity(userJpaEntity)
                .authVendor(authVendor)
                .ipAddress(ipAddress)
                .build();
    }
}
