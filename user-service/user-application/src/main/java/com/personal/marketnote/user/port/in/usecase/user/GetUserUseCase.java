package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.domain.user.UserSortProperty;
import com.personal.marketnote.user.port.in.result.GetUserInfoResult;
import com.personal.marketnote.user.port.in.result.GetUserResult;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

/**
 * 회원 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 회원 조회 관련 기능을 제공합니다.
 */
public interface GetUserUseCase {
    /**
     * @param referredUserCode 추천인 코드
     * @return 추천 코드 존재 여부 {@link boolean}
     * @Date 2025-12-28
     * @Author 성효빈
     * @Description 회원 존재 여부를 조회합니다.
     */
    boolean existsUser(String referredUserCode);

    /**
     * @param id 회원 ID
     * @return 회원 도메인 {@link User}
     * @Date 2025-12-28
     * @Author 성효빈
     * @Description 활성화된 회원을 조회합니다.
     */
    User getUser(Long id);

    /**
     * @param authVendor 인증 제공자
     * @param oidcId     외부 인증 ID
     * @return 회원 도메인 {@link User}
     * @Date 2025-12-28
     * @Author 성효빈
     * @Description 활성화된 회원을 조회합니다.
     */
    User getUser(AuthVendor authVendor, String oidcId);

    /**
     * @param referredUserCode 추천인 코드
     * @return 회원 도메인 {@link User}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 추천 코드를 통해 회원을 조회합니다.
     */
    User getUser(String referredUserCode);

    /**
     * @param id 회원 ID
     * @return 회원 도메인 {@link User}
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원을 조회합니다.
     */
    User getAllStatusUser(Long id);

    /**
     * @param email 회원 이메일 주소
     * @return 회원 도메인 {@link User}
     * @Date 2025-12-28
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원을 조회합니다.
     */
    User getAllStatusUser(String email);

    /**
     * @param authVendor 인증 제공자
     * @param oidcId     외부 인증 ID
     * @return 회원 도메인 {@link User}
     * @Date 2025-12-30
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원을 조회합니다.
     */
    User getAllStatusUser(AuthVendor authVendor, String oidcId);

    /**
     * @param id 회원 ID
     * @return 회원 키 {@link UUID}
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 회원 키를 조회합니다.
     */
    UUID getUserKey(Long id);

    /**
     * @param id 회원 ID
     * @return 회원 정보 조회 결과 {@link GetUserInfoResult}
     * @Date 2025-12-28
     * @Author 성효빈
     * @Description 활성화된 회원 정보를 조회합니다.
     */
    GetUserInfoResult getUserInfo(Long id);

    /**
     * @param id 회원 ID
     * @return 회원 정보 조회 결과 {@link GetUserInfoResult}
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원 정보를 조회합니다.
     */
    GetUserInfoResult getAllStatusUserInfo(Long id);

    /**
     * @param pageSize      페이지 크기
     * @param pageNumber    페이지 번호
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 속성
     * @param searchTarget  검색 대상
     * @param searchKeyword 검색 키워드
     * @return 회원 목록 조회 결과 {@link List< GetUserResult >}
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원 목록을 조회합니다.
     */
    Page<GetUserResult> getAllStatusUsers(
            int pageSize,
            int pageNumber,
            Sort.Direction sortDirection,
            UserSortProperty sortProperty,
            UserSearchTarget searchTarget,
            String searchKeyword
    );
}
