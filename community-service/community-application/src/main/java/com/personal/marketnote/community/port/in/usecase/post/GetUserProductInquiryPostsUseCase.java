package com.personal.marketnote.community.port.in.usecase.post;

import com.personal.marketnote.community.port.in.command.post.GetUserProductInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetUserProductInquiryPostsResult;

/**
 * (관리자) 회원 상품 문의 내역 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-01
 * @Description 관리자가 특정 회원의 상품 문의 내역을 조회합니다.
 */
public interface GetUserProductInquiryPostsUseCase {
    /**
     * @param command 회원 상품 문의 내역 조회 커맨드
     * @return 회원 상품 문의 내역 조회 결과 {@link GetUserProductInquiryPostsResult}
     * @Date 2026-04-01
     * @Author 성효빈
     * @Description 관리자가 특정 회원의 상품 문의 내역을 조회합니다.
     */
    GetUserProductInquiryPostsResult getUserProductInquiryPosts(GetUserProductInquiryPostsCommand command);
}
