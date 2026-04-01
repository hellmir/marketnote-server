package com.personal.marketnote.community.port.in.usecase.post;

import com.personal.marketnote.community.port.in.command.post.GetUserOneOnOneInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetUserOneOnOneInquiryPostsResult;

/**
 * (관리자) 회원 1:1 문의 내역 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-01
 * @Description 관리자가 특정 회원의 1:1 문의 내역을 조회합니다.
 */
public interface GetUserOneOnOneInquiryPostsUseCase {
    /**
     * @param command 회원 1:1 문의 내역 조회 커맨드
     * @return 회원 1:1 문의 내역 조회 결과 {@link GetUserOneOnOneInquiryPostsResult}
     * @Date 2026-04-01
     * @Author 성효빈
     * @Description 관리자가 특정 회원의 1:1 문의 내역을 조회합니다.
     */
    GetUserOneOnOneInquiryPostsResult getUserOneOnOneInquiryPosts(GetUserOneOnOneInquiryPostsCommand command);
}
