package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 회원 조회 포트
 *
 * @Author 성효빈
 * @Date 2025-12-26
 * @Description 회원 조회 관련 기능을 제공합니다.
 */
public interface FindUserPort {
    /**
     * @param authVendor 인증 제공자
     * @param oidcId     외부 인증 ID
     * @return 존재 여부 {@link boolean}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 인증 제공자와 외부 인증 ID로 회원 존재 여부를 조회합니다.
     */
    boolean existsByAuthVendorAndOidcId(AuthVendor authVendor, String oidcId);

    /**
     * @param nickname 닉네임
     * @return 존재 여부 {@link boolean}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 닉네임으로 회원 존재 여부를 조회합니다.
     */
    boolean existsByNickname(String nickname);

    /**
     * @param email 이메일 주소
     * @return 존재 여부 {@link boolean}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 이메일로 회원 존재 여부를 조회합니다.
     */
    boolean existsByEmail(String email);

    /**
     * @param phoneNumber 전화번호
     * @return 존재 여부 {@link boolean}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 전화번호로 회원 존재 여부를 조회합니다.
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * @param referenceCode 추천 코드
     * @return 존재 여부 {@link boolean}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 추천 코드로 회원 존재 여부를 조회합니다.
     */
    boolean existsByReferenceCode(String referenceCode);

    /**
     * @param id 회원 ID
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 회원 ID로 회원을 조회합니다.
     */
    Optional<User> findById(Long id);

    /**
     * @param authVendor 인증 제공자
     * @param oidcId     외부 인증 ID
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 인증 제공자와 외부 인증 ID로 회원을 조회합니다.
     */
    Optional<User> findByAuthVendorAndOidcId(AuthVendor authVendor, String oidcId);

    /**
     * @param phoneNumber 전화번호
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 전화번호로 회원을 조회합니다.
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * @param email 이메일 주소
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 이메일로 회원을 조회합니다.
     */
    Optional<User> findByEmail(String email);

    /**
     * @param referredUserCode 추천 코드
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-26
     * @Author 성효빈
     * @Description 추천 코드로 회원을 조회합니다.
     */
    Optional<User> findByReferenceCode(String referredUserCode);

    /**
     * @param id 회원 ID
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원을 조회합니다.
     */
    Optional<User> findAllStatusUserById(Long id);

    /**
     * @param email 이메일 주소
     * @return 회원 {@link Optional<User>}
     * @Date 2025-12-28
     * @Author 성효빈
     * @Description 이메일로 활성화/비활성화/비노출 회원을 조회합니다.
     */
    Optional<User> findAllStatusUserByEmail(String email);

    /**
     * @param id 회원 ID
     * @return 회원 키 {@link Optional<UUID>}
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 회원 키를 조회합니다.
     */
    Optional<UUID> findUserKeyById(Long id);

    /**
     * @return 전체 회원 목록 {@link List<User>}
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원 전체 목록을 조회합니다.
     */
    List<User> findAllStatusUsers();

    /**
     * @param pageable      페이지네이션 정보
     * @param searchTarget  검색 대상
     * @param searchKeyword 검색 키워드
     * @return 회원 목록 {@link Page<User>}
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 활성화/비활성화/비노출 회원 목록을 페이지 단위로 조회합니다.
     */
    Page<User> findAllStatusUsersByPage(Pageable pageable, UserSearchTarget searchTarget, String searchKeyword);
}
