package com.personal.marketnote.community.port.in.usecase.review;

import com.personal.marketnote.community.port.in.command.review.GetUserReviewsCommand;
import com.personal.marketnote.community.port.in.result.review.GetUserReviewsResult;

/**
 * (관리자) 회원 리뷰 내역 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-01
 * @Description 관리자가 특정 회원의 리뷰 내역을 조회합니다.
 */
public interface GetUserReviewsUseCase {
    /**
     * @param command 회원 리뷰 내역 조회 커맨드
     * @return 회원 리뷰 내역 조회 결과 {@link GetUserReviewsResult}
     * @Date 2026-04-01
     * @Author 성효빈
     * @Description 관리자가 특정 회원의 리뷰 내역을 조회합니다.
     */
    GetUserReviewsResult getUserReviews(GetUserReviewsCommand command);
}
