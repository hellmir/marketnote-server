package com.personal.marketnote.user.adapter.out.persistence.authentication.entity;

import com.personal.marketnote.user.domain.authentication.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>회원의 권한 정보를 저장하는 entity.
 * <p>Role.roleId는 <code>ROLE_</code>로 시작한다.
 *
 * @author 성효빈
 */
@Entity
@Table(name = "role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RoleJpaEntity {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Transient
    private String code;

    public static RoleJpaEntity from(Role role) {
        return new RoleJpaEntity(role.getId(), role.getName(), role.getCode());
    }
}
