package com.personal.marketnote.community.adapter.out.persistence.post.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.adapter.out.persistence.post.entity.PostJpaEntity;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostSortProperty;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostQueryRepository {
    List<PostJpaEntity> findByBoardAndFilters(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetGroupId,
            Long cursor,
            boolean isDesc,
            Boolean isPublicOnly,
            Long filterUserId,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status,
            Pageable pageable,
            PostSortProperty sortProperty
    );

    long countByBoardAndFilters(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetGroupId,
            Boolean isPublicOnly,
            Long filterUserId,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status
    );

    long countByUserIdAndBoard(
            Long userId,
            Board board,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status
    );

    List<PostJpaEntity> findByUserIdAndBoard(
            Long userId,
            Board board,
            Long cursor,
            boolean isDesc,
            boolean isAnsweredOnly,
            boolean searchInTitle,
            boolean searchInContent,
            String searchKeywordPattern,
            EntityStatus status,
            Pageable pageable,
            PostSortProperty sortProperty
    );

    List<PostJpaEntity> findByUserIdAndBoardByOffset(
            Long userId,
            Board board,
            EntityStatus status,
            Pageable pageable
    );

    List<PostJpaEntity> findRepliesByParentIds(
            List<Long> parentIds,
            EntityStatus status
    );
}
