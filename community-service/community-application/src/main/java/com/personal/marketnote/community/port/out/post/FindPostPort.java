package com.personal.marketnote.community.port.out.post;

import com.personal.marketnote.community.domain.post.*;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 게시글 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 게시글 조회 관련 기능을 제공합니다.
 */
public interface FindPostPort {
    /**
     * @param id 게시글 ID
     * @return 게시글 존재 여부 {@link boolean}
     * @Date 2026-01-13
     * @Author 성효빈
     * @Description 게시글 존재 여부를 조회합니다.
     */
    boolean existsById(Long id);

    /**
     * @param board           게시판
     * @param category        카테고리
     * @param targetGroupType 대상 그룹 유형
     * @param targetId        대상 ID
     * @param cursor          커서(무한 스크롤 페이지 설정)
     * @param pageable        페이지네이션 정보
     * @param isDesc          내림차순 여부
     * @param sortProperty    정렬 속성
     * @param userId          회원 ID
     * @param filter          필터 카테고리
     * @param filterValue     필터 값
     * @param searchTarget    검색 키워드 카테고리
     * @param searchKeyword   검색 키워드
     * @return 게시글 목록 {@link Posts}
     * @Date 2025-12-06
     * @Author 성효빈
     * @Description 게시글 목록을 조회합니다.
     */
    Posts findPublicPosts(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetId,
            Long cursor,
            Pageable pageable,
            boolean isDesc,
            PostSortProperty sortProperty,
            Long userId,
            PostFilterCategory filter,
            PostFilterValue filterValue,
            PostSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param userId        회원 ID
     * @param board         게시판
     * @param cursor        커서(무한 스크롤 페이지 설정)
     * @param pageable      페이지네이션 정보
     * @param isDesc        내림차순 여부
     * @param sortProperty  정렬 속성
     * @param searchTarget  검색 키워드 카테고리
     * @param searchKeyword 검색 키워드
     * @return 게시글 목록 {@link Posts}
     * @Date 2025-12-06
     * @Author 성효빈
     * @Description 회원 게시글 목록을 조회합니다.
     */
    Posts findUserPosts(
            Long userId,
            Board board,
            Long cursor,
            Pageable pageable,
            boolean isDesc,
            PostSortProperty sortProperty,
            PostSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param board           게시판
     * @param category        카테고리
     * @param targetGroupType 대상 그룹 유형
     * @param targetGroupId   대상 그룹 ID
     * @param userId          회원 ID
     * @param filter          필터 카테고리
     * @param filterValue     필터 값
     * @param searchTarget    검색 키워드 카테고리
     * @param searchKeyword   검색 키워드
     * @return 게시글 개수 {@link long}
     * @Date 2025-12-06
     * @Author 성효빈
     * @Description 공개 게시판 게시글 개수를 조회합니다.
     */
    long countPublicPosts(
            Board board,
            String category,
            PostTargetGroupType targetGroupType,
            Long targetGroupId,
            Long userId,
            PostFilterCategory filter,
            PostFilterValue filterValue,
            PostSearchTarget searchTarget,
            String searchKeyword
    );

    /**
     * @param userId        회원 ID
     * @param board         게시판
     * @param searchTarget  검색 키워드 카테고리
     * @param searchKeyword 검색 키워드
     * @return 게시글 개수 {@link long}
     * @Date 2025-12-06
     * @Author 성효빈
     * @Description 회원 게시판 게시글 개수를 조회합니다.
     */
    long countUserPosts(Long userId, Board board, PostSearchTarget searchTarget, String searchKeyword);

    /**
     * @param id 게시글 ID
     * @return 게시글 {@link Post}
     * @Date 2026-01-29
     * @Author 성효빈
     * @Description 게시글과 하위 답글 목록을 조회합니다.
     */
    Optional<Post> findByIdWithReplies(Long id);

    /**
     * @param id 게시글 ID
     * @return 게시글 {@link Post}
     * @Date 2026-01-15
     * @Author 성효빈
     * @Description 게시글을 조회합니다.
     */
    Optional<Post> findById(Long id);
}
