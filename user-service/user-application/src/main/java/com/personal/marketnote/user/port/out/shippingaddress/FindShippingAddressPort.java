package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;

import java.util.List;
import java.util.Optional;

/**
 * 배송지 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 배송지 조회 관련 기능을 제공합니다.
 */
public interface FindShippingAddressPort {
    /**
     * @param userId      회원 ID
     * @param addressType 배송지 유형
     * @return 배송지 존재 여부 {@link boolean}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 회원의 특정 유형 배송지 존재 여부를 조회합니다.
     */
    boolean existsByUserIdAndAddressType(Long userId, ShippingAddressType addressType);

    /**
     * @param userId      회원 ID
     * @param addressType 배송지 유형
     * @return 배송지 개수 {@link long}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 회원의 특정 유형 배송지 개수를 조회합니다.
     */
    long countByUserIdAndAddressType(Long userId, ShippingAddressType addressType);

    /**
     * @param userId 회원 ID
     * @return 배송지 존재 여부 {@link boolean}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 회원의 배송지 존재 여부를 조회합니다.
     */
    boolean existsByUserId(Long userId);

    /**
     * @param userId 회원 ID
     * @return 배송지 목록 {@link List<ShippingAddress>}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 회원의 전체 배송지 목록을 조회합니다.
     */
    List<ShippingAddress> findAllByUserId(Long userId);

    /**
     * @param id     배송지 ID
     * @param userId 회원 ID
     * @return 배송지 {@link Optional<ShippingAddress>}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 배송지를 조회합니다.
     */
    Optional<ShippingAddress> findByIdAndUserId(Long id, Long userId);

    /**
     * @param userId 회원 ID
     * @return 기본 배송지 목록 {@link List<ShippingAddress>}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 회원의 기본 배송지 목록을 조회합니다.
     */
    List<ShippingAddress> findDefaultsByUserId(Long userId);
}
