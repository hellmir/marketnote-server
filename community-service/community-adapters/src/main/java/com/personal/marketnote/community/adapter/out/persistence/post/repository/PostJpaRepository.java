package com.personal.marketnote.community.adapter.out.persistence.post.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostJpaRepository extends JpaRepository<PostJpaEntity, Long> {
    @Query("""
            SELECT p
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.parentId IS NULL
              AND (:category IS NULL OR p.category = :category)
              AND (:targetGroupType IS NULL OR p.targetGroupType = :targetGroupType)
              AND (:targetGroupId IS NULL OR p.targetGroupId = :targetGroupId)
              AND (
                :isPublicOnly IS NULL
                OR (:isPublicOnly = true AND p.isPrivate = false)
                OR (:isPublicOnly = false)
              )
              AND (:filterUserId IS NULL OR p.userId = :filterUserId)
              AND (
                    :searchKeywordPattern IS NULL
                 OR (
                        :searchInTitle = true
                        AND LOWER(p.title) LIKE :searchKeywordPattern
                    )
                 OR (
                        :searchInContent = true
                        AND LOWER(p.content) LIKE :searchKeywordPattern
                    )
              )
              AND (
                    :cursor IS NULL
                    OR (:isDesc = true AND p.id < :cursor)
                    OR (:isDesc = false AND p.id > :cursor)
              )
            """)
    List<PostJpaEntity> findByBoardAndFilters(
            @Param("board") Board board,
            @Param("category") String category,
            @Param("targetGroupType") PostTargetGroupType targetGroupType,
            @Param("targetGroupId") Long targetGroupId,
            @Param("cursor") Long cursor,
            @Param("isDesc") boolean isDesc,
            @Param("isPublicOnly") Boolean isPublicOnly,
            @Param("filterUserId") Long filterUserId,
            @Param("searchInTitle") boolean searchInTitle,
            @Param("searchInContent") boolean searchInContent,
            @Param("searchKeywordPattern") String searchKeywordPattern,
            @Param("status") EntityStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(p)
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.parentId IS NULL
              AND (:category IS NULL OR p.category = :category)
              AND (:targetGroupType IS NULL OR p.targetGroupType = :targetGroupType)
              AND (:targetGroupId IS NULL OR p.targetGroupId = :targetGroupId)
              AND (
                :isPublicOnly IS NULL
                OR (:isPublicOnly = true AND p.isPrivate = false)
                OR (:isPublicOnly = false)
              )
              AND (:filterUserId IS NULL OR p.userId = :filterUserId)
              AND (
                    :searchKeywordPattern IS NULL
                 OR (
                        :searchInTitle = true
                        AND LOWER(p.title) LIKE :searchKeywordPattern
                    )
                 OR (
                        :searchInContent = true
                        AND LOWER(p.content) LIKE :searchKeywordPattern
                    )
              )
            """)
    long countByBoardAndFilters(
            @Param("board") Board board,
            @Param("category") String category,
            @Param("targetGroupType") PostTargetGroupType targetGroupType,
            @Param("targetGroupId") Long targetGroupId,
            @Param("isPublicOnly") Boolean isPublicOnly,
            @Param("filterUserId") Long filterUserId,
            @Param("searchInTitle") boolean searchInTitle,
            @Param("searchInContent") boolean searchInContent,
            @Param("searchKeywordPattern") String searchKeywordPattern,
            @Param("status") EntityStatus status
    );

    @Query("""
            SELECT COUNT(p)
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.userId = :userId
              AND p.parentId IS NULL
              AND (
                    :searchKeywordPattern IS NULL
                 OR (
                        :searchInTitle = true
                        AND LOWER(p.title) LIKE :searchKeywordPattern
                    )
                 OR (
                        :searchInContent = true
                        AND LOWER(p.content) LIKE :searchKeywordPattern
                    )
              )
            """)
    long countByUserIdAndBoard(
            @Param("userId") Long userId,
            @Param("board") Board board,
            @Param("searchInTitle") boolean searchInTitle,
            @Param("searchInContent") boolean searchInContent,
            @Param("searchKeywordPattern") String searchKeywordPattern,
            @Param("status") EntityStatus status
    );

    @Query("""
            SELECT p
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.userId = :userId
              AND p.parentId IS NULL
              AND (
                    :searchKeywordPattern IS NULL
                 OR (
                        :searchInTitle = true
                        AND LOWER(p.title) LIKE :searchKeywordPattern
                    )
                 OR (
                        :searchInContent = true
                        AND LOWER(p.content) LIKE :searchKeywordPattern
                    )
              )
              AND (
                    :cursor IS NULL
                    OR (:isDesc = true AND p.id < :cursor)
                    OR (:isDesc = false AND p.id > :cursor)
              )
            """)
    List<PostJpaEntity> findByUserIdAndBoard(
            @Param("userId") Long userId,
            @Param("board") Board board,
            @Param("cursor") Long cursor,
            @Param("isDesc") boolean isDesc,
            @Param("searchInTitle") boolean searchInTitle,
            @Param("searchInContent") boolean searchInContent,
            @Param("searchKeywordPattern") String searchKeywordPattern,
            @Param("status") EntityStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.parentId IS NULL
              AND (:category IS NULL OR p.category = :category)
              AND (:targetGroupType IS NULL OR p.targetGroupType = :targetGroupType)
              AND (:targetGroupId IS NULL OR p.targetGroupId = :targetGroupId)
              AND (
                :isPublicOnly IS NULL
                OR (:isPublicOnly = true AND p.isPrivate = false)
                OR (:isPublicOnly = false)
              )
              AND (:filterUserId IS NULL OR p.userId = :filterUserId)
              AND (
                    :searchKeywordPattern IS NULL
                 OR (
                        :searchInTitle = true
                        AND LOWER(p.title) LIKE :searchKeywordPattern
                    )
                 OR (
                        :searchInContent = true
                        AND LOWER(p.content) LIKE :searchKeywordPattern
                    )
              )
              AND (
                    :cursor IS NULL
                    OR (:isDesc = true AND p.id < :cursor)
                    OR (:isDesc = false AND p.id > :cursor)
              )
            ORDER BY
              CASE WHEN :isDesc = true THEN (CASE WHEN EXISTS (SELECT 1 FROM PostJpaEntity c WHERE c.parentId = p.id AND c.status = :status) THEN 1 ELSE 0 END) END DESC,
              CASE WHEN :isDesc = false THEN (CASE WHEN EXISTS (SELECT 1 FROM PostJpaEntity c WHERE c.parentId = p.id AND c.status = :status) THEN 1 ELSE 0 END) END ASC,
              CASE WHEN :isDesc = true THEN p.id END DESC,
              CASE WHEN :isDesc = false THEN p.id END ASC
            """)
    List<PostJpaEntity> findByBoardAndFiltersOrderByAnswered(
            @Param("board") Board board,
            @Param("category") String category,
            @Param("targetGroupType") PostTargetGroupType targetGroupType,
            @Param("targetGroupId") Long targetGroupId,
            @Param("cursor") Long cursor,
            @Param("isDesc") boolean isDesc,
            @Param("isPublicOnly") Boolean isPublicOnly,
            @Param("filterUserId") Long filterUserId,
            @Param("searchInTitle") boolean searchInTitle,
            @Param("searchInContent") boolean searchInContent,
            @Param("searchKeywordPattern") String searchKeywordPattern,
            @Param("status") EntityStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.userId = :userId
              AND p.parentId IS NULL
              AND (
                    :searchKeywordPattern IS NULL
                 OR (
                        :searchInTitle = true
                        AND LOWER(p.title) LIKE :searchKeywordPattern
                    )
                 OR (
                        :searchInContent = true
                        AND LOWER(p.content) LIKE :searchKeywordPattern
                    )
              )
              AND (
                    :cursor IS NULL
                    OR (:isDesc = true AND p.id < :cursor)
                    OR (:isDesc = false AND p.id > :cursor)
              )
            ORDER BY
              CASE WHEN :isDesc = true THEN (CASE WHEN EXISTS (SELECT 1 FROM PostJpaEntity c WHERE c.parentId = p.id AND c.status = :status) THEN 1 ELSE 0 END) END DESC,
              CASE WHEN :isDesc = false THEN (CASE WHEN EXISTS (SELECT 1 FROM PostJpaEntity c WHERE c.parentId = p.id AND c.status = :status) THEN 1 ELSE 0 END) END ASC,
              CASE WHEN :isDesc = true THEN p.id END DESC,
              CASE WHEN :isDesc = false THEN p.id END ASC
            """)
    List<PostJpaEntity> findByUserIdAndBoardOrderByAnswered(
            @Param("userId") Long userId,
            @Param("board") Board board,
            @Param("cursor") Long cursor,
            @Param("isDesc") boolean isDesc,
            @Param("searchInTitle") boolean searchInTitle,
            @Param("searchInContent") boolean searchInContent,
            @Param("searchKeywordPattern") String searchKeywordPattern,
            @Param("status") EntityStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM PostJpaEntity p
            WHERE p.status = :status
              AND p.board = :board
              AND p.userId = :userId
              AND p.parentId IS NULL
            """)
    List<PostJpaEntity> findByUserIdAndBoardByOffset(
            @Param("userId") Long userId,
            @Param("board") Board board,
            @Param("status") EntityStatus status,
            Pageable pageable
    );

    @Query("""
                SELECT p
                FROM PostJpaEntity p
                WHERE p.id IN (
                    SELECT MAX(child.id)
                    FROM PostJpaEntity child
                    WHERE child.parentId IN :parentIds
                      AND child.status = :status
                    GROUP BY child.parentId
                )
                ORDER BY p.id DESC
            """)
    List<PostJpaEntity> findRepliesByParentIds(
            @Param("parentIds") List<Long> parentIds,
            @Param("status") EntityStatus status
    );
}
