package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.LoginHistoryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginHistoryJpaRepository extends JpaRepository<LoginHistoryJpaEntity, Long> {
    @Query("""
            SELECT lh
            FROM LoginHistoryJpaEntity lh
            WHERE lh.userJpaEntity.id = :userId
            """)
    Page<LoginHistoryJpaEntity> findLoginHistoriesByUserId(Pageable pageable, @Param("userId") Long userId);

    @Query("""
            SELECT lh
            FROM LoginHistoryJpaEntity lh
            WHERE 1 = 1
              AND (
                    :searchKeyword = ''
                 OR (
                        ( :byUserId = true AND str(lh.userJpaEntity.id) LIKE CONCAT('%', :searchKeyword, '%') )
                     OR ( :byIp = true AND LOWER(lh.ipAddress) LIKE CONCAT('%', LOWER(:searchKeyword), '%') )
                   )
              )
            """)
    Page<LoginHistoryJpaEntity> findLoginHistoriesByPage(
            Pageable pageable,
            @Param("byUserId") boolean byUserId,
            @Param("byIp") boolean byIp,
            @Param("searchKeyword") String searchKeyword
    );
}
