package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserJpaEntity;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND EXISTS (
                SELECT 1
                FROM UserOauth2VendorJpaEntity uov
                WHERE 1 = 1
                AND uov.userJpaEntity = u
                AND uov.authVendor = :authVendor
                AND uov.oidcId = :oidcId
                )
            """)
    boolean existsByAuthVendorAndOidcId(@Param("authVendor") AuthVendor authVendor, @Param("oidcId") String oidcId);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND u.nickname = :nickname
            """)
    boolean existsByNickname(@Param("nickname") String nickname);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.email = :email
            """)
    boolean existsByEmail(@Param("email") String email);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND u.phoneNumber = :phoneNumber
            """)
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM UserJpaEntity u
            WHERE u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND u.referenceCode = :referenceCode
            """)
    boolean existsByReferenceCode(@Param("referenceCode") String referenceCode);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND u.id = :id
            ORDER BY u.orderNum ASC
            """)
    Optional<UserJpaEntity> findById(@Param("id") Long id);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND u.referenceCode = :referredUserCode
            ORDER BY u.orderNum ASC
            """)
    Optional<UserJpaEntity> findByReferenceCode(String referredUserCode);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND EXISTS (
              SELECT 1
              FROM UserOauth2VendorJpaEntity uov
              WHERE uov.userJpaEntity = u
              AND uov.authVendor = :authVendor
              AND uov.oidcId = :oidcId
              )
            ORDER BY u.orderNum ASC
            """)
    Optional<UserJpaEntity> findByAuthVendorAndOidcId(
            @Param("authVendor") AuthVendor authVendor, @Param("oidcId") String oidcId
    );

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND u.phoneNumber = :phoneNumber
            ORDER BY u.orderNum ASC
            """)
    Optional<UserJpaEntity> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
                AND u.email = :email
            ORDER BY u.orderNum ASC
            """)
    Optional<UserJpaEntity> findByEmail(@Param("email") String email);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.orderNum = :id
            """)
    Optional<UserJpaEntity> findAllStatusUserById(@Param("id") Long id);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE 1 = 1
                AND u.email = :email
            """)
    Optional<UserJpaEntity> findAllStatusUserByEmail(@Param("email") String email);

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE 1 = 1
            ORDER BY u.orderNum ASC
            """)
    List<UserJpaEntity> findAllStatusUsers();

    @Query("""
            SELECT u
            FROM UserJpaEntity u
            WHERE u.status = com.personal.marketnote.common.domain.EntityStatus.ACTIVE
              AND (
                    :searchKeyword IS NULL
                 OR :searchKeyword = ''
                 OR (
                        ( :byId = true AND str(u.id) LIKE CONCAT('%', :searchKeyword, '%') )
                     OR ( :byNickname = true AND LOWER(u.nickname) LIKE CONCAT('%', LOWER(:searchKeyword), '%') )
                     OR ( :byEmail = true AND LOWER(u.email) LIKE CONCAT('%', LOWER(:searchKeyword), '%') )
                     OR ( :byPhone = true AND LOWER(u.phoneNumber) LIKE CONCAT('%', LOWER(:searchKeyword), '%') )
                     OR ( :byRefCode = true AND LOWER(u.referenceCode) LIKE CONCAT('%', LOWER(:searchKeyword), '%') )
                   )
              )
            """)
    Page<UserJpaEntity> findAllStatusUsersByPage(
            Pageable pageable,
            @Param("byId") boolean byId,
            @Param("byNickname") boolean byNickname,
            @Param("byEmail") boolean byEmail,
            @Param("byPhone") boolean byPhone,
            @Param("byRefCode") boolean byRefCode,
            @Param("searchKeyword") String searchKeyword);
}
